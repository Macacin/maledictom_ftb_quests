package dev.ftb.mods.ftbquests.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TexturedButton extends Button {
    private final ResourceLocation textureOpen;
    private final ResourceLocation textureClosed;
    private final int texU, texV, texWidth, texHeight;

    private boolean open = false; // текущее состояние, которое меняет текстуру

    public TexturedButton(int x, int y, int width, int height, Component message, OnPress onPress,
                          ResourceLocation textureClosed, ResourceLocation textureOpen,
                          int texU, int texV, int texWidth, int texHeight) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.textureClosed = textureClosed;
        this.textureOpen = textureOpen;
        this.texU = texU;
        this.texV = texV;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        ResourceLocation currentTexture = open ? textureOpen : textureClosed;
        RenderSystem.setShaderTexture(0, currentTexture);
        graphics.blit(currentTexture, getX(), getY(), texU, texV, width, height, texWidth, texHeight);

        int textColor = 0xFFE600AA;
        int textX = getX() + width / 2 - Minecraft.getInstance().font.width(getMessage()) / 2;
        int textY = getY() + (height - 8) / 2;
        graphics.drawString(Minecraft.getInstance().font, getMessage(), textX, textY, textColor, false);
    }
}