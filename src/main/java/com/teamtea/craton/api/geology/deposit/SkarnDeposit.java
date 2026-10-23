package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.IntrusiveDepositField.IntrusionResult;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class SkarnDeposit extends FieldDeposit {
    public static final MapCodec<SkarnDeposit> CODEC = RecordCodecBuilder.<SkarnDeposit>mapCodec(i -> i.group(
            Settings.CODEC.fieldOf("settings").forGetter(SkarnDeposit::settings),
            Codec.floatRange(0f, 1f).optionalFieldOf("marble_chance", .40f).forGetter(SkarnDeposit::marbleChance),
            Codec.floatRange(0f, 1f).optionalFieldOf("contact_min_grade", .62f).forGetter(SkarnDeposit::contactMinGrade),
            Codec.floatRange(0f, 1f).optionalFieldOf("marble_ore_min_grade", .53f).forGetter(SkarnDeposit::marbleOreMinGrade),
            Codec.floatRange(0f, 1f).optionalFieldOf("intrusion_0_ore_share", .50f).forGetter(SkarnDeposit::intrusion0OreShare),
            Codec.floatRange(0f, 1f).optionalFieldOf("intrusion_1_ore_share", .51f).forGetter(SkarnDeposit::intrusion1OreShare),
            Codec.floatRange(0f, 1f).optionalFieldOf("intrusion_2_ore_share", .58f).forGetter(SkarnDeposit::intrusion2OreShare)
    ).apply(i, SkarnDeposit::new)).flatXmap(SkarnDeposit::validate, SkarnDeposit::validate);
    private static com.mojang.serialization.DataResult<SkarnDeposit> validate(SkarnDeposit d) {
        return FieldDeposit.validate(d.settings(), 5, true).map(v -> d);
    }
    private final float marbleChance, contactMinGrade, marbleOreMinGrade;
    private final float intrusion0OreShare, intrusion1OreShare, intrusion2OreShare;
    public float marbleChance(){return marbleChance;}
    public float contactMinGrade(){return contactMinGrade;}
    public float marbleOreMinGrade(){return marbleOreMinGrade;}
    public float intrusion0OreShare(){return intrusion0OreShare;}
    public float intrusion1OreShare(){return intrusion1OreShare;}
    public float intrusion2OreShare(){return intrusion2OreShare;}

    public SkarnDeposit(Settings settings) {
        this(settings, .40f, .62f, .53f, .50f, .51f, .58f);
    }
    public SkarnDeposit(Settings settings, float marbleChance, float contactMinGrade, float marbleOreMinGrade,
                        float intrusion0OreShare, float intrusion1OreShare, float intrusion2OreShare) {
        super(DepositTypes.SKARN,settings,DepositRandomSequences.SKARN);
        this.marbleChance=marbleChance;
        this.contactMinGrade=contactMinGrade;
        this.marbleOreMinGrade=marbleOreMinGrade;
        this.intrusion0OreShare=intrusion0OreShare;
        this.intrusion1OreShare=intrusion1OreShare;
        this.intrusion2OreShare=intrusion2OreShare;
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public boolean matchesHost(BlockState state) {
        return hostMatches(state,settings().hostTags());
    }

    public BlockState placeContact(DepositGenerationContext ctx,BlockState originalHost,BlockState state,int y,IntrusionResult intrusion,long salt){
        if(!matchesHost(originalHost)||intrusion.candidate()==null) return state;
        if(rand01(intrusion.candidate().alterationSeed()^salt)>=settings().placement().frequency()) return state;
        double reach=settings().shape().y(intrusion.candidate().shapeSeed());
        if(intrusion.surfaceDistance()>0||intrusion.surfaceDistance()<-reach) return state;

        DepositCandidateSampler.Candidate c=intrusion.candidate();
        Shape config=settings().shape();
        double broad=GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.alterationSeed()^salt,.012,3);
        double detail=GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed()^0x5CA1L^salt,.052,2);
        double patch=.23+broad*config.broadNoise()+detail*config.detailNoise();
        double metasomatism=GeologicalNoise.smoothstep(-config.y(c.shapeSeed()),0,intrusion.surfaceDistance());
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x()-41,y+19,ctx.z()+67,c.gradeSeed()^salt,.065,3);
        double score=Math.min(patch,metasomatism);
        double alterP=GeologicalNoise.smoothstep(-.15,.45,score)*(.42+.42*grade);
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.alterationSeed()^salt,.15)>alterP) return state;

        // A carbonate-marble contact facies is distinct from the silicate skarn assemblage.
        // Fertility is fixed per parent; local mineralization still follows the contact field.
        if(settings().ores().size()>5&&intrusion.kind()==0
                &&rand01(c.alterationSeed()^salt^0x4C41504953L)<marbleChance&&broad>-.03){
            BlockState marble=com.teamtea.craton.common.registry.CratonBlocks.MARBLE.getOrigin().getBaseBlock().defaultBlockState();
            if(grade>marbleOreMinGrade&&detail>-.04&&oreAvailable(5))return abundantOre(this,ctx,c,y,5,marble);
            return marble;
        }

        BlockState altered=settings().rock();
        int first=intrusion.kind()==0?0:intrusion.kind()==1?2:4;
        int second=intrusion.kind()==0?1:intrusion.kind()==1?3:2;
        if(grade<contactMinGrade||!oreAvailable(first)&&!oreAvailable(second)) return altered;
        double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed()^salt,.115);
        double threshold=intrusion.kind()==0?intrusion0OreShare:intrusion.kind()==1?intrusion1OreShare:intrusion2OreShare;
        int mineral=mix<threshold?first:second;
        if(!oreAvailable(mineral))mineral=mineral==first?second:first;
        return abundantOre(this,ctx,c,y,mineral,altered);
    }

}
