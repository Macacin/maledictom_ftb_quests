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

    // Текстуры интерфейса
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/my_background.png");
    private static final ResourceLocation BUTTON_CLOSED = new ResourceLocation("ftbquests", "textures/gui/button_closed.png");
    private static final ResourceLocation BUTTON_OPEN = new ResourceLocation("ftbquests", "textures/gui/button_open.png");
    private static final ResourceLocation BUTTON_LOCKED = new ResourceLocation("ftbquests", "textures/gui/button_locked.png");

    private static final ResourceLocation GROUP_PROGRESS_FRAME = new ResourceLocation("ftbquests", "textures/gui/group_pr.png");
    private static final ResourceLocation CHAPTER_PROGRESS_FRAME = new ResourceLocation("ftbquests", "textures/gui/glav_pr.png");

    private final int groupIndex; // индекс группы
    private final Component groupTitle; // заголовок группы

    private final List<TexturedButton> chapterButtons = new ArrayList<>(); // кнопки глав
    private final List<Integer> chapterProgressValues = new ArrayList<>(); // прогресс каждой главы
    private int groupProgress = 0; // прогресс всей группы

    private int backgroundX, backgroundY, backgroundWidth = 512, backgroundHeight = 512; // параметры фона

    public GroupDetailScreen(Component groupTitle, int groupIndex) {
        super(groupTitle);
        this.groupIndex = groupIndex;
        this.groupTitle = groupTitle;
    }

    @Override
    protected void init() {
        if (!ClientQuestFile.exists()) return; // если нет файла квестов — выходим

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        TeamData data = file.selfTeamData;
        List<Chapter> allChapters = file.getAllChapters();

        // Определяем с какого индекса начинаются главы этой группы
        int chapterStartIndex = 0;
        for (int i = 0; i < groupIndex; i++) chapterStartIndex += MyCustomScreen.GROUP_LIMITS[i];
        int limit = MyCustomScreen.GROUP_LIMITS[groupIndex]; // сколько глав должно быть в группе

        chapterButtons.clear();
        chapterProgressValues.clear();

        int totalProgress = 0;
        int totalChapters = 0;

        backgroundX = (this.width - backgroundWidth) / 2;
        backgroundY = (this.height - backgroundHeight) / 2;

        int topOffset = 40;
        int startY = 100 + topOffset;
        int spacing = 60; // расстояние между кнопками
        int startX = (this.width - 150) / 2;

        for (int i = 0; i < limit && (chapterStartIndex + i) < allChapters.size(); i++) {
            Chapter chapter = allChapters.get(chapterStartIndex + i);
            int chapterProgress = getChapterProgress(chapter, data); // прогресс главы
            chapterProgressValues.add(chapterProgress);

            totalProgress += chapterProgress;
            totalChapters++;

            boolean locked = false; // по умолчанию глава не заблокирована

            // === ЛОГИКА БЛОКИРОВОК ===
            // 1) Если первый квест первой главы группы 1 не выполнен → блокируем 2 главу в группе 5
            if (groupIndex == 4 && i == 1) {
                Chapter g1c1 = getChapter(0, 0, allChapters);
                if (g1c1 != null && !isQuestCompleted(g1c1, 0, data)) locked = true;
            }

            // 2) Если второй квест второй главы группы 1 не выполнен → блокируем 1 главу в группе 5
            if (groupIndex == 4 && i == 0) {
                Chapter g1c2 = getChapter(0, 1, allChapters);
                if (g1c2 != null && !isQuestCompleted(g1c2, 1, data)) locked = true;
            }

            // 3) Если третий квест третьей главы группы 1 не выполнен → блокируем 1 главу в группе 3
            if (groupIndex == 2 && i == 0) {
                Chapter g1c3 = getChapter(0, 2, allChapters);
                if (g1c3 != null && !isQuestCompleted(g1c3, 2, data)) locked = true;
            }

            // Создаем кнопку главы
            TexturedButton btn = new TexturedButton(
                    startX, startY + i * spacing, 150, 20,
                    Component.literal(chapter.getTitle().getString()),
                    b -> ClientQuestFile.openGui(),
                    BUTTON_CLOSED, BUTTON_OPEN, BUTTON_LOCKED,
                    locked
            );
            chapterButtons.add(btn);
            this.addRenderableWidget(btn);
        }

        // вычисляем общий прогресс группы
        groupProgress = totalChapters > 0 ? totalProgress / totalChapters : 0;

        // Кнопка "Назад"
        int backBtnWidth = 130;
        int backBtnHeight = 18;
        int backBtnX = backgroundX + (backgroundWidth - backBtnWidth) / 2;
        int backBtnY = backgroundY + backgroundHeight - backBtnHeight - 35;

        Button backBtn = Button.builder(Component.literal("Назад"),
                        b -> Minecraft.getInstance().setScreen(new MyCustomScreen(Component.literal("Квесты"))))
                .pos(backBtnX, backBtnY)
                .size(backBtnWidth, backBtnHeight)
                .build();

        this.addRenderableWidget(backBtn);
    }

    /** Получить главу по индексу группы и главы */
    private Chapter getChapter(int groupIdx, int chapterIdx, List<Chapter> allChapters) {
        int start = 0;
        for (int i = 0; i < groupIdx; i++) start += MyCustomScreen.GROUP_LIMITS[i];
        int targetIndex = start + chapterIdx;
        return targetIndex < allChapters.size() ? allChapters.get(targetIndex) : null;
    }

    /** Проверить выполнение квеста по индексу в главе */
    private boolean isQuestCompleted(Chapter chapter, int questIndex, TeamData data) {
        List<Quest> quests = chapter.getQuests();
        if (questIndex >= quests.size()) return false;
        return data.isCompleted(quests.get(questIndex));
    }

    /** Получить прогресс главы */
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
        // Рисуем фон
        this.renderBackground(graphics);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        graphics.blit(BACKGROUND_TEXTURE, backgroundX, backgroundY, 0, 0, backgroundWidth, backgroundHeight, backgroundWidth, backgroundHeight);

        int topOffset = 40;
        graphics.drawString(this.font, groupTitle, this.width / 2 - this.font.width(groupTitle) / 2, 20 + topOffset, 0xFFFFFF);

        // Рисуем прогресс группы
        drawProgressBar(graphics, (this.width - 83) / 2, 40 + topOffset, 93, 9, groupProgress, true);

        // Рисуем прогресс глав
        int barWidth = 93;
        int barHeight = 9;
        for (int i = 0; i < chapterButtons.size(); i++) {
            TexturedButton btn = chapterButtons.get(i);
            int progress = chapterProgressValues.get(i);
            int barX = btn.getX() + 26;
            int barY = btn.getY() + btn.getHeight() + 5;
            drawProgressBar(graphics, barX, barY, barWidth, barHeight, progress, false);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    // Отрисовка прогресс-бара
    private void drawProgressBar(GuiGraphics graphics, int x, int y, int width, int height, int progress, boolean isGroup) {
        ResourceLocation frameTex = isGroup ? GROUP_PROGRESS_FRAME : CHAPTER_PROGRESS_FRAME;

        int frameWidth = 97;
        int frameHeight = 11;
        int frameX = x + width / 2 - frameWidth / 2;
        int frameY = y + height / 2 - frameHeight / 2;
        int filled = progress * width / 100;
        graphics.fill(x + filled, y, x + width, y + height, 0xFF808080); // фон
        graphics.fill(x, y, x + filled, y + height, 0xFF0000FF); // прогресс

        RenderSystem.setShaderTexture(0, frameTex);
        graphics.blit(frameTex, frameX, frameY, 0, 0, frameWidth, frameHeight, frameWidth, frameHeight);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Класс текстурированной кнопки (как в MyCustomScreen)
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
            float targetScale = this.isHovered() ? 1.1f : 1.0f;
            scale += (targetScale - scale) * 0.1f;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f, 0);
            guiGraphics.pose().scale(scale, scale, 1f);
            guiGraphics.pose().translate(-this.width / 2f, -this.height / 2f, 0);

            ResourceLocation tex = locked ? textureLocked : (open ? textureOpen : textureClosed);
            RenderSystem.setShaderTexture(0, tex);
            guiGraphics.blit(tex, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

            int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
            int textX = (this.width - Minecraft.getInstance().font.width(this.getMessage())) / 2;
            int textY = (this.height - 8) / 2;
            guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage(), textX, textY, textColor);

            guiGraphics.pose().popPose();
        }
    }
}
