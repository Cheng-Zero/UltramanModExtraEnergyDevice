package cheng.common;

import com.benniao.ultraman.registry.attachments.UltramanEnergyData;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;

public class EnergyDeviceItem extends Item {
    public static final float consumeEnergy = 1;
    public static final ResourceKey<Level> BlackHole = getDimension("ultraman_mod:black_hole");
    public static final ResourceKey<Level> LandOfLight = getDimension("ultraman_mod:land_of_light");

    public EnergyDeviceItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        EnergyDataComponents getenergy = getenergy(stack);
        tooltipComponents.add(Component.translatable("item.ultraman_mod_extra_energy_device.energy_device.title.energy_max", getenergy.energyMax()));
        tooltipComponents.add(Component.translatable("item.ultraman_mod_extra_energy_device.energy_device.title.energy", getenergy.energy()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        InteractionResultHolder<ItemStack> use = super.use(level, player, interactionHand);

        ItemStack stack = use.getObject();
        EnergyDataComponents energyData = getenergy(stack);

        if (energyData != null) {
            if (energyData.isUse()){
                if (!level.isClientSide())
                    setEnergy(stack,false);
                player.displayClientMessage(Component.translatable("item.ultraman_mod_extra_energy_device.energy_device.title.use_on.disabled"),true);
            }
            else {
                if (!level.isClientSide())
                    setEnergy(stack,true);
                player.displayClientMessage(Component.translatable("item.ultraman_mod_extra_energy_device.energy_device.title.use_on.enabled"),true);
            }
        }else {
            Logger LOGGER = LogUtils.getLogger();
            LOGGER.warn("EnergyData Device EnergyData is null");
        }
        return use;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide()) return;

        EnergyDataComponents energy = getOrDefaultEnergyData(stack);

        boolean isBlackHole = level.dimension() == BlackHole;
        boolean isLandOfLight = level.dimension() == LandOfLight;
        boolean inEnergySafeDimension = isBlackHole || isLandOfLight;
//        boolean isOnHand = player.getItemInHand(InteractionHand.MAIN_HAND).equals(stack)|| player.getItemInHand(InteractionHand.OFF_HAND).equals(stack);

        setModel(energy,stack);

        // 开启中
        if (energy.isUse()) {
            // 获取
            UltramanEnergyData energyData = UltramanEnergyData.getData(player);
            float before = energyData.getCurrentEnergy();
            float maxEnergy = energyData.getMaxEnergy();
            // 计算差值
            float chazhi = maxEnergy - before;

            // 石像模式返回
            if (energyData.isStoneMode()) return;

            // 差值 > 消耗执行 && 存储能量 >= 消耗能量
            if (chazhi > consumeEnergy && energy.energy() >= consumeEnergy && consumeEnergy(stack, consumeEnergy)) {
                energyData.replenishEnergy(consumeEnergy);
            }
            // 存储能量 < 消耗能量
            else if (energy.energy() < consumeEnergy) {
                setEnergy(stack,false);
            }
        }
        // 否则不开始 并 处于能量充盈维度
        else if (inEnergySafeDimension) {
            addEnergy(stack, energy.energySafeDimensionReplenishEnergy());
        }
        // 否则不处于能量充盈维度
        else {
            addEnergy(stack, energy.replenishEnergy());
        }
    }

    // 辅助方法：安全地获取能量
    public static float getEnergy(ItemStack stack) {
        EnergyDataComponents energyData = getenergy(stack);
        return energyData != null ? energyData.energy() : 0f;
    }

    // 辅助方法：安全地获取最大能量
    public static float getMaxEnergy(ItemStack stack) {
        EnergyDataComponents energyData = getenergy(stack);
        return energyData != null ? energyData.energyMax() : 0f;
    }

    // 辅助方法：消耗能量
    public static boolean consumeEnergy(ItemStack stack, float amount) {
        EnergyDataComponents energyData = getenergy(stack);
        if (energyData == null) return false;

        float newEnergy = energyData.energy() - amount;
        if (newEnergy < 0) return false;

        setEnergy(stack, energyData.isUse(), newEnergy, energyData.energyMax(), energyData.replenishEnergy(), energyData.energySafeDimensionReplenishEnergy());
        return true;
    }

    // 辅助方法：增加能量
    public static void addEnergy(ItemStack stack, float amount) {
        EnergyDataComponents energyData = getenergy(stack);
        if (energyData == null) return;

        float newEnergy = Math.min(energyData.energy() + amount, energyData.energyMax());
        setEnergy(stack, energyData.isUse(), newEnergy, energyData.energyMax(), energyData.replenishEnergy(), energyData.energySafeDimensionReplenishEnergy());
    }

    // 进度条
    @Override
    public int getBarWidth(ItemStack stack) {
        EnergyDataComponents energyData = getenergy(stack);
        if (energyData == null) return 0;
        return Math.round(energyData.energy() * 13.0F / energyData.energyMax());
    }
    // 进度条显示
    @Override
    public boolean isBarVisible(ItemStack stack) {
        EnergyDataComponents energyData = getenergy(stack);
        if (energyData == null) return false;
        return stack.has(UltramanModExtraEnergyDevice.ENERGY_DATA_COMPONENT.get()) && energyData.energy() != energyData.energyMax();
    }
    // 进度条颜色
    @Override
    public int getBarColor(ItemStack stack) {
        EnergyDataComponents energyData = getenergy(stack);
        if (energyData == null) return -1;
        float stackEnergyMax = energyData.energyMax();
        float f = energyData.energy() / stackEnergyMax;
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        EnergyDataComponents energyData = getenergy(stack);
        if (energyData == null) return false;
        return energyData.isUse();
    }

    private static ResourceKey<Level> getDimension(String dimensionId){
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionId));
    }

    private static EnergyDataComponents getenergy(ItemStack itemStack){
        return itemStack.get(UltramanModExtraEnergyDevice.ENERGY_DATA_COMPONENT.get());
    }

    private static void setEnergy(ItemStack stack,boolean isUse) {
        EnergyDataComponents getenergy = getenergy(stack);
        setEnergy(stack,
                isUse,
                getenergy.energy(),
                getenergy.energyMax(),
                getenergy.replenishEnergy(),
                getenergy.energySafeDimensionReplenishEnergy());
    }

    private static void setEnergy(ItemStack stack,boolean isUse,float energy,float energyMax,float replenishEnergy,float energySafeDimensionReplenishEnergy) {
        setEnergy(stack,
                new EnergyDataComponents(
                        isUse,
                        energy,
                        energyMax,
                        replenishEnergy,
                        energySafeDimensionReplenishEnergy));
    }

    private static void setEnergy(ItemStack stack, EnergyDataComponents energyData) {
        stack.set(UltramanModExtraEnergyDevice.ENERGY_DATA_COMPONENT.get(), energyData);
    }

    private EnergyDataComponents data(){
        DataComponentMap components = this.components();
        return components.get(UltramanModExtraEnergyDevice.ENERGY_DATA_COMPONENT.get());
    }

    private EnergyDataComponents getOrDefaultEnergyData(ItemStack stack){
        EnergyDataComponents data = data();
        return stack.getOrDefault(UltramanModExtraEnergyDevice.ENERGY_DATA_COMPONENT.get(),
                new EnergyDataComponents(data.isUse(), data.energy(), data.energyMax(), data.replenishEnergy(), data.energySafeDimensionReplenishEnergy()));
    }

    private void setModel(EnergyDataComponents energy,ItemStack stack){
        if (energy.energy() >= energy.energyMax()) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));
        }else {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(0));
        }
    }
}
