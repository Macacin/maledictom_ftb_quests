package dev.ftb.mods.ftbquests.quest.reward;

import daripher.skilltree.capability.skill.IPlayerSkills;
import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.net.DisplayRewardToastMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

public class SkillXPReward extends Reward {
    private static final Logger LOGGER = LogManager.getLogger();
    protected int amount;

    public SkillXPReward(long id, Quest quest, int amount) {
        super(id, quest);
        this.amount = amount;
    }

    public SkillXPReward(long id, Quest quest) {
        this(id, quest, 10);
    }

    @Override
    public RewardType getType() {
        return RewardTypes.SKILL_XP;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putInt("amount", amount);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        amount = nbt.getInt("amount");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarInt(amount);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        amount = buffer.readVarInt();
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addInt("amount", amount, v -> amount = v, 100, 1, Integer.MAX_VALUE)
                .setNameKey("skilltree.reward.skill_xp");
    }

    // В common SkillXPReward.claim(ServerPlayer player, boolean notify)
    @Override
    public void claim(ServerPlayer player, boolean notify) {
        LOGGER.info("[SkillXPReward] Claim started for {}: amount={}, notify={}", player.getName().getString(), amount, notify);

        // Рефлексия для XP
        if (Platform.isForge()) {
            try {
                LOGGER.info("[SkillXPReward] Attempting reflection add XP for {}", player.getName().getString());

                // Получи IPlayerSkills (как раньше)
                Class<?> providerClass = Class.forName("daripher.skilltree.capability.skill.PlayerSkillsProvider");
                IPlayerSkills skills = (IPlayerSkills) providerClass.getMethod("get", net.minecraft.world.entity.player.Player.class).invoke(null, player);
                LOGGER.info("[SkillXPReward] IPlayerSkills obtained: {}", skills);

                // Теперь рефлексия на impl-класс PlayerSkills
                Class<?> implClass = Class.forName("daripher.skilltree.capability.skill.PlayerSkills");
                Method addMethod = implClass.getMethod("addSkillExperience", int.class);  // Или "addExperience" — твое реальное имя
                addMethod.invoke(skills, amount);  // skills — instance of PlayerSkills, так что OK
                LOGGER.info("[SkillXPReward] Reflection addSkillExperience invoked on PlayerSkills: +{} XP", amount);

                // Опционально: Проверь XP после (через рефлексию getter, если нужно)
                Method getXPMethod = implClass.getMethod("getSkillExperience");  // Твой getter
                int newXP = (int) getXPMethod.invoke(skills);
                LOGGER.info("[SkillXPReward] XP after add: {}", newXP);

            } catch (Exception e) {
                LOGGER.error("[SkillXPReward] Reflection failed for {}: {}", player.getName().getString(), e.getMessage(), e);
                player.giveExperiencePoints(amount);
                LOGGER.warn("[SkillXPReward] Fallback to vanilla XP");
            }
        } else {
            LOGGER.info("[SkillXPReward] Non-Forge: vanilla XP +{}", amount);
            player.giveExperiencePoints(amount);
        }

        // Platform sync
        if (Platform.isForge()) {
            LOGGER.info("[SkillXPReward] Calling claimForge for {}", player.getName().getString());
            claimForge(player);
            LOGGER.info("[SkillXPReward] claimForge completed for {}", player.getName().getString());
        }

        // Тост
        if (notify) {
            LOGGER.debug("[SkillXPReward] Sending toast for {}", player.getName().getString());
            // ... тост код
        }

        LOGGER.info("[SkillXPReward] Claim ended for {}", player.getName().getString());
    }

    // Protected для override в platform (пустой в common)
    protected void claimForge(ServerPlayer player) {
        // Override в Forge для sync
    }

    @Override
    public MutableComponent getAltTitle() {
        return Component.literal("Skill XP").append(": ").append(Component.literal("+" + amount).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public String getButtonText() {
        return "+" + amount;
    }
}