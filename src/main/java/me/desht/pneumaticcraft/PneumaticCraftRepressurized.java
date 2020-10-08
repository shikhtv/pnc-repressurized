package me.desht.pneumaticcraft;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.ClientSetup;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.capabilities.CapabilityAirHandler;
import me.desht.pneumaticcraft.common.capabilities.CapabilityHacking;
import me.desht.pneumaticcraft.common.capabilities.CapabilityHeat;
import me.desht.pneumaticcraft.common.commands.ModCommands;
import me.desht.pneumaticcraft.common.config.ConfigHolder;
import me.desht.pneumaticcraft.common.config.subconfig.AuxConfigHandler;
import me.desht.pneumaticcraft.common.core.*;
import me.desht.pneumaticcraft.common.dispenser.BehaviorDispenseDrone;
import me.desht.pneumaticcraft.common.event.*;
import me.desht.pneumaticcraft.common.fluid.FluidSetup;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.heat.BlockHeatProperties;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.Reflections;
import me.desht.pneumaticcraft.common.util.upgrade.UpgradesDBSetup;
import me.desht.pneumaticcraft.common.villages.POIFixup;
import me.desht.pneumaticcraft.common.villages.VillageStructures;
import me.desht.pneumaticcraft.datagen.*;
import me.desht.pneumaticcraft.datagen.loot.ModLootFunctions;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Names.MOD_ID)
public class PneumaticCraftRepressurized {
    public PneumaticCraftRepressurized() {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ConfigHolder.init();
        AuxConfigHandler.preInit();

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSetup::initEarly);

        modBus.addListener(this::commonSetup);

        forgeBus.addListener(this::serverStopping);
        forgeBus.addListener(this::addReloadListeners);
        forgeBus.addListener(this::registerCommands);

        registerAllDeferredRegistryObjects(modBus);

        Reflections.init();
        PneumaticRegistry.init(PneumaticCraftAPIHandler.getInstance());
        AdvancementTriggers.registerTriggers();

        ModLootFunctions.init();

        forgeBus.register(new TickHandlerPneumaticCraft());
        forgeBus.register(new EventHandlerPneumaticCraft());
        forgeBus.register(new EventHandlerAmadron());
        forgeBus.register(new EventHandlerPneumaticArmor());
        forgeBus.register(new EventHandlerUniversalSensor());
        forgeBus.register(new DroneSpecialVariableHandler());
        forgeBus.register(new EventHandlerWorldGen());
        forgeBus.register(ItemGPSAreaTool.EventHandler.class);
        forgeBus.register(HackTickHandler.instance());

        modBus.addGenericListener(Feature.class, EventPriority.LOW, EventHandlerWorldGen::registerConfiguredFeatures);
        forgeBus.addListener(EventPriority.HIGH, EventHandlerWorldGen::onBiomeLoading);
    }

    private void registerAllDeferredRegistryObjects(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModFluids.FLUIDS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModTileEntities.TILE_ENTITIES.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModContainers.CONTAINERS.register(modBus);
        ModParticleTypes.PARTICLES.register(modBus);
        ModRecipes.RECIPES.register(modBus);
        ModDecorators.DECORATORS.register(modBus);
        ModVillagers.POI.register(modBus);
        ModVillagers.PROFESSIONS.register(modBus);

        // TODO: custom registries not handled via deferred registration (harvest handlers, hoe handlers, progwidgets)
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Log.info(Names.MOD_NAME + " is loading!");

        ThirdPartyManager.instance().init();
        AuxConfigHandler.init();
        registerCapabilities();
        NetworkHandler.init();
        FluidSetup.init();
        ArmorUpgradeRegistry.init();
        HackableHandler.addDefaultEntries();
        SensorHandler.getInstance().init();
        UpgradesDBSetup.init();
        POIFixup.fixup();
        VillageStructures.init();
        ModNameCache.init();

        event.enqueueWork(() -> {
            DispenserBlock.registerDispenseBehavior(ModItems.DRONE.get(), new BehaviorDispenseDrone());
            DispenserBlock.registerDispenseBehavior(ModItems.LOGISTICS_DRONE.get(), new BehaviorDispenseDrone());
            DispenserBlock.registerDispenseBehavior(ModItems.HARVESTING_DRONE.get(), new BehaviorDispenseDrone());

            ModEntities.registerGlobalAttributes();

            ThirdPartyManager.instance().postInit();

            for (RegistryObject<Block> block : ModBlocks.BLOCKS.getEntries()) {
                if (block.get() instanceof IUpgradeAcceptor) {
                    PneumaticRegistry.getInstance().getItemRegistry().registerUpgradeAcceptor((IUpgradeAcceptor) block.get());
                }
            }
            for (RegistryObject<Item> item : ModItems.ITEMS.getEntries()) {
                if (item.get() instanceof IUpgradeAcceptor) {
                    PneumaticRegistry.getInstance().getItemRegistry().registerUpgradeAcceptor((IUpgradeAcceptor) item.get());
                }
            }
        });
    }

    private void registerCapabilities() {
        CapabilityAirHandler.register();
        CapabilityHeat.register();
        CapabilityHacking.register();
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(PneumaticCraftRecipeType.getCacheReloadListener());
        event.addListener(new AmadronOfferManager.ReloadListener());
        event.addListener(new BlockHeatProperties.ReloadListener());
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    private void serverStopping(FMLServerStoppingEvent event) {
        AmadronOfferManager.getInstance().saveAll();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class DataGenerators {
        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            DataGenerator generator = event.getGenerator();
            if (event.includeServer()) {
                generator.addProvider(new ModRecipeProvider(generator));
                generator.addProvider(new ModLootTablesProvider(generator));
                BlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(generator);
                generator.addProvider(blockTagsProvider);
                generator.addProvider(new ModItemTagsProvider(generator, blockTagsProvider));
                generator.addProvider(new ModFluidTagsProvider(generator));
            }
        }
    }
}
