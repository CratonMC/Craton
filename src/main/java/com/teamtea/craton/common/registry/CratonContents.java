package com.teamtea.craton.common.registry;

import com.teamtea.craton.common.worldgen.deposit.DepositPlan;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.api.geology.deposit.Deposit;
import net.minecraft.resources.Identifier;
import com.teamtea.craton.api.geology.ore.OreType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Climate;
import com.teamtea.craton.api.geology.GeologyRegion;
import com.teamtea.craton.common.worldgen.geology.GeologyTransition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.NewDatapackRegistryEvent;

import java.util.*;
import java.util.stream.Stream;

@EventBusSubscriber
public class CratonContents {

    @SubscribeEvent
    public static void onNewRegistry(NewDatapackRegistryEvent event) {
        event.worldRegistry(
                CratonRegistries.GEOLOGY_REGION,
                GeologyRegion.CODEC,
                GeologyRegion.CODEC
        );

        event.worldRegistry(
                CratonRegistries.GEOLOGY_LAYER,
                GeologyLayer.CODEC,
                GeologyLayer.CODEC
        );

        event.worldRegistry(
                CratonRegistries.GEOLOGY_PROFILE,
                GeologyProfile.CODEC,
                GeologyProfile.CODEC
        );

        event.worldRegistry(
                CratonRegistries.DEPOSIT,
                Deposit.CODEC,
                Deposit.CODEC
        );

        event.worldRegistry(
                CratonRegistries.ORE_TYPE,
                OreType.CODEC,
                OreType.CODEC
        );
    }

    /** One immutable generation view retained by a complete chunk build. */
    public record GenerationData(
            List<Holder<GeologyRegion>> regions,
            Map<ResourceKey<GeologyRegion>, Holder<GeologyProfile>> profiles,
            GeologyTransition transition,
            com.teamtea.craton.common.worldgen.deposit.DepositPlan depositPlan
    ) {
        public GenerationData {
            regions = List.copyOf(regions);
            profiles = Map.copyOf(profiles);
        }

        private Optional<Holder<GeologyRegion>> classify(Climate.TargetPoint sample) {
            long pv = GeologyRegion.peaksAndValleys(sample.weirdness());
            for (Holder<GeologyRegion> region : regions) {
                if (region.value().matches(sample, pv)) return Optional.of(region);
            }
            return Optional.empty();
        }

        public Optional<Holder<GeologyProfile>> getGeologyProfile(Climate.TargetPoint sample) {
            return classify(sample).flatMap(Holder::unwrapKey).map(profiles::get);
        }
    }

    private record Contents(
            GenerationData generation,
            List<Holder<GeologyProfile>> profiles,
            List<Holder<Deposit>> deposits,
            Map<ResourceKey<OreType>, Holder<OreType>> ores,
            Map<Identifier, List<Holder<Deposit>>> depositsByType
    ) {
        private static final Contents EMPTY = new Contents(
                new GenerationData(List.of(), Map.of(), new GeologyTransition(List.of(), Map.of()),
                        com.teamtea.craton.common.worldgen.deposit.DepositPlan.EMPTY),
                List.of(), List.of(), Map.of(), Map.of());
    }

    // Publish only after all rules have compiled successfully. No collection is mutated in place.
    private static volatile Contents contents = Contents.EMPTY;

    public static GenerationData getGenerationData() { return contents.generation(); }
    public static GeologyTransition getGeologyTransition() { return contents.generation().transition(); }
    public static List<Holder<GeologyRegion>> getGeologyRegions() { return contents.generation().regions(); }
    public static Optional<Holder<GeologyRegion>> getGeologyRegion(Climate.TargetPoint sample) {
        return contents.generation().classify(sample);
    }
    public static List<Holder<GeologyProfile>> getGeologyProfiles() { return contents.profiles(); }
    public static com.teamtea.craton.common.worldgen.deposit.DepositPlan getDepositPlan() {
        return contents.generation().depositPlan();
    }
    public static Map<Identifier, List<Holder<Deposit>>> getDepositsByType() { return contents.depositsByType(); }
    public static List<Holder<Deposit>> getDeposits() { return contents.deposits(); }
    public static Optional<Holder<OreType>> getOreType(ResourceKey<OreType> key) {
        return Optional.ofNullable(contents.ores().get(key));
    }
    public static Optional<Holder<GeologyProfile>> getGeologyProfile(Climate.TargetPoint sample) {
        return contents.generation().getGeologyProfile(sample);
    }

    @SubscribeEvent
    public static void onNewRegistry(TagsUpdatedEvent.ServerDataLoad event) {
        List<Holder<GeologyProfile>> profiles = new ArrayList<>(event.getRegistries()
                .lookup(CratonRegistries.GEOLOGY_PROFILE).map(Registry::listElements)
                .map(Stream::toList).orElse(List.of()));
        List<Holder<Deposit>> deposits = new ArrayList<>(event.getRegistries()
                .lookup(CratonRegistries.DEPOSIT).map(Registry::listElements)
                .map(Stream::toList).orElse(List.of()));
        deposits.sort(Comparator.comparing(entry -> entry.unwrapKey().orElseThrow().identifier().toString()));
        Map<Identifier, List<Holder<Deposit>>> grouped = new HashMap<>();
        for (Holder<Deposit> entry : deposits)
            grouped.computeIfAbsent(entry.value().getType(), key -> new ArrayList<>()).add(entry);
        grouped.replaceAll((type, entries) -> List.copyOf(entries));
        var plan = com.teamtea.craton.common.worldgen.deposit.DepositPlan.compile(deposits);

        Map<ResourceKey<OreType>, Holder<OreType>> ores = new HashMap<>();
        event.getRegistries().lookup(CratonRegistries.ORE_TYPE)
                .ifPresent(registry -> registry.listElements().forEach(holder ->
                        holder.unwrapKey().ifPresent(key -> ores.put(key, holder))));

        // Higher priority wins; equal priorities use the registry ID deterministically.
        profiles.sort(Comparator.<Holder<GeologyProfile>>comparingInt(entry -> entry.value().priority()).reversed()
                .thenComparing(entry -> entry.unwrapKey().orElseThrow().identifier().toString()));
        List<Holder<GeologyRegion>> regions = new ArrayList<>();
        event.getRegistries().lookup(CratonRegistries.GEOLOGY_REGION)
                .ifPresent(registry -> registry.listElements().forEach(regions::add));
        regions.sort(Comparator.<Holder<GeologyRegion>>comparingInt(entry -> entry.value().priority()).reversed()
                .thenComparing(entry -> entry.unwrapKey().orElseThrow().identifier().toString()));
        Map<ResourceKey<GeologyRegion>, Holder<GeologyProfile>> selected = new HashMap<>();
        for (Holder<GeologyProfile> profile : profiles)
            selected.putIfAbsent(profile.value().region().unwrapKey().orElseThrow(), profile);

        GenerationData generation = new GenerationData(regions, selected,
                new GeologyTransition(regions, selected), plan);
        contents = new Contents(generation, List.copyOf(profiles), List.copyOf(deposits),
                Map.copyOf(ores), Map.copyOf(grouped));
    }

    @SubscribeEvent
    public static void onNewRegistry(ServerStoppedEvent event) { contents = Contents.EMPTY; }
}
