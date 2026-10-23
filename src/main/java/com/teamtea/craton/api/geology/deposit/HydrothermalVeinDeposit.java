package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.PreparedPegmatite;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class HydrothermalVeinDeposit extends OrdinaryDeposit {
    public static final MapCodec<HydrothermalVeinDeposit> CODEC = RecordCodecBuilder.<HydrothermalVeinDeposit>mapCodec(i -> i.group(
            Settings.CODEC.fieldOf("settings").forGetter(HydrothermalVeinDeposit::settings),
            Codec.floatRange(0f, 1f).optionalFieldOf("emerald_chance", .40f).forGetter(HydrothermalVeinDeposit::emeraldChance),
            Codec.floatRange(0f, 1f).optionalFieldOf("emerald_min_grade", .53f).forGetter(HydrothermalVeinDeposit::emeraldMinGrade),
            Codec.floatRange(0f, 1f).optionalFieldOf("emerald_min_mix", .42f).forGetter(HydrothermalVeinDeposit::emeraldMinMix),
            Codec.floatRange(0f, 1f).optionalFieldOf("emerald_max_mix", .68f).forGetter(HydrothermalVeinDeposit::emeraldMaxMix)
    ).apply(i, HydrothermalVeinDeposit::new)).flatXmap(HydrothermalVeinDeposit::validate, HydrothermalVeinDeposit::validate);
    private static com.mojang.serialization.DataResult<HydrothermalVeinDeposit> validate(HydrothermalVeinDeposit d) {
        if (d.emeraldMinMix > d.emeraldMaxMix) return com.mojang.serialization.DataResult.error(() -> "Emerald min mix exceeds max mix");
        return FieldDeposit.validate(d.settings(), 4, false).map(v -> d);
    }
    private final float emeraldChance, emeraldMinGrade, emeraldMinMix, emeraldMaxMix;
    public float emeraldChance(){ return emeraldChance; }
    public float emeraldMinGrade(){ return emeraldMinGrade; }
    public float emeraldMinMix(){ return emeraldMinMix; }
    public float emeraldMaxMix(){ return emeraldMaxMix; }

    public HydrothermalVeinDeposit(Settings settings) {
        this(settings, .40f, .53f, .42f, .68f);
    }
    public HydrothermalVeinDeposit(Settings settings, float emeraldChance, float emeraldMinGrade, float emeraldMinMix, float emeraldMaxMix) {
        super(DepositTypes.VEIN,settings,DepositRandomSequences.VEIN,30,PreparedPegmatite.requiredReach(settings.shape()));
        this.emeraldChance=emeraldChance;
        this.emeraldMinGrade=emeraldMinGrade;
        this.emeraldMinMix=emeraldMinMix;
        this.emeraldMaxMix=emeraldMaxMix;
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public java.util.List<PreparedPegmatite.Attachment> attachedTo(IntrusionDeposit.Prepared parent,long salt){
        var out=new java.util.ArrayList<PreparedPegmatite.Attachment>();
        var placement=placement();
        for(int i=0;i<placement.maxCandidates();i++){
            long chance=DepositCandidateSampler.mix(parent.candidate.shapeSeed()^salt^((i+1L)*0x504547L));
            if(rand01(chance)>=placement.frequency())continue;
            var attachment=PreparedPegmatite.attach(parent,settings().shape(),salt,i);
            if(attachment!=null)out.add(attachment);
        }
        return java.util.List.copyOf(out);
    }

    @Override
    public boolean canPlace(DepositGenerationContext ctx,BlockState originalHost,BlockState state,int y){
        return !state.isAir()&&!state.is(Blocks.WATER)&&!state.is(Blocks.LAVA)&&!state.is(Blocks.BEDROCK);
    }

    @Override
    public BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        // Parent association determines provenance; surrounding strata may also be cut.
        if(!canPlace(ctx,originalHost,state,y)) return null;

        Shape config=settings().shape();
        PreparedPegmatite geometry=ctx.pegmatite(c,config);
        double score=geometry.score(ctx.x(),y,ctx.z());
        if(score<-.15) return null;
        // Solid pegmatite inside the dike; contact mixing is independent of ore grade.
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed()^0x504547L,.18)
                >GeologicalNoise.smoothstep(-1,1,geometry.surfaceDistance(score))) return null;
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.075,3);
        double p=GeologicalNoise.smoothstep(-.14,.38,score)*(.34+.58*grade);
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.18)>p) return settings().rock();
        double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.105);
        BlockState pegmatite=settings().rock();
        // Mafic or schist country rock is a coarse proxy for a favorable reactive wall rock,
        // not a claim that all such rocks provide chromium or vanadium.
        boolean emeraldHost=hostMatches(state,settings().hostTags());
        if(settings().ores().size()>4&&emeraldHost&&rand01(c.alterationSeed()^0x454D4552414C44L)<emeraldChance
                &&geometry.wallFraction()>.25&&geometry.wallFraction()<.90&&grade>emeraldMinGrade&&mix>emeraldMinMix&&mix<emeraldMaxMix)
            return abundantOre(this,ctx,c,y,4,pegmatite);
        if(geometry.wallFraction()>.30&&grade>.72&&mix<.22) return abundantOre(this,ctx,c,y,0,pegmatite);
        if(geometry.wallFraction()>.45&&grade>.58&&mix<.55) return abundantOre(this,ctx,c,y,1,pegmatite);
        if(geometry.wallFraction()>.55&&mix>.82) return abundantOre(this,ctx,c,y,2,pegmatite);
        if(geometry.wallFraction()<.55&&mix<.82) return abundantOre(this,ctx,c,y,3,pegmatite);
        return pegmatite;
    }

}
