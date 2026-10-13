package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Game-scale ore distribution, independent of placement frequency and host geometry. */
public record Abundance(double poorChance,double richChance,double poorScale,double normalScale,double richScale,
                        int cellX,int cellY,int cellZ) {
    public static final Abundance NONE=new Abundance(0,0,1,1,1,0,0,0);
    public static final Abundance BANDED=new Abundance(.30,.12,.64,.84,1,48,0,32);
    public static final Codec<Abundance> CODEC=RecordCodecBuilder.<Abundance>create(i -> i.group(
            Codec.doubleRange(0,1).fieldOf("poor_chance").forGetter(Abundance::poorChance),
            Codec.doubleRange(0,1).fieldOf("rich_chance").forGetter(Abundance::richChance),
            Codec.doubleRange(.1,1).fieldOf("poor_scale").forGetter(Abundance::poorScale),
            Codec.doubleRange(.1,1).fieldOf("normal_scale").forGetter(Abundance::normalScale),
            Codec.doubleRange(.1,1).fieldOf("rich_scale").forGetter(Abundance::richScale),
            Codec.intRange(0,128).fieldOf("cell_x").forGetter(Abundance::cellX),
            Codec.intRange(0,128).fieldOf("cell_y").forGetter(Abundance::cellY),
            Codec.intRange(0,128).fieldOf("cell_z").forGetter(Abundance::cellZ)
    ).apply(i,Abundance::new)).flatXmap(Abundance::validate,Abundance::validate);
    private static DataResult<Abundance> validate(Abundance value){
        try{value.requireValid();return DataResult.success(value);}
        catch(IllegalArgumentException error){return DataResult.error(error::getMessage);}
    }
    public void requireValid(){
        if(!Double.isFinite(poorChance)||!Double.isFinite(richChance)||poorChance<0||richChance<0||poorChance+richChance>1
                ||!Double.isFinite(poorScale)||!Double.isFinite(normalScale)||!Double.isFinite(richScale)
                ||poorScale<.1||poorScale>normalScale||normalScale>richScale||richScale>1)
            throw new IllegalArgumentException("Invalid abundance probabilities or scales");
        boolean disabled=cellX==0&&cellY==0&&cellZ==0;
        if(!disabled&&(cellX<24||(cellY!=0&&cellY<24)||(cellZ!=0&&cellZ<24)||(cellY==0&&cellZ==0)||cellX>128||cellY>128||cellZ>128))
            throw new IllegalArgumentException("Abundance cells must all be zero, or X in [24,128]; Y or Z may be zero for a layer/vein projection, with at least two active axes");
    }
    public boolean enabled(){return cellX>0;}
    public static Abundance forType(net.minecraft.resources.Identifier type){
        if(type.equals(DepositTypes.KIMBERLITE))return NONE;
        if(type.equals(DepositTypes.COAL))return new Abundance(.25,.15,.67,.86,1,32,0,24);
        if(type.equals(DepositTypes.COPPER)||type.equals(DepositTypes.SHALE_PB_ZN)||type.equals(DepositTypes.URANIUM))
            return new Abundance(.35,.10,.60,.82,1,32,0,24);
        if(type.equals(DepositTypes.VMS))return new Abundance(.25,.15,.65,.86,1,32,24,28);
        if(type.equals(DepositTypes.VEIN)||type.equals(DepositTypes.EPITHERMAL)||type.equals(DepositTypes.DEEP_REDSTONE))
            return new Abundance(.35,.10,.62,.84,1,32,32,0);
        if(type.equals(DepositTypes.WEATHERING)||type.equals(DepositTypes.PLACER)||type.equals(DepositTypes.PEAT))
            return new Abundance(.30,.12,.65,.85,1,32,0,32);
        return new Abundance(.35,.10,.60,.82,1,24,24,24);
    }
}
