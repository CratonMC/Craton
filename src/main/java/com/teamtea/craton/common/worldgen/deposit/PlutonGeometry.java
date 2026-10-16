package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;
import com.teamtea.craton.common.worldgen.surface.WorldSetter;

import com.teamtea.craton.api.geology.deposit.DepositTypes;
import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import com.teamtea.craton.api.geology.deposit.IntrusionDeposit;

/** Integrates approved primary shapes and procedural diorite dikes into world generation.
 * A bounded scalar field roughens contacts without resampling warped geometry.
 * WorldSetter still owns bedrock protection.
 */
public class PlutonGeometry {
    private final IntrusionShapeModel model;
    private final IntrusionShapeModel.Column column;
    private boolean active;
    private final double broadAmplitude,detailAmplitude,amplitude;
    private final GeologicalNoise.CachedFbm broad,detail;
    private int columnX,columnZ;
    public PlutonGeometry(IntrusionDeposit.Prepared p,FieldDeposit.Shape shape){
        int kind=p.deposit.getType().equals(DepositTypes.GRANITE)?IntrusionShapeModel.GRANITE:
                p.deposit.getType().equals(DepositTypes.GABBRO)?IntrusionShapeModel.GABBRO:IntrusionShapeModel.DIORITE;
        model=new IntrusionShapeModel(kind,p.candidate.shapeSeed(),shape.satelliteCount(),shape.satelliteScale(),1);
        column=model.newColumn();
        broadAmplitude=Math.min(.09,shape.broadNoise()*.4);
        detailAmplitude=Math.min(.025,shape.detailNoise()*.3);
        amplitude=broadAmplitude+detailAmplitude;
        broad=new GeologicalNoise.CachedFbm(p.candidate.shapeSeed()^0x6A09E667F3BCC909L,.035,1);
        detail=new GeologicalNoise.CachedFbm(p.candidate.shapeSeed()^0xBB67AE8584CAA73BL,.11,1);
    }
    public void prepareColumn(DepositGenerationContext ctx,IntrusionDeposit.Prepared p,double cutoff){
        p.rotateColumn(ctx.x(),ctx.z());p.scoreCutoff=cutoff;
        columnX=ctx.x();columnZ=ctx.z();
        active=Math.hypot(p.along,p.across)<=p.deposit.contactReach(Math.max(0,-cutoff*p.minRadius))+1E-8;
        if(!active){p.minimumY=Double.POSITIVE_INFINITY;p.maximumY=Double.NEGATIVE_INFINITY;return;}
        column.prepare(p.along/p.rx,p.across/p.rz);
        p.minimumY=p.cy+p.ry*column.minimumY(cutoff-amplitude);
        p.maximumY=p.cy+p.ry*column.maximumY(cutoff-amplitude);
        p.radial=column.radial(0);
    }
    public double upperBound(IntrusionDeposit.Prepared p,int y){return active?column.upperBound((y+.5-p.cy)/p.ry)+amplitude:Double.NEGATIVE_INFINITY;}
    public double normalizedY(IntrusionDeposit.Prepared p,int y){return column.mineralY((y+.5-p.cy)/p.ry);}
    public double radial(IntrusionDeposit.Prepared p,int y){return column.radial((y+.5-p.cy)/p.ry);}
    public double sample(IntrusionDeposit.Prepared p,int x,int y,int z){
        double raw=sampleBase(p,y);
        // Compact support preserves the interior and avoids noise work away from contacts.
        if(amplitude==0||Math.abs(raw)>=3*amplitude) return raw;
        double weight=1-GeologicalNoise.smoothstep(amplitude,3*amplitude,Math.abs(raw));
        return raw+weight*(broad.sample(columnX,y,columnZ)*broadAmplitude+detail.sample(columnX,y,columnZ)*detailAmplitude);
    }
    public double sampleBase(IntrusionDeposit.Prepared p,int y){
        if(!active)return Double.NEGATIVE_INFINITY;
        double normalized=(y+.5-p.cy)/p.ry;
        column.prepareHeight(y,normalized);
        return column.sample(normalized);
    }
    public double maximumDisplacement(){return amplitude;}
    public double sampleStock(IntrusionDeposit.Prepared p,int y){return active?column.sampleStock((y+.5-p.cy)/p.ry):Double.NEGATIVE_INFINITY;}
    public IntrusionShapeModel model(){return model;}
}
