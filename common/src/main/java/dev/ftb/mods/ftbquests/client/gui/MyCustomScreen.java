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

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * Экран с группами кнопок и прогресс-барами для квестов.
 * Логика разбита на группы и подкнопки с возможностью блокировки.
 * Всё управление визуалом и положением внутри этого класса.
 */
public class MyCustomScreen extends Screen {

    // --------------------- Настройки ресурсов (текстур) ---------------------
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/my_background.png");
    private static final ResourceLocation BUTTON_CLOSED = new ResourceLocation("ftbquests", "textures/gui/button_closed.png");
    private static final ResourceLocation BUTTON_OPEN = new ResourceLocation("ftbquests", "textures/gui/button_open.png");
    private static final ResourceLocation BUTTON_ICON = new ResourceLocation("ftbquests", "textures/gui/button_execute.png");
    private static final ResourceLocation BUTTON_LOCKED = new ResourceLocation("ftbquests", "textures/gui/button_locked.png");

    // Прогресс-бары для разных процентов
    private static final Map<Integer, ResourceLocation> PROGRESS_TEXTURES = Map.ofEntries(
            Map.entry(0, new ResourceLocation("ftbquests", "textures/gui/progress_0.png")),
            Map.entry(5, new ResourceLocation("ftbquests", "textures/gui/progress_5.png")),
            Map.entry(10, new ResourceLocation("ftbquests", "textures/gui/progress_10.png")),
            Map.entry(15, new ResourceLocation("ftbquests", "textures/gui/progress_15.png")),
            Map.entry(20, new ResourceLocation("ftbquests", "textures/gui/progress_20.png")),
            Map.entry(25, new ResourceLocation("ftbquests", "textures/gui/progress_25.png")),
            Map.entry(30, new ResourceLocation("ftbquests", "textures/gui/progress_30.png")),
            Map.entry(35, new ResourceLocation("ftbquests", "textures/gui/progress_35.png")),
            Map.entry(40, new ResourceLocation("ftbquests", "textures/gui/progress_40.png")),
            Map.entry(45, new ResourceLocation("ftbquests", "textures/gui/progress_45.png")),
            Map.entry(50, new ResourceLocation("ftbquests", "textures/gui/progress_50.png")),
            Map.entry(55, new ResourceLocation("ftbquests", "textures/gui/progress_55.png")),
            Map.entry(60, new ResourceLocation("ftbquests", "textures/gui/progress_60.png")),
            Map.entry(65, new ResourceLocation("ftbquests", "textures/gui/progress_65.png")),
            Map.entry(70, new ResourceLocation("ftbquests", "textures/gui/progress_70.png")),
            Map.entry(75, new ResourceLocation("ftbquests", "textures/gui/progress_75.png")),
            Map.entry(80, new ResourceLocation("ftbquests", "textures/gui/progress_80.png")),
            Map.entry(85, new ResourceLocation("ftbquests", "textures/gui/progress_85.png")),
            Map.entry(90, new ResourceLocation("ftbquests", "textures/gui/progress_90.png")),
            Map.entry(95, new ResourceLocation("ftbquests", "textures/gui/progress_95.png")),
            Map.entry(100, new ResourceLocation("ftbquests", "textures/gui/progress_100.png"))
    );

    // --------------------- Управляющие параметры — тут можно менять ---------------------
    private static final int START_X = 320; // Начальная позиция X для групп кнопок
    private static final int START_Y = 60;  // Начальная позиция Y для первой группы
    private static final int BUTTON_WIDTH = 100; // Ширина кнопок
    private static final int BUTTON_HEIGHT = 20; // Высота кнопок
    private static final int SUBBUTTON_VERTICAL_SPACING = 39; // Вертикальный отступ между подкнопками
    private static final int GROUP_VERTICAL_SPACING = 4; // Отступ между группами по вертикали

    // Максимальное число подкнопок в каждой группе (по индексу группы)
    private static final int[] GROUP_LIMITS = {5, 2, 1, 3, 2};

    // --------------------- Поля класса ---------------------
    private final Component title; // Заголовок экрана

    // Список всех групп кнопок на экране
    private final List<ButtonGroup> groups = new ArrayList<>();

    // Списки для хранения прогресса каждой подкнопки и группы
    private final List<Integer> progressPercentPerButton = new ArrayList<>();
    private final List<Integer> progressPercentPerGroup = new ArrayList<>();

    // Состояния открытости групп для запоминания между вызовами
    private final List<Boolean> groupOpenStates = new ArrayList<>();

    // --------------------- Конструктор ---------------------
    public MyCustomScreen(Component title) {
        super(title);
        this.title = title;
    }

    // --------------------- Инициализация — создаём группы и кнопки ---------------------
    @Override
    protected void init() {
        groups.clear();
        progressPercentPerButton.clear();
        progressPercentPerGroup.clear();

        if (!ClientQuestFile.exists()) {
            return; // Если файл с квестами не загружен — ничего не создаём
        }

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        List<Chapter> allChapters = file.getAllChapters();

        int chapterIndex = 0;
        int currentY = START_Y;

        // Создаём группы кнопок и подкнопки в каждой группе с названиями глав
        for (int groupNum = 0; groupNum < GROUP_LIMITS.length; groupNum++) {
            int limit = GROUP_LIMITS[groupNum];
            int remainingChapters = allChapters.size() - chapterIndex;
            int subCount = Math.min(limit, remainingChapters);

            createGroup(START_X, currentY, subCount, () -> true);
            ButtonGroup group = groups.get(groups.size() - 1);

            // Заполняем названия подкнопок названиями глав
            for (int i = 0; i < subCount; i++) {
                Chapter chapter = allChapters.get(chapterIndex);
                group.subButtons.get(i).setMessage(Component.literal(chapter.getTitle().getString()));
                chapterIndex++;
            }

            progressPercentPerGroup.add(0); // Изначальный прогресс группы 0

            currentY += 120; // Смещаем позицию для следующей группы
        }

        // Устанавливаем состояние открытия для групп по сохранённым значениям
        for (int i = 0; i < groups.size(); i++) {
            boolean openState = i < groupOpenStates.size() ? groupOpenStates.get(i) : false;
            groups.get(i).setOpen(openState);
        }

        recalcPositions();
    }

    // --------------------- Создаём группу кнопок ---------------------
    private void createGroup(int x, int y, int subCount, BooleanSupplier visibleCondition) {
        ButtonGroup group = new ButtonGroup(x, y, subCount, visibleCondition);
        groups.add(group);
        group.addToScreen(this);

        // Регистрируем прогресс каждой подкнопки как 0 по умолчанию
        for (int i = 0; i < subCount; i++) {
            progressPercentPerButton.add(0);
        }
    }

    // --------------------- Пересчёт позиции Y для всех групп и подкнопок ---------------------
    private void recalcPositions() {
        int y = START_Y;

        for (ButtonGroup g : groups) {
            g.setPositionY(y);

            int totalHeight = g.mainButton.getHeight();

            if (g.isOpen()) {
                int subCount = g.getVisibleSubButtonsCount();
                totalHeight += subCount * SUBBUTTON_VERTICAL_SPACING;
            }

            y += totalHeight + GROUP_VERTICAL_SPACING;
        }
    }

    // --------------------- Отрисовка экрана ---------------------
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fill(0, 0, this.width, this.height, 0x80000000);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);

        int texWidth = 399;
        int texHeight = 510;
        float scale = 0.8f;
        int scaledWidth = (int) (texWidth * scale);
        int scaledHeight = (int) (texHeight * scale);
        int baseX = (this.width - scaledWidth) / 2;
        int baseY = (this.height - scaledHeight) / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(baseX, baseY, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.blit(BACKGROUND_TEXTURE, 0, 0, 0, 0, texWidth, texHeight, texWidth, texHeight);
        graphics.pose().popPose();

        // Обновляем прогресс по квестам
        updatePinnedQuestProgress();

        int progressIndex = 0;

        // Для каждой группы рисуем прогресс-бар и прогресс подкнопок
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            ButtonGroup group = groups.get(groupIndex);

            int groupProgress = progressPercentPerGroup.size() > groupIndex ? progressPercentPerGroup.get(groupIndex) : -1;
            if (groupProgress >= 0) {
                int key = findClosestProgressKey(groupProgress);
                ResourceLocation texture = PROGRESS_TEXTURES.get(key);

                if (texture != null) {
                    RenderSystem.setShaderTexture(0, texture);

                    int texX = group.mainButton.getX() + group.mainButton.getWidth() + 5;
                    int texY = group.mainButton.getY() + (group.mainButton.getHeight() - 16) / 2;

                    int width = 185;
                    int height = 16;

                    graphics.blit(texture, texX, texY, 0, 0, width, height, width, height);
                }
            }

            for (TexturedButton subBtn : group.subButtons) {
                if (progressIndex >= progressPercentPerButton.size()) break;

                int percent = progressPercentPerButton.get(progressIndex);

                if (percent >= 0) {
                    int key = findClosestProgressKey(percent);
                    ResourceLocation texture = PROGRESS_TEXTURES.get(key);

                    if (texture != null) {
                        RenderSystem.setShaderTexture(0, texture);

                        int texX = subBtn.getX();
                        int texY = subBtn.getY() - 17;

                        int width = 185;
                        int height = 16;

                        graphics.blit(texture, texX, texY, 0, 0, width, height, width, height);
                    }
                }
                progressIndex++;
            }
        }

        // Отрисовываем все кнопки (super.render)
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    // --------------------- Поиск ближайшего ключа прогресса для отрисовки ---------------------
    private int findClosestProgressKey(int percent) {
        List<Integer> keys = new ArrayList<>(PROGRESS_TEXTURES.keySet());
        Collections.sort(keys);

        int result = 0;

        for (int key : keys) {
            if (key <= percent) {
                result = key;
            } else {
                break;
            }
        }

        return result;
    }

    // --------------------- Обновление прогресса по квестам (для отображения на кнопках) ---------------------
    private void updatePinnedQuestProgress() {
        if (!ClientQuestFile.exists()) {
            progressPercentPerButton.clear();
            progressPercentPerGroup.clear();
            return;
        }

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        TeamData data = file.selfTeamData;

        List<Chapter> allChapters = file.getAllChapters();
        int globalChapterIndex = 0;
        int progressIndex = 0;

        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            ButtonGroup group = groups.get(groupIndex);

            // Если группа закрыта — ставим прогресс -1 (не отображать)
            if (!group.isOpen()) {
                for (int i = 0; i < group.subButtons.size(); i++) {
                    setProgress(progressIndex, -1);
                    progressIndex++;
                    globalChapterIndex++;
                }
                setGroupProgress(groupIndex, -1);
                continue;
            }

            int groupSum = 0;
            int groupCount = 0;

            for (int i = 0; i < group.subButtons.size(); i++) {
                if (globalChapterIndex >= allChapters.size()) {
                    setProgress(progressIndex, -1);
                    progressIndex++;
                    globalChapterIndex++;
                    continue;
                }

                Chapter chapter = allChapters.get(globalChapterIndex);
                globalChapterIndex++;

                int sum = 0, count = 0;
                for (Quest quest : chapter.getQuests()) {
                    if (!data.canStartTasks(quest)) continue;

                    int prog = data.getRelativeProgress(quest);
                    sum += prog >= 100 ? 100 : prog;
                    count++;
                }

                int progress = count > 0 ? sum / count : 0;
                setProgress(progressIndex, progress);
                progressIndex++;

                groupSum += progress;
                groupCount++;
            }

            int groupProgress = groupCount > 0 ? groupSum / groupCount : 0;
            setGroupProgress(groupIndex, groupProgress);
        }
    }

    // --------------------- Установка прогресса подкнопки ---------------------
    private void setProgress(int index, int value) {
        if (index >= progressPercentPerButton.size()) {
            progressPercentPerButton.add(value);
        } else {
            progressPercentPerButton.set(index, value);
        }
    }

    // --------------------- Установка прогресса группы ---------------------
    private void setGroupProgress(int groupIndex, int value) {
        while (progressPercentPerGroup.size() <= groupIndex) {
            progressPercentPerGroup.add(0);
        }
        progressPercentPerGroup.set(groupIndex, value);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // --------------------- Запоминание и установка состояния открытия группы ---------------------
    private void setGroupOpenState(int index, boolean open) {
        while (groupOpenStates.size() <= index) {
            groupOpenStates.add(false);
        }
        groupOpenStates.set(index, open);
    }

    // --------------------- Вложенный класс для группы кнопок ---------------------
    private class ButtonGroup {
        private final TexturedButton mainButton;
        private final List<TexturedButton> subButtons = new ArrayList<>();
        private final BooleanSupplier visibleCondition;
        private boolean open = false;

        /**
         * Создаёт группу с главной кнопкой и подкнопками.
         * @param x позиция X
         * @param y позиция Y
         * @param subCount количество подкнопок
         * @param visibleCondition условие видимости группы
         */
        ButtonGroup(int x, int y, int subCount, BooleanSupplier visibleCondition) {
            this.visibleCondition = visibleCondition;

            // Главная кнопка, при нажатии переключает открытость группы
            this.mainButton = new TexturedButton(
                    x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Главная кнопка"),
                    b -> {
                        open = !open;
                        ((TexturedButton) b).setOpen(open);
                        updateVisibility();
                        recalcPositions();

                        int idx = groups.indexOf(this);
                        if (idx >= 0) {
                            setGroupOpenState(idx, open);
                        }
                    },
                    BUTTON_CLOSED,
                    BUTTON_OPEN,
                    BUTTON_LOCKED,
                    0, 0, BUTTON_WIDTH, BUTTON_HEIGHT,
                    false
            );

            // Создаём подкнопки с возможностью блокировки в зависимости от прогресса квестов
            for (int i = 0; i < subCount; i++) {
                boolean locked = false;

                int thisGroupIndex = groups.size(); // индекс создаваемой группы после добавления
                // Индекс группы кнопок (считается с 0)
                boolean isFifthGroup = (thisGroupIndex == 4);  // 5-я группа (индекс 4)
                boolean isFirstGroup = (thisGroupIndex == 0);  // 1-я группа (индекс 0)

// Индексы подкнопок внутри группы
                boolean isSecondSubButton = (i == 1); // вторая подкнопка (индекс 1)
                boolean isThirdSubButton = (i == 2);  // третья подкнопка (индекс 2)

                if (ClientQuestFile.exists()) {
                    ClientQuestFile fil = ClientQuestFile.INSTANCE;
                    TeamData data = fil.selfTeamData;
                    List<Chapter> allChapter = fil.getAllChapters();

                    // Пример 1: Блокировка второй кнопки 5-й группы, если не выполнен первый квест второй главы
                    if (isFifthGroup && isSecondSubButton) {  // <-- тут проверяем группу и кнопку
                        if (allChapter.size() > 1) {           // <-- 1 — индекс главы (вторая глава, т.к. с 0)
                            Chapter secondChapter = allChapter.get(1);
                            List<Quest> quests = secondChapter.getQuests();

                            if (!quests.isEmpty()) {
                                Quest firstQuest = quests.get(0);  // <-- 0 — первый квест в главе
                                int firstQuestProgress = data.getRelativeProgress(firstQuest);

                                if (firstQuestProgress < 100) {
                                    locked = true;
                                }
                            }
                        }
                    }

                    // Пример 2: Блокировка третьей кнопки 1-й группы, если не выполнены все квесты 4-й главы
                    if (isFirstGroup && isThirdSubButton) {   // <-- группа 1, кнопка 3
                        if (allChapter.size() > 3) {            // <-- 3 — индекс главы (4-я глава)
                            Chapter fourthChapter = allChapter.get(3);
                            List<Quest> quests = fourthChapter.getQuests();

                            boolean allCompleted = true;
                            for (Quest quest : quests) {
                                if (data.getRelativeProgress(quest) < 100) {
                                    allCompleted = false;
                                    break;
                                }
                            }

                            if (!allCompleted) {
                                locked = true;
                            }
                        }
                    }

                    // Пример 3: Блокировка первой подкнопки 5-й группы, если не выполнен первый квест 3-й главы
                    if (isFifthGroup && i == 0) {             // <-- группа 5, кнопка 1
                        if (allChapter.size() > 2) {            // <-- 2 — индекс главы (3-я глава)
                            Chapter thirdChapter = allChapter.get(2);
                            List<Quest> quests = thirdChapter.getQuests();

                            if (!quests.isEmpty()) {
                                Quest firstQuest = quests.get(0);  // <-- 0 — первый квест в главе
                                int firstQuestProgress = data.getRelativeProgress(firstQuest);

                                if (firstQuestProgress < 100) {
                                    locked = true;
                                }
                            }
                        }
                    }

                }

                final boolean lockedFinal = locked;

                TexturedButton sub = new TexturedButton(
                        x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.literal("Подкнопка " + (i + 1)),
                        btn -> {
                            if (!lockedFinal) {
                                if (ClientQuestFile.exists()) {
                                    ClientQuestFile.openGui();
                                }
                            } else {
                                Minecraft.getInstance().player.displayClientMessage(
                                        Component.literal("Сначала нужно завершить необходимые квесты!"), true);
                            }
                        },
                        BUTTON_ICON,
                        BUTTON_ICON,
                        BUTTON_LOCKED,
                        0, 0, BUTTON_WIDTH, BUTTON_HEIGHT,
                        lockedFinal
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

            int currentY = y + mainButton.getHeight() + 19;

            for (TexturedButton sub : subButtons) {
                sub.setY(currentY);
                currentY += SUBBUTTON_VERTICAL_SPACING;
            }
        }

        void updateVisibility() {
            if (!visibleCondition.getAsBoolean()) {
                mainButton.visible = false;
                subButtons.forEach(btn -> btn.visible = false);
                return;
            }

            mainButton.visible = true;
            for (TexturedButton sub : subButtons) {
                sub.visible = open && visibleCondition.getAsBoolean();
            }
        }

        int getVisibleSubButtonsCount() {
            return visibleCondition.getAsBoolean() && open ? subButtons.size() : 0;
        }

        boolean isOpen() {
            return open;
        }

        void setOpen(boolean open) {
            this.open = open;
            updateVisibility();
        }
    }

    // --------------------- Вложенный класс для кнопок с текстурами ---------------------
    private class TexturedButton extends Button {
        private final ResourceLocation textureClosed;
        private final ResourceLocation textureOpen;
        private final ResourceLocation textureLocked;
        private boolean open = false;
        private boolean locked = false;
        private float scale = 1f;

        public TexturedButton(int x, int y, int width, int height, Component title, OnPress onPress,
                              ResourceLocation textureClosed, ResourceLocation textureOpen, ResourceLocation textureLocked,
                              int texX, int texY, int texWidth, int texHeight,
                              boolean locked) {
            super(x, y, width, height, title, onPress, DEFAULT_NARRATION);
            this.textureClosed = textureClosed;
            this.textureOpen = textureOpen;
            this.textureLocked = textureLocked;
            this.locked = locked;
            this.active = !locked;
        }

        public void setOpen(boolean open) {
            this.open = open;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
            this.active = !locked;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            boolean hovered = this.isHoveredOrFocused();
            float targetScale = hovered ? 1.05f : 1.0f;
            scale += (targetScale - scale) * 0.2f;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f, 0);
            guiGraphics.pose().scale(scale, scale, 1f);
            guiGraphics.pose().translate(-this.width / 2f, -this.height / 2f, 0);

            ResourceLocation tex;
            if (locked) {
                tex = textureLocked;
            } else {
                tex = open ? textureOpen : textureClosed;
            }

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
