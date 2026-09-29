package com.teamtea.craton.common.core;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic spatial index for large deposits.
 * Cells are only lookup buckets: deposit geometry is free to cross cell boundaries.
 */
public final class DepositCandidateSampler {
    private static final long PLACEMENT_SALT=0x243F6A8885A308D3L;
    private static final long SHAPE_SALT=0x13198A2E03707344L;

    private DepositCandidateSampler(){}

    public record Candidate(int x,int z,long shapeSeed){}

    public static List<Candidate> query(PositionalRandomFactory random,long depositSalt,int x,int z,
                                        int cellSize,double horizontalReach,int maxCandidatesPerCell){
        int cellX=Math.floorDiv(x,cellSize),cellZ=Math.floorDiv(z,cellSize);
        int cellRadius=Math.max(1,(int)Math.floor(horizontalReach/cellSize)+1);
        List<Candidate> result=new ArrayList<>((cellRadius*2+1)*(cellRadius*2+1));
        for(int cx=cellX-cellRadius;cx<=cellX+cellRadius;cx++)
            for(int cz=cellZ-cellRadius;cz<=cellZ+cellRadius;cz++)
                collectCell(random,depositSalt,cx,cz,cellSize,maxCandidatesPerCell,result);
        return result;
    }

    private static void collectCell(PositionalRandomFactory random,long depositSalt,int cellX,int cellZ,
                                    int cellSize,int maxCandidates,List<Candidate> output){
        RandomSource placement=random.at(saltedPos(cellX,cellZ,depositSalt^PLACEMENT_SALT));
        int count=placement.nextInt(maxCandidates+1);
        int baseX=cellX*cellSize,baseZ=cellZ*cellSize;
        for(int i=0;i<count;i++){
            int x=baseX+placement.nextInt(cellSize);
            int z=baseZ+placement.nextInt(cellSize);
            RandomSource shape=random.at(saltedPos(cellX,cellZ,depositSalt^SHAPE_SALT^mix64(i+1L)));
            output.add(new Candidate(x,z,shape.nextLong()));
        }
    }

    /** Stable salt for registry identifiers or any other stable deposit id. */
    public static long salt(String id){
        long h=0xcbf29ce484222325L;
        for(int i=0;i<id.length();i++){
            h^=id.charAt(i);
            h*=0x100000001b3L;
        }
        return mix64(h);
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
