package com.teamtea.craton.common.survey;

import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A vanilla-sized raster of approximate candidate footprints, independent of world chunks. */
public class SurveyMapCanvas {
    public static final int SIZE=128;
    public enum Group { IRON, COAL, COPPER, LEAD_ZINC, URANIUM, TIN_TUNGSTEN, GOLD, NICKEL, GEM, BAUXITE, REDSTONE, PEGMATITE, SKARN }
    public record Mark(double x,double z,double radiusX,double radiusZ,double angle,Group group,long seed) {
        public Mark {
            if(!Double.isFinite(x)||!Double.isFinite(z)||!Double.isFinite(radiusX)||!Double.isFinite(radiusZ)
                    ||!Double.isFinite(angle)||radiusX<=0||radiusZ<=0||group==null)
                throw new IllegalArgumentException("Invalid survey footprint");
        }
    }
    private record Footprint(double x,double z,double rx,double rz,double cos,double sin,Group group,long seed,
                             int minX,int maxX,int minZ,int maxZ){}
    private final int centerX,centerZ,blocksPerPixel;
    private final byte[] colors=new byte[SIZE*SIZE];
    private final byte[][] palette;
    private final List<Footprint> footprints=new ArrayList<>();
    private final double[] strength=new double[SIZE];
    private final long[] tie=new long[SIZE];
    private int row;

    public SurveyMapCanvas(int centerX,int centerZ,int scale,byte background,byte[][] palette){
        if(scale<0||scale>4||palette.length!=Group.values().length)throw new IllegalArgumentException("Invalid survey raster");
        this.centerX=centerX;this.centerZ=centerZ;blocksPerPixel=1<<scale;
        this.palette=new byte[palette.length][];
        for(int i=0;i<palette.length;i++){
            if(palette[i].length!=3)throw new IllegalArgumentException("Expected three shades per group");
            this.palette[i]=palette[i].clone();
        }
        Arrays.fill(colors,background);
    }
    public int minimumX(){return centerX-64*blocksPerPixel;}
    public int minimumZ(){return centerZ-64*blocksPerPixel;}
    public int maximumX(){return minimumX()+SIZE*blocksPerPixel-1;}
    public int maximumZ(){return minimumZ()+SIZE*blocksPerPixel-1;}
    public int blocksPerPixel(){return blocksPerPixel;}
    public int markCount(){return footprints.size();}
    public java.util.Map<Group,Integer> markCounts(){
        var counts=new java.util.EnumMap<Group,Integer>(Group.class);
        for(Footprint f:footprints)counts.merge(f.group(),1,Integer::sum);
        return java.util.Map.copyOf(counts);
    }
    public int finishedRows(){return row;}
    public byte[] colors(){return colors.clone();}
    public void add(Mark mark){
        if(row!=0)throw new IllegalStateException("Survey raster already started");
        double x=(mark.x()-minimumX())/blocksPerPixel,z=(mark.z()-minimumZ())/blocksPerPixel;
        double rx=Math.max(1.5,mark.radiusX()/blocksPerPixel),rz=Math.max(1.5,mark.radiusZ()/blocksPerPixel);
        double cos=Math.cos(mark.angle()),sin=Math.sin(mark.angle());
        double reachX=Math.hypot(rx*cos,rz*sin)*1.15+1,reachZ=Math.hypot(rx*sin,rz*cos)*1.15+1;
        int minX=Math.max(0,(int)Math.floor(x-reachX)),maxX=Math.min(SIZE-1,(int)Math.ceil(x+reachX));
        int minZ=Math.max(0,(int)Math.floor(z-reachZ)),maxZ=Math.min(SIZE-1,(int)Math.ceil(z+reachZ));
        if(minX>maxX||minZ>maxZ)return;
        if(footprints.size()>=2048)throw new IllegalStateException("Too many survey footprints");
        footprints.add(new Footprint(x,z,rx,rz,cos,sin,mark.group(),mark.seed(),minX,maxX,minZ,maxZ));
    }
    /** One bounded row is one scheduler unit. Strongest signal wins; ties are seed-stable. */
    public boolean drawRow(){
        if(row==SIZE)return true;
        Arrays.fill(strength,0);Arrays.fill(tie,0);
        for(Footprint f:footprints){
            if(row<f.minZ()||row>f.maxZ())continue;
            for(int x=f.minX();x<=f.maxX();x++){
                double dx=x+.5-f.x(),dz=row+.5-f.z();
                double u=(dx*f.cos()+dz*f.sin())/f.rx(),v=(-dx*f.sin()+dz*f.cos())/f.rz();
                double d=u*u+v*v;if(d>1.3)continue;
                double wx=minimumX()+(x+.5)*blocksPerPixel,wz=minimumZ()+(row+.5)*blocksPerPixel;
                double rough=GeologicalNoise.fbm2(wx,wz,f.seed(),.025,1)*.16;
                double value=f.group()==Group.SKARN?1-Math.abs(Math.sqrt(d)-.86)/.22+rough:1-d+rough;
                if(value<=0||value<strength[x]||(value==strength[x]&&Long.compareUnsigned(f.seed(),tie[x])<=0))continue;
                strength[x]=value;tie[x]=f.seed();
                int shade=value>.65?2:value>.28?1:0;
                colors[row*SIZE+x]=palette[f.group().ordinal()][shade];
            }
        }
        row++;return row==SIZE;
    }
}
