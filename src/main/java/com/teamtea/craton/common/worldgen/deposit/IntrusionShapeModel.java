package com.teamtea.craton.common.worldgen.deposit;

/** Approved normalized primary shapes with candidate-specific diorite dikes.
 * Geometry only: no world-height extension, detached satellites or extra contact noise.
 * Candidate parameters are immutable; a Column is reused by one generation context.
 */
public class IntrusionShapeModel {
    public static final int GRANITE=0,GABBRO=1,DIORITE=2,PEGMATITE=3;
    private final int kind;
    private final double[] phase;
    private final double angle,cos,sin,thick,skew,pegThickness;
    private final boolean branch;
    private final Dike[] dikes;
    public record Dike(double x,double y,double z,double angle,double cos,double sin,
                       double length,double embed,double halfWidth,double halfThickness,double slope){}

    public IntrusionShapeModel(int kind,long seed,boolean branch,double pegThickness){
        this(kind,seed,branch?3:0,1,pegThickness);
    }
    public IntrusionShapeModel(int kind,long seed,int maximumBranches,double branchScale,double pegThickness){
        this(kind,phases(seed),unit(seed^0xA971L)*.64-.32,
                .88+.24*unit(seed^0xB981L),unit(seed^0xC991L)*.24-.12,
                maximumBranches>0&&branchScale>0,pegThickness,
                kind==DIORITE?createDikes(seed,phases(seed),maximumBranches,branchScale):new Dike[0]);
    }
    /** Explicit parameters describe the primary shapes without procedural diorite dikes. */
    public IntrusionShapeModel(int kind,double[] phase,double angle,double thick,double skew,
                               boolean branch,double pegThickness){
        this(kind,phase,angle,thick,skew,branch,pegThickness,new Dike[0]);
    }
    private IntrusionShapeModel(int kind,double[] phase,double angle,double thick,double skew,
                                boolean branch,double pegThickness,Dike[] dikes){
        if(kind<0||kind>3||phase.length!=8||pegThickness<=0)throw new IllegalArgumentException("Invalid shape parameters");
        this.kind=kind;this.phase=phase.clone();this.angle=angle;this.cos=Math.cos(angle);this.sin=Math.sin(angle);
        this.thick=thick;this.skew=skew;this.branch=branch;this.pegThickness=pegThickness;this.dikes=dikes;
    }
    private static Dike[] createDikes(long seed,double[] phase,int maximum,double scale){
        maximum=Math.max(0,Math.min(24,maximum));
        if(maximum==0||scale<=0)return new Dike[0];
        int count=(int)(unit(seed^0x44494F52495445L)*(maximum+1));
        Dike[] result=new Dike[count];
        for(int i=0;i<count;i++){
            long s=DepositCandidateSampler.mix(seed^((i+1L)*0x9E3779B97F4A7C15L)^0x44494B45L);
            double y=-.65+.95*unit(s^11);
            double expansion=.42*clamp((-y-.30)/.65,0,1);
            double rx=.29+expansion+.04*Math.sin(4*y+phase[2]),rz=.25+.62*expansion;
            double angle=unit(s^21)*Math.PI*2,cos=Math.cos(angle),sin=Math.sin(angle);
            double fraction=.40+.22*unit(s^31);
            // The anchor lies strictly inside the stock's superellipse at this height.
            double x=.11*y+.06*Math.sin(2*y+phase[0])+fraction*rx*cos;
            double z=.07*Math.sin(1.7*y+phase[1])+fraction*rz*sin;
            double slope=(unit(s^41)<.2?-1:1)*(.12+unit(s^51));
            result[i]=new Dike(x,y,z,angle,cos,sin,.42+.65*unit(s^61),.10+.16*unit(s^71),
                    (.055+.065*unit(s^81))*scale,(.035+.045*unit(s^91))*scale,slope);
        }
        return result;
    }
    private double stockCenterX(double y){return .11*y+.06*Math.sin(2*y+phase[0]);}
    private double stockCenterZ(double y){return .07*Math.sin(1.7*y+phase[1]);}
    public double stockAxisAlong(double y){return cos*stockCenterX(y)-sin*stockCenterZ(y);}
    public double stockAxisAcross(double y){return sin*stockCenterX(y)+cos*stockCenterZ(y);}
    public int dikeCount(){return dikes.length;}
    public Dike dike(int index){return dikes[index];}
    private static double unit(long s){return DepositFieldSupport.rand01(DepositCandidateSampler.mix(s));}
    private static double[] phases(long seed){
        double[] p=new double[8];for(int i=0;i<8;i++)p[i]=unit(seed^(i*0x9E3779B97F4A7C15L))*Math.PI*2-Math.PI;return p;
    }
    public double phase(int i){return phase[i];}
    public double angle(){return angle;}
    public double thick(){return thick;}
    public double skew(){return skew;}
    private static double sq(double x){return x*x;}
    private static double fourth(double x){double s=x*x;return s*s;}
    private static double clamp(double x,double lo,double hi){return Math.max(lo,Math.min(hi,x));}
    private static double min4(double a,double b,double c,double d){return Math.min(Math.min(a,b),Math.min(c,d));}
    private double wave(double x,double z,int i){
        return .60*Math.sin(2.7*x+1.6*z+phase[i%8])
                +.27*Math.sin(5.1*x-3.3*z+phase[(i+1)%8])
                +.13*Math.sin(10.4*x+7.2*z+phase[(i+2)%8]);
    }
    public Column newColumn(){return new Column();}

    /** Primary fields and dike projections are prepared once per column; samples allocate nothing. */
    public class Column {
        private double x,z,footprint,roof,floor,reference,half,rootAcross;
        private double w,middle,branchHorizontal;
        private final double[] dikeHorizontal=new double[dikes.length];
        private final double[] dikeCenter=new double[dikes.length];
        private final double[] dikeHalf=new double[dikes.length];
        public void prepare(double inputX,double inputZ){
            x=cos*inputX+sin*inputZ;z=-sin*inputX+cos*inputZ;
            if(kind==GRANITE){
                double xx=x+.07*wave(x,z,1),zz=z+.05*wave(x,z,3);
                footprint=1-fourth(Math.abs(xx)/1.14)-fourth(Math.abs(zz)/.72);
                roof=.28+.16*Math.sin(2.2*x+phase[0])+.08*wave(x,z,2)-.13*z*z;
                floor=-.08-.43*Math.exp(-fourth((x-.35-skew)/.55))-.06*wave(x,z,4)+.09*z*z;
                rootAcross=.19-Math.abs(z-.06);
            }else if(kind==GABBRO){
                footprint=1-fourth(Math.abs(x)/1.18)-fourth(Math.abs(z)/.74)+.075*wave(x,z,0);
                reference=-.30+.29*(x*x+.8*z*z)+.045*wave(x,z,3);
                half=(.13+.055*(1-clamp(x*x,0,1)))*thick;
                roof=reference+half;floor=reference-half;
                rootAcross=.14-Math.abs(z+.04);
            }else if(kind==DIORITE){
                roof=.60+.045*Math.sin(2*x+1.3*z+phase[1]);floor=-1.4;
                for(int i=0;i<dikes.length;i++){
                    Dike d=dikes[i];double dx=x-d.x(),dz=z-d.z();
                    double along=dx*d.cos()+dz*d.sin(),across=-dx*d.sin()+dz*d.cos();
                    double t=clamp(along/d.length(),0,1);
                    dikeHorizontal[i]=Math.min(Math.min(along+d.embed(),d.length()-along),
                            d.halfWidth()*(1-.40*t)-Math.abs(across));
                    dikeCenter[i]=d.y()+d.slope()*along;
                    dikeHalf[i]=d.halfThickness()*(1-.50*t);
                }
            }else{
                w=z-.10*Math.sin(2*x+phase[2]);
                middle=.51*x+.035*wave(x,w,1);
                footprint=1-fourth(Math.abs(x)/1.06)-fourth(Math.abs(w)/.53);
                double swelling=Math.exp(-sq((x+.10)/.30)-sq(w/.40));
                half=(.036+.058*swelling)*thick*pegThickness;
                roof=middle+half;floor=middle-half;
                branchHorizontal=Math.min(.39-Math.abs(w),Math.min(x-.22,.91-x));
            }
        }
        private double stockRim(double y){
            double cx=stockCenterX(y);
            double cz=stockCenterZ(y);
            double expansion=.42*clamp((-y-.30)/.65,0,1);
            double rx=.29+expansion+.04*Math.sin(4*y+phase[2]),rz=.25+.62*expansion;
            return 1-fourth(Math.abs((x-cx)/rx))-fourth(Math.abs((z-cz)/rz));
        }
        public double sample(double y){
            if(kind==GRANITE){
                double body=Math.min(footprint,Math.min(roof-y,y-floor));
                double root=min4(.075-Math.abs(x-(.35+skew+.12*(y+.4))),rootAcross,-.30-y,y+1.4);
                return Math.max(body,root);
            }
            if(kind==GABBRO){
                double body=Math.min(footprint,half-Math.abs(y-reference));
                double root=min4(.065-Math.abs(x-(.13+.14*y)),rootAcross,-.29-y,y+1.4);
                return Math.max(body,root);
            }
            if(kind==DIORITE){
                double body=sampleStock(y);
                for(int i=0;i<dikes.length;i++){
                    if(dikeHorizontal[i]<=body)continue;
                    body=Math.max(body,Math.min(dikeHorizontal[i],dikeHalf[i]-Math.abs(y-dikeCenter[i])));
                }
                return body;
            }
            double body=Math.min(footprint,half-Math.abs(y-middle));
            double sheet=Math.min(.036*pegThickness-Math.abs(y-(.13*x+.16)),branchHorizontal);
            return branch?Math.max(body,sheet):body;
        }
        public double sampleStock(double y){return Math.min(stockRim(y),Math.min(roof-y,y+1.4));}
        public double primaryCenterY(){return kind==PEGMATITE?middle:(roof+floor)*.5;}
        public double upperBound(double y){
            if(kind==GRANITE){
                double body=Math.min(footprint,Math.min(roof-y,y-floor));
                return Math.max(body,min4(.075,rootAcross,-.30-y,y+1.4));
            }
            if(kind==GABBRO){
                double body=Math.min(footprint,half-Math.abs(y-reference));
                return Math.max(body,min4(.065,rootAcross,-.29-y,y+1.4));
            }
            if(kind==DIORITE){
                double body=Math.min(1,Math.min(roof-y,y+1.4));
                for(int i=0;i<dikes.length;i++)body=Math.max(body,
                        Math.min(dikeHorizontal[i],dikeHalf[i]-Math.abs(y-dikeCenter[i])));
                return body;
            }
            return branch?Math.max(Math.min(footprint,half-Math.abs(y-middle)),Math.min(.036*pegThickness,branchHorizontal))
                    :Math.min(footprint,half-Math.abs(y-middle));
        }
        public double minimumY(double cutoff){
            double low=floor+cutoff;
            if(kind==GRANITE||kind==GABBRO)low=Math.min(low,-1.4+cutoff);
            if(kind==DIORITE)for(int i=0;i<dikes.length;i++)if(dikeHorizontal[i]>=cutoff)
                low=Math.min(low,dikeCenter[i]-dikeHalf[i]+cutoff);
            if(branch&&kind==PEGMATITE)low=Math.min(low,.13*x+.16-.036*pegThickness+cutoff);
            return low;
        }
        public double maximumY(double cutoff){
            double high=roof-cutoff;
            if(kind==GRANITE||kind==GABBRO)high=Math.max(high,(kind==GRANITE?-.30:-.29)-cutoff);
            if(kind==DIORITE)for(int i=0;i<dikes.length;i++)if(dikeHorizontal[i]>=cutoff)
                high=Math.max(high,dikeCenter[i]+dikeHalf[i]-cutoff);
            if(branch&&kind==PEGMATITE)high=Math.max(high,.13*x+.16+.036*pegThickness-cutoff);
            return high;
        }
        public double radial(double y){return kind==DIORITE?Math.sqrt(Math.sqrt(Math.max(0,1-stockRim(y))))
                :Math.sqrt(Math.sqrt(Math.max(0,1-footprint)));}
        public double mineralY(double y){
            if(kind==GRANITE)return (y-(roof+floor)*.5)/Math.max(.01,(roof-floor)*.5);
            return kind==GABBRO?(y-reference)/.25:y;
        }
    }
}
