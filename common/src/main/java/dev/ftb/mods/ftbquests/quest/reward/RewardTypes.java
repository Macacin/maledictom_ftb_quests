package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.item.DummySkillXPIconItem;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static dev.ftb.mods.ftbquests.item.FTBQuestsItems.SKILL_XP_ICON;

public interface RewardTypes {
	Map<ResourceLocation, RewardType> TYPES = new LinkedHashMap<>();

	static RewardType register(ResourceLocation name, RewardType.Provider p, Supplier<Icon> i) {
		return TYPES.computeIfAbsent(name, id -> new RewardType(id, p, i));
	}

	RewardType ITEM = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "item"), ItemReward::new,
			() -> Icon.getIcon("minecraft:item/diamond"));
	RewardType CHOICE = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "choice"), ChoiceReward::new,
			() -> Icons.COLOR_RGB).setExcludeFromListRewards(true);
	RewardType ALL_TABLE = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "all_table"), AllTableReward::new,
			() -> Icons.COLOR_HSB).setExcludeFromListRewards(true);
	RewardType RANDOM = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "random"), RandomReward::new,
			() -> Icons.DICE).setExcludeFromListRewards(true);
	RewardType LOOT = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "loot"), LootReward::new,
			() -> Icons.MONEY_BAG).setExcludeFromListRewards(true);
	RewardType COMMAND = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "command"), CommandReward::new,
			() -> Icon.getIcon("minecraft:block/command_block_back"));
	RewardType CUSTOM = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "custom"), CustomReward::new,
			() -> Icons.COLOR_HSB);
	RewardType XP = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "xp"), XPReward::new,
			() -> Icon.getIcon("minecraft:item/experience_bottle"));
	RewardType XP_LEVELS = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "xp_levels"), XPLevelsReward::new,
			() -> Icon.getIcon("minecraft:item/experience_bottle"));
	RewardType ADVANCEMENT = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "advancement"), AdvancementReward::new,
			() -> Icon.getIcon("minecraft:item/wheat"));
	RewardType TOAST = register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "toast"), ToastReward::new,
			() -> Icon.getIcon("minecraft:item/oak_sign"));
	RewardType STAGE = RewardTypes.register(new ResourceLocation(FTBQuestsAPI.MOD_ID, "gamestage"), StageReward::new,
			() -> Icons.CONTROLLER);
	RewardType SKILL_XP = register(new ResourceLocation("skilltree", "skill_xp"),
			SkillXPReward::new,
			() -> ItemIcon.getIcon("ftbquests:item/skill_xp_icon"));

	static void init() {
	}
}
