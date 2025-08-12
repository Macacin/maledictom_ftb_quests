package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MoveChapterResponseMessage extends BaseS2CMessage {
	private final long id;
	private final boolean up;

	MoveChapterResponseMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		up = buffer.readBoolean();
	}

	public MoveChapterResponseMessage(long i, boolean u) {
		id = i;
		up = u;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.MOVE_CHAPTER_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeBoolean(up);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuestsNetClient.moveChapter(id, up);
	}
}