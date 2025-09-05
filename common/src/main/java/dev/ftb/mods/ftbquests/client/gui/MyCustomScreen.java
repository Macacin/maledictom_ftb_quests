package dev.ftb.mods.ftbquests.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.BooleanSupplier;

public class MyCustomScreen extends Screen {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/my_background.png");
    private static final ResourceLocation[] BUTTON_CLOSED = {
            new ResourceLocation("ftbquests", "textures/gui/main_button_0.png"),
            new ResourceLocation("ftbquests", "textures/gui/main_button_1.png"),
            new ResourceLocation("ftbquests", "textures/gui/main_button_2.png"),
            new ResourceLocation("ftbquests", "textures/gui/main_button_3.png"),
            new ResourceLocation("ftbquests", "textures/gui/main_button_4.png")
    };
    private static final ResourceLocation BUTTON_OPEN = new ResourceLocation("ftbquests", "textures/gui/button_open.png");
    private static final ResourceLocation BUTTON_ICON = new ResourceLocation("ftbquests", "textures/gui/button_execute.png");
    private static final ResourceLocation BUTTON_LOCKED = new ResourceLocation("ftbquests", "textures/gui/button_locked.png");

    private static final ResourceLocation ANIM1_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/animation1.png");
    private static final ResourceLocation ANIM2_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/animation2.png");
    private static final ResourceLocation ANIM3_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/animation3.png");
    private static final ResourceLocation ANIM4_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/animation4.png");

    public static final String[] GROUP_NAMES = {
            "&lСкитания",
            "&k&lПроклятый",
            "&lЧародейство",
            "&lПромышленная революция",
            "&lИспытание на прочность"
    };

    private enum Phase { ANIM1, ANIM2, ANIM3, ANIM4, MAIN }
    private Phase phase = Phase.ANIM1;
    private long animStartTime;
    private static final int ANIM1_FRAMES = 24;
    private static final int ANIM1_DURATION_MS = 700;
    private static final int ANIM1_FRAME_HEIGHT = 512;
    private static final int ANIM2_FRAMES = 24;
    private static final int ANIM2_DURATION_MS = 700;
    private static final int ANIM2_FRAME_HEIGHT = 512;
    private static final int ANIM3_FRAMES = 24;
    private static final int ANIM3_DURATION_MS = 700;
    private static final int ANIM3_FRAME_HEIGHT = 512;
    private static final int ANIM4_FRAMES = 8;
    private static final int ANIM4_DURATION_MS = 222;
    private static final int ANIM4_FRAME_HEIGHT = 512;

    public static final int[] GROUP_LIMITS = {5, 2, 1, 3, 2};
    private static final int START_X = 190;
    private static final int START_Y = 180;
    private static final int BUTTON_WIDTH = 95;
    private static final int BUTTON_HEIGHT = 15;
    private static final int SUBBUTTON_VERTICAL_SPACING = 32;
    private static final int GROUP_VERTICAL_SPACING = 16;

    public final Component title;
    private final List<ButtonGroup> groups = new ArrayList<>();
    private final List<Integer> progressPercentPerButton = new ArrayList<>();
    private final List<Integer> progressPercentPerGroup = new ArrayList<>();
    private final List<Boolean> groupOpenStates = new ArrayList<>();

    public MyCustomScreen(Component title, boolean playAnimation) {
        super(title);
        this.title = title;
        this.animStartTime = System.currentTimeMillis();
        this.phase = playAnimation ? Phase.ANIM1 : Phase.MAIN;
    }

    public MyCustomScreen(Component title) {
        this(title, true);  // По умолчанию — с анимацией
    }

    @Override
    protected void init() {
        groups.clear();
        progressPercentPerButton.clear();
        progressPercentPerGroup.clear();
        if (phase == Phase.MAIN) {
            initMainContent();
            return;
        }
        phase = Phase.ANIM1;
        animStartTime = System.currentTimeMillis();
    }

    private void initMainContent() {
        if (!ClientQuestFile.exists()) return;

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        List<Chapter> allChapters = file.getAllChapters();
        int backgroundX = (this.width - 256) / 2 + 20;
        int backgroundY = (this.height - 256) / 2 - 25;

        int chapterIndex = 0;
        int currentY = backgroundY + 90;

        for (int groupNum = 0; groupNum < GROUP_LIMITS.length; groupNum++) {
            int limit = GROUP_LIMITS[groupNum];
            int remainingChapters = allChapters.size() - chapterIndex;
            int subCount = Math.min(limit, remainingChapters);

            int startX = backgroundX + 55; // отступ слева внутри фона
            createGroup(startX, currentY, subCount, groupNum);
            ButtonGroup group = groups.get(groups.size() - 1);

            for (int i = 0; i < subCount; i++) {
                Chapter chapter = allChapters.get(chapterIndex);
                group.subButtons.get(i).setMessage(Component.literal(chapter.getTitle().getString()));
                chapterIndex++;
            }

            progressPercentPerGroup.add(0);
            currentY += 60;
        }

        for (int i = 0; i < groups.size(); i++) {
            boolean openState = i < groupOpenStates.size() ? groupOpenStates.get(i) : false;
            groups.get(i).setOpen(openState);
        }

        recalcPositions();


        InvisibleButton backBtn = new InvisibleButton(
                backgroundX + 5,
                backgroundY + 150,
                15,
                15,
                b -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
                    }
                }
        );
        this.addRenderableWidget(backBtn);

        int baseX = backgroundX + 167 + 5; // уже правильно
        int[] yOffsets = {70, 106, 137, 164, 190};

        ResourceLocation[] buttonTexturesNormal = new ResourceLocation[]{
                new ResourceLocation("ftbquests", "textures/gui/zaplatka_1_1.png"),
                new ResourceLocation("ftbquests", "textures/gui/zaplatka_2_1.png"),
                new ResourceLocation("ftbquests", "textures/gui/zaplatka_3_1.png"),
                new ResourceLocation("ftbquests", "textures/gui/zaplatka_4_1.png"),
                new ResourceLocation("ftbquests", "textures/gui/zaplatka_5_1.png")
        };

        ResourceLocation[] buttonTexturesHover = new ResourceLocation[]{
                new ResourceLocation("ftbquests", "textures/gui/zaplatka_1_2.png"),
                new ResourceLocation("ftbquests", "textures/gui/zaplatka_2_2.png"),
                new ResourceLocation("ftbquests", "textures/gui/zaplatka_3_2.png"),
                new ResourceLocation("ftbquests", "textures/gui/zaplatka_4_2.png"),
                new ResourceLocation("ftbquests", "textures/gui/zaplatka_5_2.png")
        };

        for (int i = 0; i < 5; i++) {
            int idx = i;
            if (idx >= groups.size()) continue;
            int extraX = 0;
            if (i == 3) {
                extraX = 3;
            }
            if (i == 4) {
                extraX = 5;
            }

            PatchButton groupBtn = new PatchButton(
                    baseX + i / 2 + extraX,
                    backgroundY + yOffsets[i] + 21,
                    10, 12,
                    Component.empty(),
                    b -> {
                        Minecraft.getInstance().setScreen(
                                new GroupDetailScreen(parseLegacyFormatting(GROUP_NAMES[idx]), idx)
                        );
                    },
                    buttonTexturesNormal[i],
                    buttonTexturesHover[i]
            );
            this.addRenderableWidget(groupBtn);
        }
    }

    private void createGroup(int x, int y, int subCount, int groupIndex) {
        ButtonGroup group = new ButtonGroup(x, y, subCount, () -> true, groupIndex);
        groups.add(group);
        group.addToScreen(this);

        for (int i = 0; i < subCount; i++) {
            progressPercentPerButton.add(0);
        }
    }

    private void recalcPositions() {
        int backgroundY = (this.height - 256) / 2 - 25;
        int y = backgroundY + 86;
        for (ButtonGroup g : groups) {
            g.setPositionY(y);
            int totalHeight = g.mainButton.getHeight();
            if (g.isOpen()) totalHeight += g.getVisibleSubButtonsCount() * SUBBUTTON_VERTICAL_SPACING;
            y += totalHeight + GROUP_VERTICAL_SPACING;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - animStartTime;

        if (phase == Phase.ANIM1) {
            this.renderBackground(graphics);
            renderAnimation(graphics, ANIM1_TEXTURE, ANIM1_FRAMES, ANIM1_DURATION_MS, ANIM1_FRAME_HEIGHT);
            if (elapsed >= ANIM1_DURATION_MS) { phase = Phase.ANIM2; animStartTime = currentTime; }
            return;
        } else if (phase == Phase.ANIM2) {
            this.renderBackground(graphics);
            renderAnimation(graphics, ANIM2_TEXTURE, ANIM2_FRAMES, ANIM2_DURATION_MS, ANIM2_FRAME_HEIGHT);
            if (elapsed >= ANIM2_DURATION_MS) { phase = Phase.ANIM3; animStartTime = currentTime; }
            return;

        } else if (phase == Phase.ANIM3) {
            this.renderBackground(graphics);
            renderAnimation(graphics, ANIM3_TEXTURE, ANIM3_FRAMES, ANIM3_DURATION_MS, ANIM3_FRAME_HEIGHT);
            if (elapsed >= ANIM3_DURATION_MS) { phase = Phase.ANIM4; animStartTime = currentTime; }
            return;
        } else if (phase == Phase.ANIM4) {
            this.renderBackground(graphics);
            renderAnimation(graphics, ANIM4_TEXTURE, ANIM4_FRAMES, ANIM4_DURATION_MS, ANIM4_FRAME_HEIGHT);
            if (elapsed >= ANIM4_DURATION_MS) { phase = Phase.MAIN; initMainContent(); animStartTime = currentTime; }
            return;
        }

        this.renderBackground(graphics);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);

        int texWidth = 256;
        int texHeight = 256;
        int baseX = (this.width - texWidth) / 2 + 20;
        int baseY = (this.height - texHeight) / 2 - 25;
        graphics.blit(BACKGROUND_TEXTURE, baseX, baseY, 0, 0, texWidth, texHeight, texWidth, texHeight);

        updatePinnedQuestProgress();
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void updatePinnedQuestProgress() {
        if (!ClientQuestFile.exists()) { progressPercentPerButton.clear(); progressPercentPerGroup.clear(); return; }

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        TeamData data = file.selfTeamData;
        List<Chapter> allChapters = file.getAllChapters();
        int globalChapterIndex = 0;
        int progressIndex = 0;

        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            ButtonGroup group = groups.get(groupIndex);
            int groupSum = 0;
            int groupCount = 0;

            for (TexturedButton subBtn : group.subButtons) {
                if (globalChapterIndex >= allChapters.size()) break;
                Chapter chapter = allChapters.get(globalChapterIndex++);
                int sum = 0, count = 0;
                for (Quest quest : chapter.getQuests()) {
                    if (!data.canStartTasks(quest)) continue;
                    sum += Math.min(data.getRelativeProgress(quest), 100);
                    count++;
                }
                int progress = count > 0 ? sum / count : 0;
                setProgress(progressIndex++, progress);

                groupSum += progress;
                groupCount++;
            }

            int groupProgress = groupCount > 0 ? groupSum / groupCount : 0;
            setGroupProgress(groupIndex, groupProgress);
        }
    }

    private void setProgress(int index, int value) {
        if (index >= progressPercentPerButton.size()) progressPercentPerButton.add(value);
        else progressPercentPerButton.set(index, value);
    }

    private void setGroupProgress(int groupIndex, int value) {
        while (progressPercentPerGroup.size() <= groupIndex) progressPercentPerGroup.add(0);
        progressPercentPerGroup.set(groupIndex, value);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private class ButtonGroup {
        private final TexturedButton mainButton;
        private final List<TexturedButton> subButtons = new ArrayList<>();
        private final BooleanSupplier visibleCondition;
        private boolean open = false;
        private final int groupIndex;

        ButtonGroup(int x, int y, int subCount, BooleanSupplier visibleCondition, int groupIndex) {
            this.visibleCondition = visibleCondition;
            this.groupIndex = groupIndex;

            MutableComponent groupTitle = parseLegacyFormatting(GROUP_NAMES[groupIndex]);
            this.mainButton = new TexturedButton(
                    x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                    groupTitle,
                    b -> Minecraft.getInstance().setScreen(new GroupDetailScreen(
                            parseLegacyFormatting(GROUP_NAMES[groupIndex]), groupIndex
                    )),
                    BUTTON_CLOSED[groupIndex],
                    BUTTON_OPEN,
                    BUTTON_LOCKED,
                    false
            );

            for (int i = 0; i < subCount; i++) {
                TexturedButton sub = new TexturedButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.literal("Подкнопка " + (i + 1)),
                        btn -> {},
                        BUTTON_ICON,
                        BUTTON_ICON,
                        BUTTON_LOCKED,
                        false
                );
                sub.visible = false;
                subButtons.add(sub);
            }
        }

        void addToScreen(MyCustomScreen screen) {
            screen.addRenderableWidget(mainButton);
            subButtons.forEach(screen::addRenderableWidget);
        }

        void setPositionY(int y) {
            mainButton.setY(y);
            int currentY = y + mainButton.getHeight() + 10;
            for (TexturedButton sub : subButtons) {
                sub.setY(currentY);
                currentY += SUBBUTTON_VERTICAL_SPACING;
            }
        }

        int getVisibleSubButtonsCount() {
            return visibleCondition.getAsBoolean() && open ? subButtons.size() : 0;
        }

        boolean isOpen() { return open; }
        void setOpen(boolean open) { this.open = open; }
    }

    private class PatchButton extends Button {
        private final ResourceLocation textureNormal;
        private final ResourceLocation textureHover;

        private float progress = 0f;
        private final int baseWidth, baseHeight;
        private final int hoverWidth, hoverHeight;
        private final int baseX, baseY;

        private float renderX, renderY;
        private float renderWidth, renderHeight;

        public PatchButton(int x, int y, int width, int height, Component title, OnPress onPress,
                           ResourceLocation normal, ResourceLocation hover) {
            super(x, y, width, height, title, onPress, DEFAULT_NARRATION);
            this.textureNormal = normal;
            this.textureHover = hover;
            this.baseWidth = 16;
            this.baseHeight = 12;
            this.hoverWidth = 16;
            this.hoverHeight = 12;
            this.baseX = x;
            this.baseY = y;

            this.renderX = x;
            this.renderY = y;
            this.renderWidth = width;
            this.renderHeight = height;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            boolean hovered = isHoveredOrFocused();

            int targetWidth = hovered ? hoverWidth : baseWidth;
            int targetHeight = hovered ? hoverHeight : baseHeight;

            renderWidth += (targetWidth - renderWidth) * 0.2f;
            renderHeight += (targetHeight - renderHeight) * 0.2f;

            renderX += (baseX - renderX) * 0.2f;
            renderY += (baseY - renderY) * 0.2f;

            ResourceLocation tex = hovered ? textureHover : textureNormal;
            RenderSystem.setShaderTexture(0, tex);

            graphics.blit(tex, Math.round(renderX), Math.round(renderY), 0, 0,
                    Math.round(renderWidth), Math.round(renderHeight),
                    Math.round(renderWidth), Math.round(renderHeight));
        }
    }

    private class TexturedButton extends Button {
        private final ResourceLocation textureClosed;
        private final ResourceLocation textureOpen;
        private final ResourceLocation textureLocked;
        private boolean open = false;
        private boolean locked = false;
        private float scale = 1f;

        public TexturedButton(int x, int y, int width, int height, Component title, OnPress onPress,
                              ResourceLocation textureClosed, ResourceLocation textureOpen, ResourceLocation textureLocked,
                              boolean locked) {
            super(x, y, width, height, title, onPress, DEFAULT_NARRATION);
            this.textureClosed = textureClosed;
            this.textureOpen = textureOpen;
            this.textureLocked = textureLocked;
            this.locked = locked;
            this.active = !locked;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            boolean hovered = isHoveredOrFocused();
            float targetScale = hovered ? 1.1f : 1f;
            scale += (targetScale - scale) * 0.2f;

            // ==== КНОПКА ====
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f, 0);
            guiGraphics.pose().scale(scale, scale, 1f);
            guiGraphics.pose().translate(-this.width / 2f, -this.height / 2f, 0);

            ResourceLocation tex = locked ? textureLocked : (open ? textureOpen : textureClosed);
            RenderSystem.setShaderTexture(0, tex);
            guiGraphics.blit(tex, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            guiGraphics.pose().popPose();


            // ==== ТЕКСТ ====
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f, 0);
            guiGraphics.pose().scale(0.60f, 0.60f, 1f); // текст в 2 раза меньше
            guiGraphics.pose().translate(-this.width / 2f, -this.height / 2f, 0);

            int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
            int textWidth = Minecraft.getInstance().font.width(this.getMessage());
            int textX = (this.width - textWidth) / 2;
            int textY = (this.height - 8) / 2;

            guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage(), textX, textY, textColor);
            guiGraphics.pose().popPose();
        }
    }

    private class InvisibleButton extends Button {
        private float scale = 1f;
        private final int hoverColor = 0x80FFFFFF;

        public InvisibleButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            boolean hovered = isHoveredOrFocused();

            float targetScale = hovered ? 1.1f : 1f;
            scale += (targetScale - scale) * 0.4f;

            graphics.pose().pushPose();
            graphics.pose().translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f, 0);
            graphics.pose().scale(scale, scale, 1f);
            graphics.pose().translate(-this.width / 2f, -this.height / 2f, 0);

            if (hovered) {
                graphics.fill(0, 0, this.width, this.height, hoverColor);
            }

            graphics.pose().popPose();
        }
    }

    private void renderAnimation(GuiGraphics graphics, ResourceLocation texture, int frames, int durationMs, int frameHeight) {
        long elapsed = System.currentTimeMillis() - animStartTime;
        int frame = (int) ((elapsed * frames) / durationMs);
        frame = Math.min(frame, frames - 1);
        int texWidth = 256;
        int texHeight = frameHeight * frames;
        int posX = Math.floorDiv(this.width - texWidth, 2) + 20;
        int posY = Math.floorDiv(this.height - frameHeight, 2) + 100;

        RenderSystem.setShaderTexture(0, texture);
        graphics.blit(texture, posX, posY, 0, frame * frameHeight, texWidth, frameHeight, texWidth, texHeight);
    }

    public static MutableComponent parseLegacyFormatting(String text) {
        MutableComponent component = Component.literal("");
        StringBuilder currentText = new StringBuilder();
        Style currentStyle = Style.EMPTY;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                ChatFormatting formatting = ChatFormatting.getByCode(code);
                if (formatting != null) {
                    if (!currentText.isEmpty()) {
                        component.append(Component.literal(currentText.toString()).withStyle(currentStyle));
                        currentText.setLength(0);
                    }
                    if (formatting == ChatFormatting.RESET) currentStyle = Style.EMPTY;
                    else currentStyle = currentStyle.applyFormat(formatting);
                    i++;
                    continue;
                }
            }
            currentText.append(c);
        }

        if (!currentText.isEmpty()) {
            component.append(Component.literal(currentText.toString()).withStyle(currentStyle));
        }
        return component;
    }
}
