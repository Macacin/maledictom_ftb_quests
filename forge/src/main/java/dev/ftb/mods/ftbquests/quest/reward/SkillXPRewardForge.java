package dev.ftb.mods.ftbquests.quest.reward;

import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkills;
import dev.ftb.mods.ftbquests.quest.Quest;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.network.NetworkDispatcher;
import daripher.skilltree.network.message.SyncPlayerSkillsMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class SkillXPRewardForge extends dev.ftb.mods.ftbquests.quest.reward.SkillXPReward {
    private static final Logger LOGGER = LogManager.getLogger();

    public SkillXPRewardForge(long id, Quest quest, int amount) {
        super(id, quest, amount);
    }

    public SkillXPRewardForge(long id, Quest quest) {
        super(id, quest);
    }

    // В Forge SkillXPRewardForge.claimForge(ServerPlayer player)
    @Override
    protected void claimForge(ServerPlayer player) {
        LOGGER.info("[SkillXPRewardForge] claimForge started for {}", player.getName().getString());

        player.getCapability(PlayerSkillsProvider.getCapability()).ifPresent(cap -> {
            // Cast к impl для addSkillExperience
            if (cap instanceof PlayerSkills skills) {  // Direct instanceof + cast
                LOGGER.info("[SkillXPRewardForge] PlayerSkills impl obtained; XP before: {}", skills.getSkillExperience());  // Твой getter

                // Add XP (если common рефлексия не добавила; иначе удали)
                skills.addSkillExperience(amount);  // Прямой вызов на impl
                LOGGER.info("[SkillXPRewardForge] addSkillExperience: +{} XP; after: {}", amount, skills.getSkillExperience());
            } else {
                LOGGER.warn("[SkillXPRewardForge] Capability not instanceof PlayerSkills: {}", cap.getClass().getName());
                return;
            }

            // Sync (как раньше)
            SyncPlayerSkillsMessage msg = new SyncPlayerSkillsMessage(player);
            NetworkDispatcher.network_channel.send(PacketDistributor.PLAYER.with(() -> player), msg);
            LOGGER.info("[SkillXPRewardForge] Packet sent; msg XP: ...");
        });

        LOGGER.info("[SkillXPRewardForge] claimForge ended");
    }
}