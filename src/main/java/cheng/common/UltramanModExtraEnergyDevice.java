package cheng.common;

import com.benniao.ultraman.UltramanMod;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(UltramanModExtraEnergyDevice.MODID)
public class UltramanModExtraEnergyDevice {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "ultraman_mod_extra_energy_device";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnergyDataComponents>> ENERGY_DATA_COMPONENT =
            DATA_COMPONENTS.registerComponentType(
                    "energy_data", builder -> builder
                            .persistent(EnergyDataComponents.codec)
                            .networkSynchronized(EnergyDataComponents.streamCodec)
            );
    // 0.5x Weakened version
    // 0.5x弱化款
    public static final EnergyDataComponents NEGATIVE_ENERGY_DATA = basicEnergyDataComponents(1800);
    public static final EnergyDataComponents NEGATIVE_ENERGY_DATA_MAX = basicMaxEnergyDataComponents(1800);
    // 1.0x Basic version
    // 1.0x基础款
    public static final EnergyDataComponents ENERGY_DATA = basicEnergyDataComponents(3600);
    public static final EnergyDataComponents ENERGY_DATA_MAX = basicMaxEnergyDataComponents(3600);
    // 1.5x Reinforced version
    // 1.5x强化款
    public static final EnergyDataComponents ENERGY_DATA_POWER_UP = basicEnergyDataComponents(5400);
    public static final EnergyDataComponents ENERGY_DATA_POWER_UP_MAX = basicMaxEnergyDataComponents(5400);
    // 2.0x Reinforced version
    // 2.0x强化款
    public static final EnergyDataComponents ENERGY_DATA_SECOND_POWER_UP = basicEnergyDataComponents(7200);
    public static final EnergyDataComponents ENERGY_DATA_SECOND_POWER_UP_MAX = basicMaxEnergyDataComponents(7200);

    public static final DeferredItem<Item> ENERGY_DEVICE = registerEnergyDevice("energy_device", ENERGY_DATA);

    public UltramanModExtraEnergyDevice(IEventBus modEventBus, ModContainer modContainer) {

        DATA_COMPONENTS.register(modEventBus);
        ITEMS.register(modEventBus);

        modEventBus.addListener(this::addItem);
    }

    public void addItem(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == UltramanMod.PLASMA_SPARK_TAB.get()) {
            event.accept(setEnergyData(ENERGY_DEVICE, NEGATIVE_ENERGY_DATA));
            event.accept(setEnergyData(ENERGY_DEVICE, NEGATIVE_ENERGY_DATA_MAX));
            event.accept(ENERGY_DEVICE);
            event.accept(setEnergyData(ENERGY_DEVICE, ENERGY_DATA_MAX));
            event.accept(setEnergyData(ENERGY_DEVICE, ENERGY_DATA_POWER_UP));
            event.accept(setEnergyData(ENERGY_DEVICE, ENERGY_DATA_POWER_UP_MAX));
            event.accept(setEnergyData(ENERGY_DEVICE, ENERGY_DATA_SECOND_POWER_UP));
            event.accept(setEnergyData(ENERGY_DEVICE, ENERGY_DATA_SECOND_POWER_UP_MAX));
        }
    }

    public static DeferredItem<Item> registerEnergyDevice(String name, EnergyDataComponents energyDataComponents) {
        return ITEMS.register(name, () -> new EnergyDeviceItem(new Item.Properties().stacksTo(1)
                .component(ENERGY_DATA_COMPONENT.get(), energyDataComponents)));
    }

    public static ItemStack setEnergyData(DeferredItem<Item> item, EnergyDataComponents energyDataComponents) {
        ItemStack stack = item.toStack();
        stack.set(ENERGY_DATA_COMPONENT.get(), energyDataComponents);
        return stack;
    }

    public static EnergyDataComponents basicEnergyDataComponents(float energyMax) {
        return basicEnergyDataComponents(0, energyMax);
    }

    public static EnergyDataComponents basicMaxEnergyDataComponents(float energyMax) {
        return basicEnergyDataComponents(energyMax, energyMax);
    }

    public static EnergyDataComponents basicEnergyDataComponents(float energy, float energyMax) {
        if (energy <= energyMax)
            return basicEnergyDataComponents(energy, energyMax, 0.25f, 0.5f);
        return basicEnergyDataComponents( energyMax, energyMax, 0.25f, 0.5f);
    }

    public static EnergyDataComponents basicEnergyDataComponents(float energy, float energyMax, float replenishEnergy, float energySafeDimensionReplenishEnergy) {
        return basicEnergyDataComponents(false, energy, energyMax, replenishEnergy, energySafeDimensionReplenishEnergy);
    }

    public static EnergyDataComponents basicEnergyDataComponents(boolean isUse, float energy, float energyMax,float replenishEnergy,float energySafeDimensionReplenishEnergy) {
        return new EnergyDataComponents(isUse, energy, energyMax, replenishEnergy, energySafeDimensionReplenishEnergy);
    }
}
