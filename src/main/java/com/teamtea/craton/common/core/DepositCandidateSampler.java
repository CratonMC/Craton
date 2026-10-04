package com.teamtea.craton.common.core;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic spatial index for large deposits.
 * Cells are lookup buckets only; no deposit is aligned to or clipped by a cell boundary.
 */
public final class DepositCandidateSampler {
    private static final long PLACEMENT_SALT=0x243F6A8885A308D3L;
    private static final long SHAPE_SALT=0x13198A2E03707344L;
    private static final long GRADE_SALT=0xA4093822299F31D0L;
    private static final long ORE_SALT=0x082EFA98EC4E6C89L;
    private static final long VERTICAL_SALT=0x452821E638D01377L;
    private static final long ROTATION_SALT=0xBE5466CF34E90C6CL;
    private static final long ALTERATION_SALT=0xC0AC29B7C97C50DDL;
    private static final long OCCUPANCY_SALT=0x3F84D5B5B5470917L;

    private DepositCandidateSampler(){}

    public record Candidate(
            int x,
            int z,
            long shapeSeed,
            long gradeSeed,
            long oreSeed,
            long verticalSeed,
            long rotationSeed,
            long alterationSeed,
            long occupancySeed
    ){}

    public static List<Candidate> query(PositionalRandomFactory rootRandom,DepositRandomSequences sequence,long depositSalt,int x,int z,
                                        int cellSize,double horizontalReach,int maxCandidatesPerCell){
        return new CellCache(rootRandom).query(sequence,depositSalt,x,z,cellSize,horizontalReach,maxCandidatesPerCell);
    }

    /** Scoped to one buildSurface call; caches unfiltered cells shared by neighboring columns. */
    public static final class CellCache {
        private static final int MAX_CELLS=8192;
        private final PositionalRandomFactory rootRandom;
        private final Map<DepositRandomSequences,PositionalRandomFactory> sequences=new EnumMap<>(DepositRandomSequences.class);
        private final Map<CellKey,List<Candidate>> cells=new LinkedHashMap<>(256,.75f,true){
            @Override protected boolean removeEldestEntry(Map.Entry<CellKey,List<Candidate>> eldest){
                return size()>MAX_CELLS;
            }
        };

        public CellCache(PositionalRandomFactory rootRandom){this.rootRandom=rootRandom;}

        public List<Candidate> query(DepositRandomSequences sequence,long depositSalt,int x,int z,
                                     int cellSize,double horizontalReach,int maxCandidatesPerCell){
            List<Candidate> result=new ArrayList<>();
            queryInto(sequence,depositSalt,x,z,cellSize,horizontalReach,maxCandidatesPerCell,result);
            return result;
        }

        /** Reuses a caller-owned temporary list; callers must consume it before the next query. */
        public void queryInto(DepositRandomSequences sequence,long depositSalt,int x,int z,
                              int cellSize,double horizontalReach,int maxCandidatesPerCell,List<Candidate> result){
        if(cellSize<=0||!Double.isFinite(horizontalReach)||horizontalReach<0||maxCandidatesPerCell<0)
            throw new IllegalArgumentException("Invalid deposit candidate query");
        result.clear();
        if(maxCandidatesPerCell==0) return;
        PositionalRandomFactory random=sequences.computeIfAbsent(sequence,s -> s.factory(rootRandom));
        long root=mix64(depositSalt);
        int cellX=Math.floorDiv(x,cellSize),cellZ=Math.floorDiv(z,cellSize);
        int cellRadius=Math.max(1,(int)Math.floor(horizontalReach/cellSize)+1);
        for(int cx=cellX-cellRadius;cx<=cellX+cellRadius;cx++)
            for(int cz=cellZ-cellRadius;cz<=cellZ+cellRadius;cz++){
                CellKey key=new CellKey(sequence,depositSalt,cx,cz,cellSize,maxCandidatesPerCell);
                List<Candidate> candidates=cells.get(key);
                if(candidates==null){
                    List<Candidate> generated=new ArrayList<>();
                    collectCell(random,root,cx,cz,cellSize,maxCandidatesPerCell,generated);
                    candidates=List.copyOf(generated);
                    cells.put(key,candidates);
                }
                for(Candidate candidate:candidates)
                    if(Math.hypot(x+.5-candidate.x(),z+.5-candidate.z())<=horizontalReach)
                        result.add(candidate);
            }
        }
    }

    private record CellKey(DepositRandomSequences sequence,long depositSalt,int x,int z,int cellSize,int maxCandidates){}

    private static void collectCell(PositionalRandomFactory random,long root,int cellX,int cellZ,
                                    int cellSize,int maxCandidates,List<Candidate> output){
        RandomSource placement=random.at(saltedPos(cellX,cellZ,root^PLACEMENT_SALT));
        int count=placement.nextInt(maxCandidates+1);
        int baseX=cellX*cellSize,baseZ=cellZ*cellSize;
        for(int i=0;i<count;i++){
            int x=baseX+placement.nextInt(cellSize);
            int z=baseZ+placement.nextInt(cellSize);
            long instance=mix64(root^mix64(i+1L)^mix64((((long)cellX)<<32)^(cellZ&0xffffffffL)));
            output.add(new Candidate(
                    x,z,
                    seed(random,cellX,cellZ,instance^SHAPE_SALT),
                    seed(random,cellX,cellZ,instance^GRADE_SALT),
                    seed(random,cellX,cellZ,instance^ORE_SALT),
                    seed(random,cellX,cellZ,instance^VERTICAL_SALT),
                    seed(random,cellX,cellZ,instance^ROTATION_SALT),
                    seed(random,cellX,cellZ,instance^ALTERATION_SALT),
                    seed(random,cellX,cellZ,instance^OCCUPANCY_SALT)
            ));
        }
    }

    private static long seed(PositionalRandomFactory random,int cellX,int cellZ,long salt){
        return random.at(saltedPos(cellX,cellZ,salt)).nextLong();
    }

    public static long salt(String id){
        long h=0xcbf29ce484222325L;
        for(int i=0;i<id.length();i++){
            h^=id.charAt(i);
            h*=0x100000001b3L;
        }
        return mix64(h);
    }

    public static long mix(long value){
        return mix64(value);
    }

    private static BlockPos saltedPos(int cellX,int cellZ,long salt){
        int sx=(int)(salt^(salt>>>32));
        int sy=(int)(salt>>>17);
        int sz=(int)(Long.rotateLeft(salt,29)^(salt>>>11));
        return new BlockPos(cellX*7349+sx,sy,cellZ*9151+sz);
    }

    private static long mix64(long z){
        z=(z^(z>>>30))*0xbf58476d1ce4e5b9L;
        z=(z^(z>>>27))*0x94d049bb133111ebL;
        return z^(z>>>31);
    }
}
