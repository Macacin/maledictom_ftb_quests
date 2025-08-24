package dev.ftb.mods.ftbquests.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailScreen extends Screen {

    // === Текстуры интерфейса ===
    private static final ResourceLocation BUTTON_CLOSED = new ResourceLocation("ftbquests", "textures/gui/button_closed.png");
    private static final ResourceLocation BUTTON_OPEN = new ResourceLocation("ftbquests", "textures/gui/button_open.png");
    private static final ResourceLocation BUTTON_LOCKED = new ResourceLocation("ftbquests", "textures/gui/button_locked.png");

    private static final ResourceLocation GROUP_PROGRESS_FRAME = new ResourceLocation("ftbquests", "textures/gui/group_pr.png");
    private static final ResourceLocation CHAPTER_PROGRESS_FRAME = new ResourceLocation("ftbquests", "textures/gui/glav_pr.png");
    private static final ResourceLocation PROGRESS_FILL = new ResourceLocation("ftbquests", "textures/gui/zapoln.png");
    private static final ResourceLocation PROGRESS_FILL_FULL = new ResourceLocation("ftbquests", "textures/gui/zapoln_full.png");

    // === Фоны для разных групп ===
    private static final ResourceLocation[] BACKGROUNDS = new ResourceLocation[]{
            new ResourceLocation("ftbquests", "textures/gui/blacknot2.png"),
            new ResourceLocation("ftbquests", "textures/gui/blacknot3.png"),
            new ResourceLocation("ftbquests", "textures/gui/blacknot4.png"),
            new ResourceLocation("ftbquests", "textures/gui/blacknot5.png"),
            new ResourceLocation("ftbquests", "textures/gui/blacknot6.png"),
    };

    // === Текстуры кнопок групп (справа) ===
    private static final ResourceLocation[] GROUP_BUTTON_NORMAL = new ResourceLocation[]{
            new ResourceLocation("ftbquests", "textures/gui/zaplatka_1_1.png"),
            new ResourceLocation("ftbquests", "textures/gui/zaplatka_2_1.png"),
            new ResourceLocation("ftbquests", "textures/gui/zaplatka_3_1.png"),
            new ResourceLocation("ftbquests", "textures/gui/zaplatka_4_1.png"),
            new ResourceLocation("ftbquests", "textures/gui/zaplatka_5_1.png"),
    };

    private static final ResourceLocation[] GROUP_BUTTON_HOVER = new ResourceLocation[]{
            new ResourceLocation("ftbquests", "textures/gui/zaplatka_1_2.png"),
            new ResourceLocation("ftbquests", "textures/gui/zaplatka_2_2.png"),
            new ResourceLocation("ftbquests", "textures/gui/zaplatka_3_2.png"),
            new ResourceLocation("ftbquests", "textures/gui/zaplatka_4_2.png"),
            new ResourceLocation("ftbquests", "textures/gui/zaplatka_5_2.png"),
    };

    // === Координаты кнопок заплаток (поделены на 2) ===
    private static final int[][][] GROUP_BUTTON_POSITIONS = new int[][][]{
            { {0, 0}, {162, 118}, {162, 146}, {162, 173}, {162, 201} },
            { {159, 100}, {0, 0}, {159, 155}, {159, 182}, {159, 210} },
            { {162, 103}, {162, 131}, {0, 0}, {162, 186}, {162, 213} },
            { {164, 103}, {164, 130}, {164, 158}, {0, 0}, {164, 218} },
            { {165, 101}, {165, 128}, {165, 156}, {165, 183}, {0, 0} },
    };

    private final int groupIndex;
    private final Component groupTitle;

    private final List<TexturedButton> chapterButtons = new ArrayList<>();
    private final List<Integer> chapterProgressValues = new ArrayList<>();
    private final List<ImageButton> groupButtons = new ArrayList<>();

    private int groupProgress = 0;
    private int backgroundX, backgroundY;

    int backgroundWidth = 256;
    int backgroundHeight = 256;

    public GroupDetailScreen(Component groupTitle, int groupIndex) {
        super(groupTitle);
        this.groupIndex = groupIndex;
        this.groupTitle = groupTitle;
    }

    @Override
    protected void init() {
        if (!ClientQuestFile.exists()) return;

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        TeamData data = file.selfTeamData;
        List<Chapter> allChapters = file.getAllChapters();

        int chapterStartIndex = 0;
        for (int i = 0; i < groupIndex; i++) chapterStartIndex += MyCustomScreen.GROUP_LIMITS[i];
        int limit = MyCustomScreen.GROUP_LIMITS[groupIndex];

        chapterButtons.clear();
        chapterProgressValues.clear();
        groupButtons.clear();

        backgroundX = (this.width - backgroundWidth) / 2;
        backgroundY = (this.height - backgroundHeight) / 2;

        int topOffset = 20;
        int startY = backgroundY + 89;
        int spacing = 30;
        int startX = this.width / 2 - 75;

        int totalProgress = 0;
        int totalChapters = 0;

        // === Кнопки глав ===
        for (int i = 0; i < limit && (chapterStartIndex + i) < allChapters.size(); i++) {
            Chapter chapter = allChapters.get(chapterStartIndex + i);
            int chapterProgress = getChapterProgress(chapter, data);
            chapterProgressValues.add(chapterProgress);

            totalProgress += chapterProgress;
            totalChapters++;

            boolean locked = false;

            // --- Логика блокировок ---
            if (groupIndex == 4 && i == 1) {
                Chapter g1c1 = getChapter(0, 0, allChapters);
                if (g1c1 != null && !isQuestCompleted(g1c1, 0, data)) locked = true;
            }
            if (groupIndex == 4 && i == 0) {
                Chapter g1c2 = getChapter(0, 1, allChapters);
                if (g1c2 != null && !isQuestCompleted(g1c2, 1, data)) locked = true;
            }
            if (groupIndex == 2 && i == 0) {
                Chapter g1c3 = getChapter(0, 2, allChapters);
                if (g1c3 != null && !isQuestCompleted(g1c3, 2, data)) locked = true;
            }

            TexturedButton btn = new TexturedButton(
                    startX, startY + i * spacing, 75, 10,
                    Component.literal(chapter.getTitle().getString()),
                    b -> ClientQuestFile.openGui(),
                    BUTTON_CLOSED, BUTTON_OPEN, BUTTON_LOCKED,
                    locked
            );
            chapterButtons.add(btn);
            this.addRenderableWidget(btn);
        }

        groupProgress = totalChapters > 0 ? totalProgress / totalChapters : 0;

        // === Кнопка Назад ===
        InvisibleButton backBtn = new InvisibleButton(
                backgroundX + 5, backgroundY + 148,
                15, 15,
                b -> Minecraft.getInstance().setScreen(new MyCustomScreen(Component.literal("Квесты")))
        );
        this.addRenderableWidget(backBtn);

        // === Кнопки групп справа ===
        for (int i = 0; i < 5; i++) {
            if (i == groupIndex) continue;
            int idx = i;

            int btnX = backgroundX + GROUP_BUTTON_POSITIONS[groupIndex][idx][0];
            int btnY = backgroundY + GROUP_BUTTON_POSITIONS[groupIndex][idx][1];

            ImageButton gBtn = new ImageButton(btnX, btnY, 8, 12,
                    GROUP_BUTTON_NORMAL[i], GROUP_BUTTON_HOVER[i],
                    b -> Minecraft.getInstance().setScreen(
                            new GroupDetailScreen(MyCustomScreen.parseLegacyFormatting(MyCustomScreen.GROUP_NAMES[idx]), idx)
                    )
            );
            groupButtons.add(gBtn);
            this.addRenderableWidget(gBtn);
        }
    }

    private Chapter getChapter(int groupIdx, int chapterIdx, List<Chapter> allChapters) {
        int start = 0;
        for (int i = 0; i < groupIdx; i++) start += MyCustomScreen.GROUP_LIMITS[i];
        int targetIndex = start + chapterIdx;
        return targetIndex < allChapters.size() ? allChapters.get(targetIndex) : null;
    }

    private boolean isQuestCompleted(Chapter chapter, int questIndex, TeamData data) {
        List<Quest> quests = chapter.getQuests();
        if (questIndex >= quests.size()) return false;
        return data.isCompleted(quests.get(questIndex));
    }

    private int getChapterProgress(Chapter chapter, TeamData data) {
        int sum = 0, count = 0;
        for (Quest quest : chapter.getQuests()) {
            if (!data.canStartTasks(quest)) continue;
            sum += Math.min(data.getRelativeProgress(quest), 100);
            count++;
        }
        return count > 0 ? sum / count : 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);

        ResourceLocation bg = groupIndex < BACKGROUNDS.length
                ? BACKGROUNDS[groupIndex]
                : new ResourceLocation("ftbquests", "textures/gui/my_background.png");

        RenderSystem.setShaderTexture(0, bg);
        graphics.blit(bg, backgroundX, backgroundY, 0, 0, backgroundWidth, backgroundHeight, backgroundWidth, backgroundHeight);

        int topOffset = 20;
        graphics.drawString(this.font, groupTitle, this.width / 2 - this.font.width(groupTitle) / 2, 10 + topOffset, 0xFFFFFF);

        drawProgressBar(graphics, (this.width - 41) / 2, 20 + topOffset, 46, 5, groupProgress, true);

        int barWidth = 46;
        int barHeight = 5;
        for (int i = 0; i < chapterButtons.size(); i++) {
            TexturedButton btn = chapterButtons.get(i);
            int progress = chapterProgressValues.get(i);
            int barX = btn.getX() + 13;
            int barY = btn.getY() + btn.getHeight() + 3;
            drawProgressBar(graphics, barX, barY, barWidth, barHeight, progress, false);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void drawProgressBar(GuiGraphics graphics, int x, int y, int width, int height, int progress, boolean isGroup) {
        ResourceLocation frameTex = isGroup ? GROUP_PROGRESS_FRAME : CHAPTER_PROGRESS_FRAME;
        int frameWidth = 48;
        int frameHeight = 5;
        int frameX = x + width / 2 - frameWidth / 2;
        int frameY = y + height / 2 - frameHeight / 2;

        RenderSystem.setShaderTexture(0, frameTex);
        graphics.blit(frameTex, frameX, frameY, 0, 0, frameWidth, frameHeight, frameWidth, frameHeight);

        int barMaxWidth = 42;
        int barHeight = 2;

        if (progress > 0) {
            int barX = frameX + 3;
            int barY = frameY + 2;
            if (isGroup) barY -= 1;

            ResourceLocation fillTex;
            int filledWidth;

            if (progress >= 100) {
                fillTex = PROGRESS_FILL_FULL;
                filledWidth = barMaxWidth;
            } else {
                fillTex = PROGRESS_FILL;
                filledWidth = progress * barMaxWidth / 100;
            }

            RenderSystem.setShaderTexture(0, fillTex);
            graphics.blit(fillTex, barX, barY, 0, 0, filledWidth, barHeight, barMaxWidth, barHeight);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // --- Кнопки ---
    private class TexturedButton extends Button {
        private final ResourceLocation textureClosed, textureOpen, textureLocked;
        private boolean open = false, locked = false;

        private float scale = 1f;
        private final float hoverScale = 1.1f;
        private final float animSpeed = 0.15f;

        public TexturedButton(int x, int y, int w, int h, Component title, OnPress onPress,
                              ResourceLocation texClosed, ResourceLocation texOpen, ResourceLocation texLocked,
                              boolean locked) {
            super(x, y, w, h, title, onPress, DEFAULT_NARRATION);
            this.textureClosed = texClosed;
            this.textureOpen = texOpen;
            this.textureLocked = texLocked;
            this.locked = locked;
            this.active = !locked;
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
            float targetScale = this.isHovered() ? hoverScale : 1f;
            scale += (targetScale - scale) * animSpeed;

            // ==== Рисуем кнопку ====
            g.pose().pushPose();
            g.pose().translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f, 0);
            g.pose().scale(scale, scale, 1f);
            g.pose().translate(-this.width / 2f, -this.height / 2f, 0);

            ResourceLocation tex = locked ? textureLocked : (open ? textureOpen : textureClosed);
            RenderSystem.setShaderTexture(0, tex);
            g.blit(tex, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            g.pose().popPose();


            // ==== Рисуем текст отдельно ====
            g.pose().pushPose();
            g.pose().translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f, 0);
            g.pose().scale(0.7f, 0.7f, 1f); // <-- делаем текст в 2 раза меньше
            g.pose().translate(-this.width / 2f, -this.height / 2f, 0);

            int color = this.active ? 0xFFFFFF : 0xA0A0A0;
            int tx = (this.width - Minecraft.getInstance().font.width(this.getMessage())) / 2;
            int ty = (this.height - 8) / 2;
            g.drawString(Minecraft.getInstance().font, this.getMessage(), tx, ty, color);

            g.pose().popPose();
        }
    }

    private class ImageButton extends Button {
        private final ResourceLocation normal, hover;
        private final int heightFixed;

        public ImageButton(int x, int y, int w, int h, ResourceLocation normal, ResourceLocation hover, OnPress onPress) {
            super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
            this.normal = normal;
            this.hover = hover;
            this.heightFixed = h;
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float pt) {
            boolean hov = this.isHovered();
            int widthCurrent = hov ? 16 : 10;

            ResourceLocation tex = hov ? hover : normal;
            RenderSystem.setShaderTexture(0, tex);
            g.blit(tex, this.getX(), this.getY(), 0, 0, widthCurrent, heightFixed, 16, 12);
        }
    }

    private class InvisibleButton extends Button {
        private float scale = 1f;
        private final int hoverColor = 0x80FFFFFF;

        public InvisibleButton(int x, int y, int w, int h, OnPress onPress) {
            super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float pt) {
            boolean hov = isHoveredOrFocused();

            float target = hov ? 1.1f : 1f;
            scale += (target - scale) * 0.4f;

            g.pose().pushPose();
            g.pose().translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f, 0);
            g.pose().scale(scale, scale, 1f);
            g.pose().translate(-this.width / 2f, -this.height / 2f, 0);

            if (hov) g.fill(0, 0, this.width, this.height, hoverColor);

            g.pose().popPose();
        }
    }
}
