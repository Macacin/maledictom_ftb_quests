package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class SyncTeamDataMessage extends BaseS2CMessage {
	private final boolean self;
	private final TeamData teamData;

	SyncTeamDataMessage(FriendlyByteBuf buffer) {
		self = buffer.readBoolean();
		teamData = new TeamData(buffer.readUUID(), ClientQuestFile.INSTANCE);
		teamData.read(buffer, self);
	}

	public SyncTeamDataMessage(TeamData teamData, boolean self) {
		this.self = self;
		this.teamData = teamData;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.SYNC_TEAM_DATA;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(self);
		buffer.writeUUID(teamData.getTeamId());
		teamData.write(buffer, self);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuestsNetClient.syncTeamData(self, teamData);
	}
}