package com.teamtea.craton.common.worldgen.geology;

import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Climate;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;

/** Block-column climate reconstructed from the quart lattice, at a fixed reference height. */
public class GeologyClimateSampler {
    private final Climate.Sampler sampler;
    private final int quartY;
    // One buildSurface invocation; the chunk itself uses 25 points, deposit anchors may query farther away.
    private final Long2ObjectLinkedOpenHashMap<Climate.TargetPoint> lattice=new Long2ObjectLinkedOpenHashMap<>(32);

    public GeologyClimateSampler(Climate.Sampler sampler, int referenceY) {
        this.sampler = sampler;
        this.quartY = QuartPos.fromBlock(referenceY);
    }

    /**
     * Regional uplift proxy for the vanilla overworld depth field. Its vertical
     * gradient is -1/128; remove the fixed sample height before estimating the
     * depth-zero elevation. This is not a local heightmap or a cave roof.
     * Ocean/plains columns keep the nominal section; mountains stretch only
     * the part above Y=0. Custom non-overworld height ranges opt out upstream.
     */
    public double upperSectionScale(Climate.TargetPoint sample) {
        double regionalTop=quartY*4.0+128.0*sample.depth()/Climate.quantizeCoord(1F);
        return Math.max(1.0,Math.min(5.0,regionalTop/64.0));
    }

    /** Continuous regional terrain proxy; does not load neighboring chunks. */
    public double regionalSurfaceY(int x,int z) {
        return quartY*4.0+128.0*sample(x,z).depth()/Climate.quantizeCoord(1F);
    }

    public Climate.TargetPoint sample(int blockX, int blockZ) {
        int qx = QuartPos.fromBlock(blockX), qz = QuartPos.fromBlock(blockZ);
        // floorMod also keeps the interpolation continuous across negative coordinates.
        double tx = (Math.floorMod(blockX, 4) + .5) / 4.0;
        double tz = (Math.floorMod(blockZ, 4) + .5) / 4.0;
        Climate.TargetPoint a = corner(qx, qz), b = corner(qx + 1, qz);
        Climate.TargetPoint c = corner(qx, qz + 1), d = corner(qx + 1, qz + 1);
        return new Climate.TargetPoint(
                blend(a.temperature(), b.temperature(), c.temperature(), d.temperature(), tx, tz),
                blend(a.humidity(), b.humidity(), c.humidity(), d.humidity(), tx, tz),
                blend(a.continentalness(), b.continentalness(), c.continentalness(), d.continentalness(), tx, tz),
                blend(a.erosion(), b.erosion(), c.erosion(), d.erosion(), tx, tz),
                blend(a.depth(), b.depth(), c.depth(), d.depth(), tx, tz),
                blend(a.weirdness(), b.weirdness(), c.weirdness(), d.weirdness(), tx, tz)
        );
    }

    private Climate.TargetPoint corner(int qx, int qz) {
        long key = ((long) qx << 32) ^ (qz & 0xffffffffL);
        Climate.TargetPoint point=lattice.getAndMoveToLast(key);
        if(point==null){
            point=sampler.sample(qx,quartY,qz);lattice.putAndMoveToLast(key,point);
            if(lattice.size()>2048)lattice.removeFirst();
        }
        return point;
    }

    private static long blend(long a, long b, long c, long d, double tx, double tz) {
        double north = a + tx * ((double) b - a);
        double south = c + tx * ((double) d - c);
        return Math.round(north + tz * (south - north));
    }
}
