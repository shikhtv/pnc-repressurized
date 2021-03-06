package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidSetup {
    /**
     * Fluid setup tasks to be done AFTER fluids (and items/blocks) are registered
     *
     * Note: fluid fuel values now all done via datapack
     */
    public static void init() {
        PneumaticCraftAPIHandler api = PneumaticCraftAPIHandler.getInstance();

        // register hot fluids as (very inefficient) fuels
        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
            if (fluid.getAttributes().getTemperature() >= PNCConfig.Common.General.minFluidFuelTemperature && fluid.isSource(fluid.getDefaultState())) {
                // non-API usage... register an explicit fluid rather than a tag
                FuelRegistry.getInstance().registerHotFluid(fluid, (fluid.getAttributes().getTemperature() - 300) * 40, 0.25f);
            }
        }

        // no magnet'ing PCB's out of etching acid pools
        api.getItemRegistry().registerMagnetSuppressor(
                e -> e instanceof ItemEntity && ((ItemEntity) e).getItem().getItem() == ModItems.EMPTY_PCB.get()
                        && e.getEntityWorld().getFluidState(e.getPosition()).getFluid() == ModFluids.ETCHING_ACID.get()
        );

        api.registerXPFluid(ModFluids.MEMORY_ESSENCE.get(), 20);
    }
}
