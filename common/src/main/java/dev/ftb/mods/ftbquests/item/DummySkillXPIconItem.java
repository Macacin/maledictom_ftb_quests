package dev.ftb.mods.ftbquests.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DummySkillXPIconItem extends Item {
    public DummySkillXPIconItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;  // Без полоски прочности
    }
}