package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

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
public class DepositCandidateSampler {
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
    public static class CellCache {
        private static final int MAX_CELLS=8192;
        private final PositionalRandomFactory rootRandom;
        private final Map<DepositRandomSequences,PositionalRandomFactory> sequences=new EnumMap<>(DepositRandomSequences.class);
        private final Map<CellKey,List<Candidate>> cells=new LinkedHashMap<>(256,.75f,true){
            @Override protected boolean removeEldestEntry(Map.Entry<CellKey,List<Candidate>> eldest){
                return size()>MAX_CELLS;
            }
        };

        private final Map<QueryKey,Neighborhood> neighborhoods=new LinkedHashMap<>(16,.75f,true){
            @Override protected boolean removeEldestEntry(Map.Entry<QueryKey,Neighborhood> eldest){return size()>64;}
        };
        private int preparedNeighborhoods;
        private static class Neighborhood {
            int cellX,cellZ,radius;
            boolean ready;
            List<Candidate> candidates=List.of();
        }
        private record QueryKey(DepositRandomSequences sequence,long salt,int cellSize,int maxCandidates){}

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
            if(cellSize<=0||!Double.isFinite(horizontalReach)||horizontalReach<0
                    ||maxCandidatesPerCell<0||maxCandidatesPerCell>8||horizontalReach/cellSize>8)
                throw new IllegalArgumentException("Deposit candidate query requires finite reach <= eight cells and 0..8 candidates");
            result.clear();
            if(maxCandidatesPerCell==0) return;
            PositionalRandomFactory random=sequences.computeIfAbsent(sequence,s -> s.factory(rootRandom));
            long root=GeologicalNoise.mix(depositSalt);
            int cellX=Math.floorDiv(x,cellSize),cellZ=Math.floorDiv(z,cellSize);
            int cellRadius=Math.max(1,(int)Math.floor(horizontalReach/cellSize)+1);
            QueryKey queryKey=new QueryKey(sequence,depositSalt,cellSize,maxCandidatesPerCell);
            Neighborhood neighborhood=neighborhoods.get(queryKey);
            if(neighborhood==null){
                if(neighborhoods.size()+preparedNeighborhoods>=64){
                    // A cyclic scan of >64 definitions must not rebuild/evict every pooled neighborhood.
                    // Keep admitted working sets and use the original bounded cell scan for the rest.
                    for(int cx=cellX-cellRadius;cx<=cellX+cellRadius;cx++)
                        for(int cz=cellZ-cellRadius;cz<=cellZ+cellRadius;cz++)
                            addNearby(cell(random,root,queryKey,cx,cz),x,z,horizontalReach,result);
                    return;
                }
                neighborhood=new Neighborhood();neighborhoods.put(queryKey,neighborhood);
            }
            if(!neighborhood.ready||neighborhood.cellX!=cellX||neighborhood.cellZ!=cellZ||neighborhood.radius!=cellRadius){
                List<Candidate> pool=new ArrayList<>();
                for(int cx=cellX-cellRadius;cx<=cellX+cellRadius;cx++)
                    for(int cz=cellZ-cellRadius;cz<=cellZ+cellRadius;cz++){
                        pool.addAll(cell(random,root,queryKey,cx,cz));
                    }
                neighborhood.cellX=cellX;neighborhood.cellZ=cellZ;neighborhood.radius=cellRadius;
                neighborhood.candidates=List.copyOf(pool);neighborhood.ready=true;
            }
            // Preserve cell/candidate ordering and the exact original radial predicate.
            addNearby(neighborhood.candidates,x,z,horizontalReach,result);
        }

        /** Enumerate one placement bucket for regional surveys, using the generation seeds and frequency filter. */
        public List<Candidate> queryCell(DepositRandomSequences sequence,long salt,int cellX,int cellZ,
                                         int cellSize,int maxCandidates,double frequency){
            if(cellSize<16||cellSize>4096||maxCandidates<0||maxCandidates>8
                    ||!Double.isFinite(frequency)||frequency<0||frequency>1)
                throw new IllegalArgumentException("Invalid survey cell query");
            if(maxCandidates==0||frequency==0)return List.of();
            PositionalRandomFactory random=sequences.computeIfAbsent(sequence,s -> s.factory(rootRandom));
            long root=GeologicalNoise.mix(salt);
            List<Candidate> selected=new ArrayList<>();
            for(Candidate candidate:cell(random,root,new QueryKey(sequence,salt,cellSize,maxCandidates),cellX,cellZ))
                if(frequency==1||DepositFieldSupport.rand01(candidate.shapeSeed()^salt)<frequency)selected.add(candidate);
            return List.copyOf(selected);
        }

        /** Compile fixed query parameters once; each handle belongs to this chunk context. */
        public PreparedQuery prepare(DepositRandomSequences sequence,long salt,int cellSize,double reach,int maxCandidates,double frequency){
            if(cellSize<=0||!Double.isFinite(reach)||reach<0||reach/cellSize>8||maxCandidates<0||maxCandidates>8
                    ||!Double.isFinite(frequency)||frequency<0||frequency>1)
                throw new IllegalArgumentException("Invalid prepared deposit query");
            Neighborhood pool=null;
            if(maxCandidates>0&&frequency>0&&neighborhoods.size()+preparedNeighborhoods<64){pool=new Neighborhood();preparedNeighborhoods++;}
            return new PreparedQuery(new QueryKey(sequence,salt,cellSize,maxCandidates),reach,frequency,pool);
        }

        public class PreparedQuery {
            private final QueryKey key;
            private final double reach,frequency;
            private final int radius;
            private final long root;
            private final PositionalRandomFactory random;
            private final Neighborhood neighborhood;
            private PreparedQuery(QueryKey key,double reach,double frequency,Neighborhood neighborhood){
                this.key=key;this.reach=reach;this.frequency=frequency;this.neighborhood=neighborhood;
                radius=Math.max(1,(int)Math.floor(reach/key.cellSize())+1);root=GeologicalNoise.mix(key.salt());
                random=sequences.computeIfAbsent(key.sequence(),sequence -> sequence.factory(rootRandom));
            }
            private boolean accepts(Candidate candidate){
                return frequency==1||DepositFieldSupport.rand01(candidate.shapeSeed()^key.salt())<frequency;
            }
            public void queryInto(int x,int z,List<Candidate> result){
                result.clear();
                if(key.maxCandidates()==0||frequency==0) return;
                int cx=Math.floorDiv(x,key.cellSize()),cz=Math.floorDiv(z,key.cellSize());
                if(neighborhood==null){
                    for(int qx=cx-radius;qx<=cx+radius;qx++) for(int qz=cz-radius;qz<=cz+radius;qz++)
                        for(Candidate candidate:cell(random,root,key,qx,qz)){
                            if(!accepts(candidate)) continue;
                            double dx=x+.5-candidate.x(),dz=z+.5-candidate.z();
                            if(Math.abs(dx)<=reach&&Math.abs(dz)<=reach&&Math.hypot(dx,dz)<=reach) result.add(candidate);
                        }
                    return;
                }
                if(!neighborhood.ready||neighborhood.cellX!=cx||neighborhood.cellZ!=cz){
                    List<Candidate> pool=new ArrayList<>();
                    for(int qx=cx-radius;qx<=cx+radius;qx++) for(int qz=cz-radius;qz<=cz+radius;qz++)
                        for(Candidate candidate:cell(random,root,key,qx,qz)) if(accepts(candidate)) pool.add(candidate);
                    neighborhood.cellX=cx;neighborhood.cellZ=cz;neighborhood.radius=radius;
                    neighborhood.candidates=List.copyOf(pool);neighborhood.ready=true;
                }
                addNearby(neighborhood.candidates,x,z,reach,result);
            }
        }

        private List<Candidate> cell(PositionalRandomFactory random,long root,QueryKey query,int cx,int cz){
            CellKey key=new CellKey(query.sequence(),query.salt(),cx,cz,query.cellSize(),query.maxCandidates());
            List<Candidate> candidates=cells.get(key);
            if(candidates==null){
                List<Candidate> generated=new ArrayList<>();
                collectCell(random,root,cx,cz,query.cellSize(),query.maxCandidates(),generated);
                candidates=List.copyOf(generated);cells.put(key,candidates);
            }
            return candidates;
        }
        private static void addNearby(List<Candidate> candidates,int x,int z,double reach,List<Candidate> result){
            for(int i=0,size=candidates.size();i<size;i++){
                Candidate candidate=candidates.get(i);
                double dx=x+.5-candidate.x(),dz=z+.5-candidate.z();
                if(Math.abs(dx)<=reach&&Math.abs(dz)<=reach&&Math.hypot(dx,dz)<=reach) result.add(candidate);
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
            long instance=GeologicalNoise.mix(root^GeologicalNoise.mix(i+1L)^GeologicalNoise.mix((((long)cellX)<<32)^(cellZ&0xffffffffL)));
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
        return GeologicalNoise.mix(h);
    }

    public static long mix(long value){
        return GeologicalNoise.mix(value);
    }

    private static BlockPos saltedPos(int cellX,int cellZ,long salt){
        int sx=(int)(salt^(salt>>>32));
        int sy=(int)(salt>>>17);
        int sz=(int)(Long.rotateLeft(salt,29)^(salt>>>11));
        return new BlockPos(cellX*7349+sx,sy,cellZ*9151+sz);
    }

}
