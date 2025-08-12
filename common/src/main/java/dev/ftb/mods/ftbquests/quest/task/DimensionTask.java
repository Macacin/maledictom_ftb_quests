package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class DimensionTask extends AbstractBooleanTask {
	private ResourceKey<Level> dimension;

	public DimensionTask(long id, Quest quest) {
		super(id, quest);

		dimension = Level.NETHER;
	}

	public DimensionTask withDimension(ResourceKey<Level> dimension) {
		this.dimension = dimension;
		return this;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.DIMENSION;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("dimension", dimension.location().toString());
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("dimension")));
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeResourceLocation(dimension.location());
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		dimension = ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		if (KnownServerRegistries.client != null && !KnownServerRegistries.client.dimensions.isEmpty()) {
			config.addEnum("dim", dimension.location(), v -> dimension = ResourceKey.create(Registries.DIMENSION, v), NameMap.of(KnownServerRegistries.client.dimensions.iterator().next(), KnownServerRegistries.client.dimensions.toArray(new ResourceLocation[0])).create());
		} else {
			config.addString("dim", dimension.location().toString(), v -> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(v)), "minecraft:the_nether");
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.task.ftbquests.dimension").append(": ").append(Component.literal(dimension.location().toString()).withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 100;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return !player.isSpectator() && player.level().dimension() == dimension;
	}
}
