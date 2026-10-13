package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import com.teamtea.craton.api.geology.deposit.IntrusionDeposit;
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

/** Finite, dipping pegmatite dikes. X is half strike length, Y half down-dip extent,
 * Z half normal thickness. No stratigraphic boundary controls the dike position.
 * Mutable column projections belong to one generation context.
 */
public class PreparedPegmatite {
    private record Dike(double x,double y,double z,double cos,double sin,double dipCos,double dipSin,
                        double length,double extent,double thickness,double bend,double phase){}
    private final int minY;
    private final double cx,cy,cz,length,extent,thickness,cos,sin,dip,reach;
    private final boolean attached;
    private final Dike[] dikes;
    private final double[] along,across,curve;
    private boolean initialized;
    private int lastX,lastZ;
    private double wallFraction;

    public PreparedPegmatite(DepositCandidateSampler.Candidate c,int minY,FieldDeposit.Shape shape){
        this(c,minY,shape,Double.NaN,angle(c.rotationSeed()));
    }
    private PreparedPegmatite(DepositCandidateSampler.Candidate c,int minY,FieldDeposit.Shape shape,
                             double anchorY,double worldAngle){
        this.minY=minY;attached=Double.isFinite(anchorY);
        cx=c.x()+(attached?.5:0);cz=c.z()+(attached?.5:0);
        cy=attached?anchorY:shape.height(minY,c.verticalSeed());
        length=shape.x(c.shapeSeed());extent=shape.y(c.shapeSeed()^0x50454759L);
        thickness=shape.z(c.shapeSeed()^0x5045475AL);
        cos=Math.cos(worldAngle);sin=Math.sin(worldAngle);
        dip=Math.toRadians(25+60*rand01(c.rotationSeed()^0xD1FL));
        int maximum=Math.min(24,shape.satelliteCount());
        int branches=shape.satelliteScale()<=0?0:(int)(rand01(c.shapeSeed()^0x4252414EL)*(maximum+1));
        dikes=new Dike[branches+1];
        double bend=Math.min(2,thickness*.65)*(shape.broadNoise()+shape.detailNoise());
        double phase=rand01(c.shapeSeed()^0x50484153L)*Math.PI*2;
        dikes[0]=new Dike(0,0,0,cos,sin,Math.cos(dip),Math.sin(dip),length,extent,thickness,bend,phase);
        double bound=Math.hypot(length,extent)+thickness+bend+4;
        for(int i=0;i<branches;i++){
            long seed=DepositCandidateSampler.mix(c.shapeSeed()^((i+1L)*0x9E3779B97F4A7C15L));
            double u=(rand01(seed)-.5)*length*1.2;
            double localCurve=bend*(Math.sin(u/length*Math.PI+phase)-Math.sin(phase));
            // Junctions lie on the primary dike's curved center plane.
            double bx=u*cos-localCurve*Math.sin(dip)*sin;
            double bz=u*sin+localCurve*Math.sin(dip)*cos;
            double by=localCurve*Math.cos(dip);
            double angle=worldAngle+(rand01(seed^0x11L)<.5?-1:1)*(.25+.65*rand01(seed^0x22L));
            double branchDip=Math.toRadians(25+60*rand01(seed^0x33L));
            double factor=(.30+.35*rand01(seed^0x44L))*Math.min(1,shape.satelliteScale());
            Dike d=new Dike(bx,by,bz,Math.cos(angle),Math.sin(angle),Math.cos(branchDip),Math.sin(branchDip),
                    length*factor,extent*(.45+.25*rand01(seed^0x55L)),thickness*(.50+.25*rand01(seed^0x66L)),
                    bend*.6,rand01(seed^0x77L)*Math.PI*2);
            dikes[i+1]=d;
            bound=Math.max(bound,Math.hypot(bx,bz)+Math.hypot(d.length(),d.extent())+d.thickness()+d.bend()*2+4);
        }
        reach=bound;along=new double[dikes.length];across=new double[dikes.length];curve=new double[dikes.length];
    }
    public record Attachment(DepositCandidateSampler.Candidate candidate,PreparedPegmatite geometry,
                             int anchorY,double parentScore){}

    /** Derive the pose from one parent, independently of column order and terrain sampling. */
    public static Attachment attach(IntrusionDeposit.Prepared parent,FieldDeposit.Shape shape,long salt,int index){
        long seed=DepositCandidateSampler.mix(parent.candidate.shapeSeed()^salt^((index+1L)*0x9E3779B97F4A7C15L));
        double u=(index%2==0?1:-1)*(.65+.25*rand01(seed^0x11L));
        double v=(rand01(seed^0x21L)-.5)*.30;
        int x=(int)Math.floor(parent.candidate.x()+u*parent.rx*parent.rotCos-v*parent.rz*parent.rotSin);
        int z=(int)Math.floor(parent.candidate.z()+u*parent.rx*parent.rotSin+v*parent.rz*parent.rotCos);
        IntrusionShapeModel.Column parentColumn=parent.geometry.model().newColumn();
        double dx=x+.5-parent.candidate.x(),dz=z+.5-parent.candidate.z();
        parentColumn.prepare((dx*parent.rotCos+dz*parent.rotSin)/parent.rx,
                (-dx*parent.rotSin+dz*parent.rotCos)/parent.rz);
        int anchor=parent.originMinY;double best=0;
        // Search actual block centers, retaining the best interior point on this line.
        for(int i=0;i<=64;i++){
            int y=(int)Math.floor(parent.cy+parent.ry*(-.65+i*1.25/64)-.5);
            if(y<=parent.originMinY)continue;
            double score=parentColumn.sample((y+.5-parent.cy)/parent.ry);
            if(score>best){best=score;anchor=y;}
        }
        if(best<=0)return null;
        var c=new DepositCandidateSampler.Candidate(x,z,
                DepositCandidateSampler.mix(seed^0x31L),DepositCandidateSampler.mix(seed^0x41L),
                DepositCandidateSampler.mix(seed^0x51L),DepositCandidateSampler.mix(seed^0x61L),
                DepositCandidateSampler.mix(seed^0x71L),DepositCandidateSampler.mix(seed^0x81L),
                DepositCandidateSampler.mix(seed^0x91L));
        double direction=parent.angle+(rand01(seed^0xA1L)-.5)*2.2;
        PreparedPegmatite geometry=new PreparedPegmatite(c,parent.originMinY,shape,anchor+.5,direction);
        return new Attachment(c,geometry,anchor,best);
    }
    public static double requiredReach(FieldDeposit.Shape shape){
        return 2.2*shape.radiusXMax()+1.5*shape.radiusYMax()+4*shape.radiusZMax()+12;
    }
    public double worldAngle(){return Math.atan2(sin,cos);}
    public double dipDegrees(){return Math.toDegrees(dip);}
    public int branchCount(){return dikes.length-1;}
    public double projectedHalfLength(){return reach;}
    public double projectedHalfWidth(){return extent*Math.cos(dip)+thickness+4;}
    public int minY(){return minY;}
    public double centerY(){return cy;}
    public double surfaceDistance(double score){return score*thickness;}
    public double wallFraction(){return wallFraction;}
    public boolean attached(){return attached;}
    public boolean affectsColumn(int x,int z){return Math.hypot(x+.5-cx,z+.5-cz)<=reach;}
    public double score(int x,int y,int z){
        if(!initialized||x!=lastX||z!=lastZ){
            double dx=x+.5-cx,dz=z+.5-cz;
            for(int i=0;i<dikes.length;i++){
                Dike d=dikes[i];double a=dx-d.x(),b=dz-d.z();
                along[i]=a*d.cos()+b*d.sin();across[i]=-a*d.sin()+b*d.cos();
                curve[i]=d.bend()*(Math.sin(along[i]/d.length()*Math.PI+d.phase())-Math.sin(d.phase()));
            }
            initialized=true;lastX=x;lastZ=z;
        }
        double best=-Double.MAX_VALUE;
        for(int i=0;i<dikes.length;i++){
            Dike d=dikes[i];double u=along[i]/d.length();
            if(Math.abs(u)>1.25)continue;
            double dy=y+.5-cy-d.y();
            double down=across[i]*d.dipCos()+dy*d.dipSin();
            double normal=-across[i]*d.dipSin()+dy*d.dipCos()-curve[i];
            double v=down/d.extent(),envelope=1-u*u-v*v;
            double taper=GeologicalNoise.clamp(envelope/.20,0,1);
            double half=d.thickness()*(.35+.65*Math.max(0,envelope))*taper;
            double value=Math.min(d.thickness()*envelope,half-Math.abs(normal))/thickness;
            if(value>best){best=value;wallFraction=Math.abs(normal)/Math.max(.01,half);}
        }
        return best;
    }
}
