package cheng.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EnergyDataComponents(boolean isUse, float energy, float energyMax,float replenishEnergy,float energySafeDimensionReplenishEnergy) {
    public static final Codec<EnergyDataComponents> codec = RecordCodecBuilder.create(energyInstance ->
        energyInstance.group(
                Codec.BOOL.fieldOf("isUse").forGetter(EnergyDataComponents::isUse),
                Codec.FLOAT.fieldOf("energy").forGetter(EnergyDataComponents::energy),
                Codec.FLOAT.fieldOf("energyMax").forGetter(EnergyDataComponents::energyMax),
                Codec.FLOAT.fieldOf("replenishEnergy").forGetter(EnergyDataComponents::replenishEnergy),
                Codec.FLOAT.fieldOf("energySafeDimensionReplenishEnergy").forGetter(EnergyDataComponents::energySafeDimensionReplenishEnergy)
        ).apply(energyInstance, EnergyDataComponents::new)
    );

    public static final StreamCodec<ByteBuf, EnergyDataComponents> streamCodec = StreamCodec.composite(
            ByteBufCodecs.BOOL, EnergyDataComponents::isUse,
            ByteBufCodecs.FLOAT, EnergyDataComponents::energy,
            ByteBufCodecs.FLOAT, EnergyDataComponents::energyMax,
            ByteBufCodecs.FLOAT, EnergyDataComponents::replenishEnergy,
            ByteBufCodecs.FLOAT, EnergyDataComponents::energySafeDimensionReplenishEnergy,
            EnergyDataComponents::new
    );

}