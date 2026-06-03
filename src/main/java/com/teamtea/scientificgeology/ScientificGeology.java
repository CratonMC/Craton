package com.teamtea.scientificgeology;


import net.minecraft.resources.Identifier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ScientificGeology.MODID)
public class ScientificGeology {
    public static final String MODID = "scientific_geology";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final String NETWORK_VERSION = "1.0";

    public ScientificGeology(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::FMLCommonSetup);
        modEventBus.addListener(this::FMLCommonSetup);
        modEventBus.addListener(this::gatherData);



        if (FMLLoader.getCurrentOrNull().getDist() == Dist.CLIENT)
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

    }


    public static Identifier rl(String id) {
        return Identifier.fromNamespaceAndPath(MODID, id);
    }

    public static Identifier parse(String id) {
        return Identifier.parse(id);
    }


    public void FMLCommonSetup(final FMLCommonSetupEvent event) {
        // SimpleNetworkHandler.init();
        // CompatModule.init();
    }


    public void gatherData(final GatherDataEvent.Client event) {
    }


    public static void logger(String x) {
        LOGGER.info(x);
    }

    public static void logger(Object... x) {

        // if (!FMLEnvironment.production||General.bool.get())
        {
            StringBuilder output = new StringBuilder();

            for (Object i : x) {
                if (i == null) output.append(", ").append("null");
                else if (i.getClass().isArray()) {
                    output.append(", [");
                    if (i instanceof Object[] objects) {
                        for (Object c : objects) {
                            output.append(c).append(",");
                        }
                    } else if (i instanceof float[] objects) {
                        for (float c : objects) {
                            output.append(c).append(",");
                        }
                    } else if (i instanceof int[] objects) {
                        for (int c : objects) {
                            output.append(c).append(",");
                        }
                    } else if (i instanceof double[] objects) {
                        for (double c : objects) {
                            output.append(c).append(",");
                        }
                    } else if (i instanceof long[] objects) {
                        for (long c : objects) {
                            output.append(c).append(",");
                        }
                    } else if (i instanceof boolean[] objects) {
                        for (boolean c : objects) {
                            output.append(c).append(",");
                        }
                    }
                    output.append("]");
                } else if (i instanceof List list) {
                    output.append(", [");
                    for (Object c : list) {
                        output.append(c);
                    }
                    output.append("]");
                } else
                    output.append(", ").append(i);
            }
            LOGGER.info(output.substring(1));
        }

    }


}
