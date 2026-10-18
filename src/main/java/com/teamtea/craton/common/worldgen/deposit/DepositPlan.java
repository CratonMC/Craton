package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.api.geology.deposit.*;
import net.minecraft.core.Holder;
import com.teamtea.craton.common.loot.OreAvailability;
import java.util.ArrayList;
import java.util.List;

/**
 * Compile immutable deposit rules during data loading for use across chunks.
 * Only definitions, ordering and derived constants are shared; runtime caches belong to DepositGenerationContext.
 */
public class DepositPlan {
    public static final DepositPlan EMPTY=new DepositPlan(List.of());
    private final List<Holder<Deposit>> sources;
    private final boolean availabilityResolved;
    private record ActivePlan(Object version,DepositPlan plan){}
    private volatile ActivePlan activePlan;
    final List<Definition> definitions;
    final List<SkarnRule> skarns;
    final double maximumPlacerDepth,maximumSkarnReach,maximumPeatDepth;
    final int[] ordinaryOrder;
    public record Definition(Deposit deposit,long salt,double reach){}
    public record SkarnRule(SkarnDeposit deposit,long salt){}

    public List<Definition> definitions(){return definitions;}
    public List<SkarnRule> skarns(){return skarns;}

    public double maximumPeatDepth(){return maximumPeatDepth;}
    public double maximumPlacerDepth(){return maximumPlacerDepth;}

    public static DepositPlan compile(List<? extends Holder<Deposit>> deposits){
        return new DepositPlan(List.copyOf(deposits));
    }

    /** Resolve after data maps and tags are available; keep original IDs and salts. */
    public DepositPlan active(){
        if(availabilityResolved||sources.isEmpty())return this;
        Object version=OreAvailability.version();
        ActivePlan cached=activePlan;
        if(cached!=null&&cached.version()==version)return cached.plan();
        DepositPlan resolved=new DepositPlan(sources,true);
        activePlan=new ActivePlan(version,resolved);
        return resolved;
    }

    public static boolean mineralizationAvailable(Deposit deposit){
        if(deposit instanceof BandedIronFormation bif)return bif.ore().value().available();
        return !(deposit instanceof FieldDeposit field)||field.mineralizationAvailable();
    }

    private DepositPlan(List<? extends Holder<Deposit>> deposits){
        this(deposits,false);
    }

    private DepositPlan(List<? extends Holder<Deposit>> deposits,boolean availabilityResolved){
        this.sources=List.copyOf(deposits);
        this.availabilityResolved=availabilityResolved;
        List<Definition> entries=new ArrayList<>();
        List<SkarnRule> contacts=new ArrayList<>();
        for(Holder<Deposit> holder:deposits){
            Deposit deposit=holder.value();
            // Intrusions and contact alteration retain their geological bodies without ores.
            if(availabilityResolved&&!(deposit instanceof IntrusionDeposit)&&!(deposit instanceof SkarnDeposit)
                    &&!mineralizationAvailable(deposit))continue;
            long salt=DepositCandidateSampler.salt(holder.unwrapKey().orElseThrow().identifier().toString());
            entries.add(new Definition(deposit,salt,deposit.placement().reach()));
            if(deposit instanceof SkarnDeposit skarn && skarn.settings().placement().frequency()>0) contacts.add(new SkarnRule(skarn,salt));
        }
        double peatDepth=0;
        for(Definition entry:entries)if(entry.deposit() instanceof PeatDeposit peat&&peat.placement().frequency()>0&&peat.placement().maxCandidates()>0)
            peatDepth=Math.max(peatDepth,peat.settings().shape().radiusYMax());
        maximumPeatDepth=peatDepth;
        double placerDepth=0;
        for(Definition entry:entries)
            if(entry.deposit() instanceof PlacerDeposit placer
                    && placer.placement().frequency()>0 && placer.placement().maxCandidates()>0)
                placerDepth=Math.max(placerDepth,placer.settings().shape().radiusYMax());
        this.maximumPlacerDepth=placerDepth;
        this.skarns=List.copyOf(contacts);
        double reach=0;
        for(SkarnRule rule:contacts)
            reach=Math.max(reach,rule.deposit().settings().shape().radiusYMax());
        this.maximumSkarnReach=reach;
        List<Definition> resolved=new ArrayList<>(entries.size());
        for(Definition entry:entries){
            Deposit deposit=entry.deposit();
            double candidateReach=deposit instanceof IntrusionDeposit intrusion
                    ?Math.max(entry.reach(),intrusion.contactReach(reach)):entry.reach();
            if(deposit instanceof GraniteIntrusionDeposit granite){
                for(Definition child:entries){
                    if(!(child.deposit() instanceof HydrothermalVeinDeposit vein)
                            ||vein.placement().frequency()<=0||vein.placement().maxCandidates()==0)continue;
                    double anchorOffset=Math.max(granite.settings().shape().radiusXMax(),granite.settings().shape().radiusZMax())+1;
                    candidateReach=Math.max(candidateReach,anchorOffset+vein.placement().reach());
                }
            }
            if(!(deposit instanceof SkarnDeposit)&&deposit.placement().frequency()>0&&deposit.placement().maxCandidates()>0
                    &&(!Double.isFinite(candidateReach)||candidateReach/deposit.placement().cellSize()>8))
                throw new IllegalArgumentException("Craton deposit "+deposit.getType()+" effective reach "+candidateReach
                        +" exceeds eight placement cells; increase cell_size or reduce radii/contact reach");
            resolved.add(new Definition(deposit,entry.salt(),candidateReach));
        }
        this.definitions=List.copyOf(resolved);
        List<Integer> order=new ArrayList<>();
        for(int i=0;i<definitions.size();i++){
            Deposit deposit=definitions.get(i).deposit();
            if(deposit instanceof OrdinaryDeposit&&!(deposit instanceof PlacerDeposit)&&!(deposit instanceof PeatDeposit)
                    &&deposit.placement().frequency()>0&&deposit.placement().maxCandidates()>0) order.add(i);
        }
        order.sort(java.util.Comparator.comparingInt(i -> ((OrdinaryDeposit)definitions.get(i).deposit()).order()));
        ordinaryOrder=order.stream().mapToInt(Integer::intValue).toArray();
    }
}
