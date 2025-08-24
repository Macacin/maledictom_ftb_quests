package dev.ftb.mods.ftbquests.quest;

import com.mojang.util.UUIDTypeAdapter;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.net.*;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import dev.ftb.mods.ftbquests.util.FileUtils;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLoggedInAfterTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

public class ServerQuestFile extends BaseQuestFile {
	public static final LevelResource FTBQUESTS_DATA = new LevelResource("ftbquests");

	public static ServerQuestFile INSTANCE;

	public final MinecraftServer server;
	private boolean shouldSave;
	private boolean isLoading;
	private Path folder;
	private ServerPlayer currentPlayer = null;

	public ServerQuestFile(MinecraftServer s) {
		server = s;
		shouldSave = false;
		isLoading = false;

		int taskTypeId = 0;
		for (TaskType type : TaskTypes.TYPES.values()) {
			type.internalId = ++taskTypeId;
			taskTypeIds.put(type.internalId, type);
		}

		int rewardTypeId = 0;
		for (RewardType type : RewardTypes.TYPES.values()) {
			type.intId = ++rewardTypeId;
			rewardTypeIds.put(type.intId, type);
		}
	}

	public void load() {
		folder = Platform.getConfigFolder().resolve("ftbquests/quests");

		// --- Копируем кастомные конфиги, если их нет ---
		copyCustomConfigs();

		if (Files.exists(folder)) {
			FTBQuests.LOGGER.info("Loading quests from " + folder);
			isLoading = true;
			readDataFull(folder);
			isLoading = false;
		}

		Path path = server.getWorldPath(FTBQUESTS_DATA);

		if (Files.exists(path)) {
			try (Stream<Path> s = Files.list(path)) {
				s.filter(p -> p.getFileName().toString().contains("-") && p.getFileName().toString().endsWith(".snbt"))
						.forEach(path1 -> {
							SNBTCompoundTag nbt = SNBT.read(path1);
							if (nbt != null) {
								try {
									UUID uuid = UUIDTypeAdapter.fromString(nbt.getString("uuid"));
									TeamData data = new TeamData(uuid, this);
									addData(data, true);
									data.deserializeNBT(nbt);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void copyCustomConfigs() {
		Path chaptersFolder = folder.resolve("chapters");

		// Пример: копируем my_custom_chapter.snbt из ресурсов в папку, если там нет файла
		Path a1 = chaptersFolder.resolve("1EB570174CB7CA6D.snbt");
		Path a2 = chaptersFolder.resolve("1ECD5960277CF13D.snbt");
		Path a3 = chaptersFolder.resolve("2E237036B2AE62A0.snbt");
		Path a4 = chaptersFolder.resolve("4E019C588A7FFFBB.snbt");
		Path a5 = chaptersFolder.resolve("05B5305DDDE03163.snbt");
		Path a6 = chaptersFolder.resolve("6BD3657B08E60209.snbt");
		Path a7 = chaptersFolder.resolve("6F54CED2221D09F0.snbt");
		Path a8 = chaptersFolder.resolve("25C528B87CD22003.snbt");
		Path a9 = chaptersFolder.resolve("033F29CC7BA1CCC2.snbt");
		Path a10 = chaptersFolder.resolve("41DB200F53BA483D.snbt");
		Path a11 = chaptersFolder.resolve("0489DB002B84BF75.snbt");
		Path a12 = chaptersFolder.resolve("1076F85C2EECAC56.snbt");
		Path a13 = chaptersFolder.resolve("07194C3D3A6194CA.snbt");

		if (Files.notExists(a1)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/1EB570174CB7CA6D.snbt", a1);
		}
		if (Files.notExists(a2)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/1ECD5960277CF13D.snbt", a2);
		}
		if (Files.notExists(a3)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/2E237036B2AE62A0.snbt", a3);
		}
		if (Files.notExists(a4)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/4E019C588A7FFFBB.snbt", a4);
		}
		if (Files.notExists(a5)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/05B5305DDDE03163.snbt", a5);
		}
		if (Files.notExists(a6)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/6BD3657B08E60209.snbt", a6);
		}
		if (Files.notExists(a7)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/6F54CED2221D09F0.snbt", a7);
		}
		if (Files.notExists(a8)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/25C528B87CD22003.snbt", a8);
		}
		if (Files.notExists(a9)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/033F29CC7BA1CCC2.snbt", a9);
		}
		if (Files.notExists(a10)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/41DB200F53BA483D.snbt", a10);
		}
		if (Files.notExists(a11)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/0489DB002B84BF75.snbt", a11);
		}
		if (Files.notExists(a12)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/1076F85C2EECAC56.snbt", a12);
		}
		if (Files.notExists(a13)) {
			try {
				Files.createDirectories(chaptersFolder);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("Failed to create directories for chapters folder", e);
				return;
			}
			copyResourceToFile("/data/ftbquests/quests/chapters/07194C3D3A6194CA.snbt", a13);
		}

	}

	private void copyResourceToFile(String resourcePath, Path targetFile) {
		try (InputStream is = ServerQuestFile.class.getResourceAsStream(resourcePath)) {
			if (is == null) {
				FTBQuests.LOGGER.warn("Resource not found: " + resourcePath);
				return;
			}
			Files.copy(is, targetFile);
			FTBQuests.LOGGER.info("Copied default config file to " + targetFile);
		} catch (IOException e) {
			FTBQuests.LOGGER.error("Failed to copy config file " + targetFile, e);
		}
	}

	// --- остальной код класса (уже без изменений) ---

	@Override
	public Env getSide() {
		return Env.SERVER;
	}

	@Override
	public boolean isLoading() {
		return isLoading;
	}

	@Override
	public Path getFolder() {
		return folder;
	}

	@Override
	public void deleteObject(long id) {
		QuestObjectBase object = getBase(id);
		if (object != null) {
			object.deleteChildren();
			object.deleteSelf();
			refreshIDMap();
			markDirty();
			object.getPath().ifPresent(path -> FileUtils.delete(getFolder().resolve(path).toFile()));
		}
		new DeleteObjectResponseMessage(id).sendToAll(server);
	}

	@Override
	public void markDirty() {
		shouldSave = true;
	}

	public void saveNow() {
		if (shouldSave) {
			writeDataFull(getFolder());
			shouldSave = false;
		}
		getAllTeamData().forEach(TeamData::saveIfChanged);
	}

	public void unload() {
		saveNow();
		deleteChildren();
		deleteSelf();
	}

	public ServerPlayer getCurrentPlayer() {
		return currentPlayer;
	}

	public void withPlayerContext(ServerPlayer player, Runnable toDo) {
		currentPlayer = player;
		try {
			toDo.run();
		} finally {
			currentPlayer = null;
		}
	}

	public void playerLoggedIn(PlayerLoggedInAfterTeamEvent event) {
		ServerPlayer player = event.getPlayer();
		TeamData data = getOrCreateTeamData(event.getTeam());

		new SyncQuestsMessage(this).sendTo(player);
		new SyncEditorPermissionMessage(PermissionsHelper.hasEditorPermission(player, false)).sendTo(player);
		player.inventoryMenu.addSlotListener(new FTBQuestsInventoryListener(player));

		if (!data.isLocked()) {
			withPlayerContext(player, () -> forAllQuests(quest -> {
				if (!data.isCompleted(quest) && quest.isCompletedRaw(data)) {
					quest.onCompleted(new QuestProgressEventData<>(new Date(), data, quest, data.getOnlineMembers(), Collections.singletonList(player)));
				}
				data.checkAutoCompletion(quest);
				if (data.canStartTasks(quest)) {
					quest.getTasks().stream().filter(Task::checkOnLogin).forEach(task -> task.submitTask(data, player));
				}
			}));
		}
	}

	public void teamCreated(TeamCreatedEvent event) {
		UUID id = event.getTeam().getId();
		TeamData data = teamDataMap.computeIfAbsent(id, k -> {
			TeamData newTeamData = new TeamData(id, this);
			newTeamData.markDirty();
			return newTeamData;
		});
		data.setName(event.getTeam().getShortName());
		addData(data, false);

		if (event.getTeam() instanceof PartyTeam) {
			FTBTeamsAPI.api().getManager().getPlayerTeamForPlayerID(event.getCreator().getUUID()).ifPresent(playerTeam -> {
				TeamData oldTeamData = getOrCreateTeamData(playerTeam);
				data.copyData(oldTeamData);
			});
		}
		TeamDataUpdate self = new TeamDataUpdate(data);
		new CreateOtherTeamDataMessage(self).sendToAll(server);
	}

	public void playerChangedTeam(PlayerChangedTeamEvent event) {
		event.getPreviousTeam().ifPresent(prevTeam -> {
			Team curTeam = event.getTeam();
			TeamData oldTeamData = getOrCreateTeamData(prevTeam);
			TeamData newTeamData = getOrCreateTeamData(curTeam);

			if (prevTeam.isPlayerTeam() && curTeam.isPartyTeam() && !curTeam.getOwner().equals(event.getPlayerId())) {
				newTeamData.mergeData(oldTeamData);
			} else if (prevTeam.isPartyTeam() && curTeam.isPlayerTeam()) {
				newTeamData.mergeClaimedRewards(oldTeamData);
			}

			new TeamDataChangedMessage(new TeamDataUpdate(oldTeamData), new TeamDataUpdate(newTeamData)).sendToAll(server);
			new SyncTeamDataMessage(newTeamData, true).sendTo(curTeam.getOnlineMembers());
		});
	}

	@Override
	public boolean isPlayerOnTeam(Player player, TeamData teamData) {
		return FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID())
				.map(team -> team.getTeamId().equals(teamData.getTeamId()))
				.orElse(false);
	}

	@Override
	public boolean moveChapterGroup(long id, boolean movingUp) {
		if (super.moveChapterGroup(id, movingUp)) {
			markDirty();
			clearCachedData();
			new MoveChapterGroupResponseMessage(id, movingUp).sendToAll(server);
			return true;
		}
		return false;
	}
}