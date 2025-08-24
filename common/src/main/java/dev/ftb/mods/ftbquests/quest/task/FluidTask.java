package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.fluid.FluidStack;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.FluidConfig;
import dev.ftb.mods.ftblibrary.config.NBTConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FluidTask extends Task {
	public static final ResourceLocation TANK_TEXTURE = new ResourceLocation(FTBQuestsAPI.MOD_ID, "textures/tasks/tank.png");

	private Fluid fluid = Fluids.WATER;
	private CompoundTag fluidNBT = null;
	private long amount = FluidStack.bucketAmount();
	private FluidStack cachedFluidStack = null;

	public FluidTask(long id, Quest quest) {
		super(id, quest);
	}

	public Fluid getFluid() {
		return fluid;
	}

	public FluidTask setFluid(Fluid fluid) {
		this.fluid = fluid;
		return this;
	}

	public CompoundTag getFluidNBT() {
		return fluidNBT;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.FLUID;
	}

	@Override
	public long getMaxProgress() {
		return amount;
	}

	@Override
	public String formatMaxProgress() {
		return getVolumeString(amount);
	}

	@Override
	public String formatProgress(TeamData teamData, long progress) {
		return getVolumeString((int) Math.min(Integer.MAX_VALUE, progress));
	}

	@Override
	public boolean consumesResources() {
		return true;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("fluid", RegistrarManager.getId(fluid, Registries.FLUID).toString());
		nbt.putLong("amount", amount);

		if (fluidNBT != null) {
			nbt.put("nbt", fluidNBT);
		}
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);

		fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(nbt.getString("fluid")));

		if (fluid == null || fluid == Fluids.EMPTY) {
			fluid = Fluids.WATER;
		}

		amount = Math.max(1L, nbt.getLong("amount"));
		fluidNBT = (CompoundTag) nbt.get("nbt");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeResourceLocation(RegistrarManager.getId(fluid, Registries.FLUID));
		buffer.writeNbt(fluidNBT);
		buffer.writeVarLong(amount);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		fluid = BuiltInRegistries.FLUID.get(buffer.readResourceLocation());

		if (fluid == null || fluid == Fluids.EMPTY) {
			fluid = Fluids.WATER;
		}

		fluidNBT = buffer.readNbt();
		amount = buffer.readVarLong();
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();
		cachedFluidStack = null;
	}

	public FluidStack createFluidStack() {
		if (cachedFluidStack == null) {
			cachedFluidStack = FluidStack.create(fluid, FluidStack.bucketAmount(), fluidNBT);
		}

		return cachedFluidStack;
	}

	public static String getVolumeString(long a) {
		StringBuilder builder = new StringBuilder();

		if (a >= FluidStack.bucketAmount()) {
			if (a % FluidStack.bucketAmount() != 0L) {
				builder.append(StringUtils.formatDouble(a / (double) FluidStack.bucketAmount()));
			} else {
				builder.append(a / FluidStack.bucketAmount());
			}
			builder.append(" B");
		} else {
			builder.append(a).append(" mB");
		}

		return builder.toString();
	}

	@Override
	public MutableComponent getAltTitle() {
		return Component.literal(getVolumeString(amount) + " of ").append(createFluidStack().getName());
	}

	@Override
	public Icon getAltIcon() {
		FluidStack stack = createFluidStack();

		return Icon.getIcon(ClientUtils.getStillTexture(stack)).withTint(Color4I.rgb(ClientUtils.getFluidColor(stack)));
	}

	@Override
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		config.add("fluid", new FluidConfig(false), FluidStack.create(fluid, 1000), v -> fluid = v.getFluid(), FluidStack.create(Fluids.WATER, 1000));
		config.add("fluid_nbt", new NBTConfig(), fluidNBT, v -> fluidNBT = v, null);
		config.addLong("amount", amount, v -> amount = v, FluidStack.bucketAmount(), 1, Long.MAX_VALUE);
	}

	@Override
	public boolean canInsertItem() {
		return true;
	}

	@Override
	@Nullable
	public Optional<PositionedIngredient> getIngredient(Widget widget) {
		return PositionedIngredient.of(createFluidStack(), widget);
	}
}
