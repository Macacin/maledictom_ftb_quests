package dev.ftb.mods.ftbquests.util;

import com.mojang.util.UUIDTypeAdapter;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record QuestKey(UUID uuid, long id) implements Comparable<QuestKey> {

	public static QuestKey create(UUID uuid, long questObjectId) {
		return new QuestKey(uuid, questObjectId);
	}

	public static QuestKey forReward(UUID uuid, Reward reward) {
		return create(reward.isTeamReward() ? Util.NIL_UUID : uuid, reward.id);
	}

	public static QuestKey fromNetwork(FriendlyByteBuf buf) {
		return create(buf.readUUID(), buf.readLong());
	}

	public static QuestKey fromString(String string) {
		return create(UUIDTypeAdapter.fromString(string.substring(0, 32)), QuestObjectBase.parseCodeString(string.substring(33)));
	}

	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return UUIDTypeAdapter.fromUUID(uuid) + ":" + QuestObjectBase.getCodeString(id);
	}

	@Override
	public int compareTo(@NotNull QuestKey key) {
		int i = uuid.compareTo(key.uuid);
		return i == 0 ? Long.compareUnsigned(id, key.id) : i;
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeUUID(uuid);
		buf.writeLong(id);
	}
}
