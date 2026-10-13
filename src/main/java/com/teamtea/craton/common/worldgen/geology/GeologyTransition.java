package com.teamtea.craton.common.worldgen.geology;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.api.geology.GeologyRegion;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Climate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Immutable world-wide horizon layout. Column weights never change the identity/order of its slots. */
public class GeologyTransition {
    private final List<Holder<GeologyLayer>> layers;
    private final List<Holder<GeologyProfile>> profiles;
    private final List<int[]> mappings;
    private final List<Rule> rules;
    private final boolean smoothTransitions;
    private record Rule(GeologyRegion region,int profile) {}

    public GeologyTransition(List<Holder<GeologyRegion>> regions,
                             Map<ResourceKey<GeologyRegion>,Holder<GeologyProfile>> selectedProfiles) {
        var ordered=new ArrayList<>(selectedProfiles.values());
        ordered.removeIf(profile -> profile.value().layers().isEmpty());
        ordered.sort(Comparator.comparing(profile -> profile.unwrapKey().orElseThrow().identifier().toString()));
        profiles=List.copyOf(ordered);
        var profileIndices=new HashMap<ResourceKey<GeologyProfile>,Integer>();
        var slots=new ArrayList<Holder<GeologyLayer>>();
        var maps=new ArrayList<int[]>();
        for(int source=0;source<profiles.size();source++) {
            var profile=profiles.get(source);
            profileIndices.put(profile.unwrapKey().orElseThrow(),source);
            List<Holder<GeologyLayer>> incoming=profile.value().layers();
            // Shortest-common-supersequence alignment preserves both source orders.
            // Reversed/repeated horizons get distinct slots rather than crossing contacts.
            int n=slots.size(),m=incoming.size();
            int[][] lcs=new int[n+1][m+1];
            for(int i=n-1;i>=0;i--) for(int j=m-1;j>=0;j--)
                lcs[i][j]=same(slots.get(i),incoming.get(j))?1+lcs[i+1][j+1]
                        :Math.max(lcs[i+1][j],lcs[i][j+1]);
            var merged=new ArrayList<Holder<GeologyLayer>>();
            int[] oldSlots=new int[n],newSlots=new int[m];
            int i=0,j=0;
            while(i<n||j<m) {
                int slot=merged.size();
                if(i<n&&j<m&&same(slots.get(i),incoming.get(j))) {
                    oldSlots[i]=slot;newSlots[j]=slot;merged.add(slots.get(i));i++;j++;
                } else if(i<n&&(j==m||lcs[i+1][j]>=lcs[i][j+1])) {
                    oldSlots[i]=slot;merged.add(slots.get(i++));
                } else {
                    newSlots[j]=slot;merged.add(incoming.get(j++));
                }
            }
            for(int[] mapping:maps) for(int k=0;k<mapping.length;k++) mapping[k]=oldSlots[mapping[k]];
            maps.add(newSlots);slots=merged;
        }
        layers=List.copyOf(slots);
        mappings=List.copyOf(maps);
        var orderedRules=new ArrayList<Holder<GeologyRegion>>(regions);
        orderedRules.sort(Comparator.<Holder<GeologyRegion>>comparingInt(r -> r.value().priority()).reversed()
                .thenComparing(r -> r.unwrapKey().orElseThrow().identifier().toString()));
        var builtRules=new ArrayList<Rule>();
        for(var region:orderedRules) {
            var profile=selectedProfiles.get(region.unwrapKey().orElseThrow());
            Integer index=profile==null?null:profileIndices.get(profile.unwrapKey().orElseThrow());
            builtRules.add(new Rule(region.value(),index==null?-1:index));
        }
        rules=List.copyOf(builtRules);
        smoothTransitions=rules.stream().anyMatch(rule -> rule.region().transitionWidth()>0);
    }

    private static boolean same(Holder<GeologyLayer> a,Holder<GeologyLayer> b) {
        return a.unwrapKey().orElseThrow().equals(b.unwrapKey().orElseThrow());
    }

    public List<Holder<GeologyLayer>> layers(){return layers;}
    public List<Holder<GeologyProfile>> profiles(){return profiles;}
    public boolean hasSmoothTransitions(){return smoothTransitions;}
    int slot(int source,int sourceLayer){return mappings.get(source)[sourceLayer];}

    /** Priority compositing keeps weights continuous even at three-region junctions. */
    public double[] weights(Climate.TargetPoint sample) {
        double[] weights=new double[profiles.size()];
        long pv=GeologyRegion.peaksAndValleys(sample.weirdness());
        double remaining=1;
        for(Rule rule:rules) {
            double alpha=rule.region().coverage(sample,pv);
            double weight=remaining*alpha;
            if(rule.profile()>=0) weights[rule.profile()]+=weight;
            remaining*=1-alpha;
            if(remaining==0) break;
        }
        double total=0;
        for(double weight:weights) total+=weight;
        if(total>0) for(int i=0;i<weights.length;i++) weights[i]/=total;
        return weights;
    }
}
