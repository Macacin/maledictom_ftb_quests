package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class QuestBarrierBlockEntity extends BlockEntity implements BarrierBlockEntity {
	private long objId = 0L;

	public QuestBarrierBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(FTBQuestsBlockEntities.BARRIER.get(), blockPos, blockState);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		objId = QuestObjectBase.parseCodeString(tag.getString("Object"));
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putString("Object", QuestObjectBase.getCodeString(objId));
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return saveWithoutMetadata();
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.getChunkSource().blockChanged(getBlockPos());
		}
	}

	@Override
	public void update(String s) {
		objId = ServerQuestFile.INSTANCE.getID(s);
		setChanged();
	}

	@Override
	public boolean isOpen(Player player) {
		BaseQuestFile file = FTBQuestsAPI.api().getQuestFile(player.level().isClientSide());
		QuestObject qo = file.get(objId);
		return qo != null && file.getOrCreateTeamData(player).isCompleted(qo);
	}
}
