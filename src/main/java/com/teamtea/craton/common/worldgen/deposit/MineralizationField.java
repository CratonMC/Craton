package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.api.geology.deposit.Abundance;
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.rand01;

/** Bounded, jittered enrichment lenses clipped by each deposit's geological field.
 * Cells organize lenses, not blocks: no independent per-voxel random thinning.
 * Host rocks and alteration survive outside the enrichment lenses.
 */
public class MineralizationField {
    public enum Richness { POOR, NORMAL, RICH }
    private final Abundance config;
    private final long seed;
    private final double cos,sin,offsetX,offsetY,offsetZ,scale;
    private final Richness richness;
    private int lastX,lastZ,cellX,cellZ,lastCellY;
    private boolean horizontalReady,packetReady;
    private double px,pz,centerX,centerY,centerZ,rx,ry,rz,skew;

    public MineralizationField(Abundance config,long seed,long rotationSeed){
        this(config,seed,rand01(rotationSeed)*Math.PI*2);
    }
    public MineralizationField(Abundance config,long seed,double angle){
        config.requireValid();this.config=config;this.seed=seed;
        cos=Math.cos(angle);sin=Math.sin(angle);
        offsetX=rand01(seed^0xA1L)*1024;offsetY=rand01(seed^0xB2L)*1024;offsetZ=rand01(seed^0xC3L)*1024;
        double choice=rand01(seed^0x52494348L);
        richness=choice<config.poorChance()?Richness.POOR:choice>1-config.richChance()?Richness.RICH:Richness.NORMAL;
        scale=switch(richness){case POOR -> config.poorScale();case NORMAL -> config.normalScale();case RICH -> config.richScale();};
    }
    public double scale(){return scale;}
    public Richness richness(){return richness;}
    public boolean contains(int x,int y,int z){
        if(!config.enabled())return true;
        if(!horizontalReady||lastX!=x||lastZ!=z){
            px=(x+.5)*cos+(z+.5)*sin+offsetX;
            pz=-(x+.5)*sin+(z+.5)*cos+offsetZ;
            int nx=(int)Math.floor(px/config.cellX()),nz=config.cellZ()==0?0:(int)Math.floor(pz/config.cellZ());
            if(!horizontalReady||nx!=cellX||nz!=cellZ)packetReady=false;
            cellX=nx;cellZ=nz;lastX=x;lastZ=z;horizontalReady=true;
        }
        double py=y+.5+offsetY;
        int cy=config.cellY()==0?0:(int)Math.floor(py/config.cellY());
        if(!packetReady||lastCellY!=cy){
            long h=DepositCandidateSampler.mix(seed^((long)cellX*0x632BE59BD9B4E019L)
                    ^((long)cy*0x9E3779B97F4A7C15L)^((long)cellZ*0x94D049BB133111EBL));
            centerX=(cellX+.5+.07*(rand01(h)-.5))*config.cellX();
            centerY=(cy+.5+.07*(rand01(h^0x11L)-.5))*config.cellY();
            centerZ=(cellZ+.5+.07*(rand01(h^0x22L)-.5))*config.cellZ();
            // A local poor/rich modulation sits inside the deposit-wide richness class.
            double local=scale*(.78+.22*rand01(h^0x33L));
            rx=config.cellX()*.40*local;ry=config.cellY()*.40*local;rz=config.cellZ()*.40*local;
            skew=(rand01(h^0x44L)-.5)*.22;
            lastCellY=cy;packetReady=true;
        }
        double a=(px-centerX)/rx,b=config.cellY()==0?0:(py-centerY)/ry,c=config.cellZ()==0?0:(pz-centerZ)/rz;
        if(Math.abs(a)>1||Math.abs(b)>1||Math.abs(c)>1)return false;
        // Curved internal contour; never expands the conservative lens envelope.
        double contour=1-.12*(1-a*a)*(1-b*b)*(1-c*c)*(1+skew*a*b);
        return a*a+b*b+c*c<contour;
    }
}
