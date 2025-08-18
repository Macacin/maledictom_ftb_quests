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

    // Ресурсы текстур
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/my_background.png");
    private static final ResourceLocation BUTTON_CLOSED = new ResourceLocation("ftbquests", "textures/gui/button_closed.png");
    private static final ResourceLocation BUTTON_OPEN = new ResourceLocation("ftbquests", "textures/gui/button_open.png");
    private static final ResourceLocation BUTTON_ICON = new ResourceLocation("ftbquests", "textures/gui/button_execute.png");
    private static final ResourceLocation BUTTON_LOCKED = new ResourceLocation("ftbquests", "textures/gui/button_locked.png");

    private static final String[] GROUP_NAMES = {
            "&lСкитания",   // Измените на ваше название 1
            "&k&lПроклятый",    // Название 2
            "&lЧародейство",   // Название 3
            "&lПромышленная революция",  // Название 4
            "&lИспытание на прочность"  // Название 5
    };

    private enum Phase { ANIM1, ANIM2, ANIM3, MAIN }
    private Phase phase = Phase.ANIM1;
    private long animStartTime;
    private static final ResourceLocation ANIM1_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/animation1.png"); // 512x24576, 24 кадра
    private static final ResourceLocation ANIM2_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/animation2.png"); // 512x13312, 13 кадров
    private static final ResourceLocation ANIM3_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/animation3.png"); // 512x12288, 24 кадра по 512x512
    private static final int ANIM1_FRAMES = 24;
    private static final int ANIM1_DURATION_MS = 1000; // 1 секунда
    private static final int ANIM1_FRAME_HEIGHT = 1024; // 24576 / 24
    private static final int ANIM2_FRAMES = 24;
    private static final int ANIM2_DURATION_MS = 1000; // 0.54 секунды
    private static final int ANIM2_FRAME_HEIGHT = 1024; // 13312 / 13 ≈ 1024
    private static final int ANIM3_FRAMES = 24;
    private static final int ANIM3_DURATION_MS = 1000; // Предполагаем 1 секунду, измени если нужно
    private static final int ANIM3_FRAME_HEIGHT = 512; // Каждый кадр 512x512

    // Настройки групп и кнопок
    public static final int[] GROUP_LIMITS = {5, 2, 1, 3, 2}; // сколько подкнопок в каждой группе
    private static final int START_X = 380; // начальная позиция X для первой группы
    private static final int START_Y = 100; // начальная позиция Y для первой группы
    private static final int BUTTON_WIDTH = 200; // ширина кнопки
    private static final int BUTTON_HEIGHT = 40; // высота кнопки
    private static final int SUBBUTTON_VERTICAL_SPACING = 39; // вертикальный отступ между подкнопками
    private static final int GROUP_VERTICAL_SPACING = 10; // отступ между группами

    private final Component title; // заголовок экрана
    private final List<ButtonGroup> groups = new ArrayList<>(); // список всех групп
    private final List<Integer> progressPercentPerButton = new ArrayList<>(); // прогресс для каждой подкнопки
    private final List<Integer> progressPercentPerGroup = new ArrayList<>(); // прогресс для каждой группы
    private final List<Boolean> groupOpenStates = new ArrayList<>(); // состояния открытости групп

    public MyCustomScreen(Component title) {
        super(title);
        this.title = title;
        this.animStartTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        groups.clear();
        progressPercentPerButton.clear();
        progressPercentPerGroup.clear();
        phase = Phase.ANIM1;
        animStartTime = System.currentTimeMillis();
    }

    private void initMainContent() {
        if (!ClientQuestFile.exists()) return; // если нет файла с квестами — выходим

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        List<Chapter> allChapters = file.getAllChapters(); // получаем все главы

        int chapterIndex = 0;
        int currentY = START_Y;

        // Создание групп и их подкнопок
        for (int groupNum = 0; groupNum < GROUP_LIMITS.length; groupNum++) {
            int limit = GROUP_LIMITS[groupNum];
            int remainingChapters = allChapters.size() - chapterIndex;
            int subCount = Math.min(limit, remainingChapters); // сколько подкнопок реально будет

            createGroup(START_X, currentY, subCount, groupNum); // создаем группу
            ButtonGroup group = groups.get(groups.size() - 1);

            // Подписываем подкнопки названиями глав
            for (int i = 0; i < subCount; i++) {
                Chapter chapter = allChapters.get(chapterIndex);
                group.subButtons.get(i).setMessage(Component.literal(chapter.getTitle().getString()));
                chapterIndex++;
            }

            progressPercentPerGroup.add(0); // изначальный прогресс группы = 0
            currentY += 120; // сдвигаем координату Y для следующей группы
        }

        // Установка состояния открытия групп
        for (int i = 0; i < groups.size(); i++) {
            boolean openState = i < groupOpenStates.size() ? groupOpenStates.get(i) : false;
            groups.get(i).setOpen(openState);
        }

        recalcPositions(); // пересчитываем позиции всех кнопок

        // Создание кнопки "Назад"
        int backBtnWidth = 130;
        int backBtnHeight = 18;
        int backBtnX = (this.width - backBtnWidth) / 2 - 1;
        int backBtnY = this.height - backBtnHeight - 66; // на 20 пикселей выше, чем было

        Button backBtn = Button.builder(Component.literal("Назад"),
                        b -> {
                            Minecraft mc = Minecraft.getInstance();
                            if (mc.player != null) {
                                // открываем стандартный инвентарь игрока
                                mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
                            }
                        })
                .pos(backBtnX, backBtnY)
                .size(backBtnWidth, backBtnHeight)
                .build();
        this.addRenderableWidget(backBtn);
    }

    // Создание группы и её подкнопок
    private void createGroup(int x, int y, int subCount, int groupIndex) {
        ButtonGroup group = new ButtonGroup(x, y, subCount, () -> true, groupIndex);
        groups.add(group);
        group.addToScreen(this);

        for (int i = 0; i < subCount; i++) {
            progressPercentPerButton.add(0); // начальный прогресс каждой подкнопки
        }
    }

    // Пересчет Y-позиций всех кнопок
    private void recalcPositions() {
        int y = START_Y;
        for (ButtonGroup g : groups) {
            g.setPositionY(y);
            int totalHeight = g.mainButton.getHeight();
            if (g.isOpen()) {
                totalHeight += g.getVisibleSubButtonsCount() * SUBBUTTON_VERTICAL_SPACING;
            }
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
            if (elapsed >= ANIM1_DURATION_MS) {
                phase = Phase.ANIM2;
                animStartTime = currentTime;
            }
            return; // не рендерить дальше
        } else if (phase == Phase.ANIM2) {
            this.renderBackground(graphics);
            renderAnimation(graphics, ANIM2_TEXTURE, ANIM2_FRAMES, ANIM2_DURATION_MS, ANIM2_FRAME_HEIGHT);
            if (elapsed >= ANIM2_DURATION_MS) {
                phase = Phase.ANIM3;
                animStartTime = currentTime;
            }
            return;
        } else if (phase == Phase.ANIM3) {
            this.renderBackground(graphics);
            renderAnimation(graphics, ANIM3_TEXTURE, ANIM3_FRAMES, ANIM3_DURATION_MS, ANIM3_FRAME_HEIGHT);
            if (elapsed >= ANIM3_DURATION_MS) {
                phase = Phase.MAIN;
                initMainContent(); // инициализируем кнопки только после анимаций
                animStartTime = currentTime;
            }
            return;
        }
        this.renderBackground(graphics); // фон экрана
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);

        int texWidth = 512;
        int texHeight = 512;
        int baseX = (this.width - texWidth) / 2 + 40;
        int baseY = (this.height - texHeight) / 2;
        graphics.blit(BACKGROUND_TEXTURE, baseX, baseY, 0, 0, texWidth, texHeight, texWidth, texHeight); // отрисовка фона

        updatePinnedQuestProgress(); // обновляем прогресс квестов
        super.render(graphics, mouseX, mouseY, partialTicks); // рендер кнопок
    }

    // Обновление прогресса всех подкнопок и групп
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

            int groupSum = 0;
            int groupCount = 0;

            for (TexturedButton subBtn : group.subButtons) {
                if (globalChapterIndex >= allChapters.size()) break;
                Chapter chapter = allChapters.get(globalChapterIndex);
                globalChapterIndex++;

                int sum = 0, count = 0;
                for (Quest quest : chapter.getQuests()) {
                    if (!data.canStartTasks(quest)) continue;
                    int prog = data.getRelativeProgress(quest);
                    sum += Math.min(prog, 100);
                    count++;
                }
                int progress = count > 0 ? sum / count : 0;
                setProgress(progressIndex, progress); // обновляем прогресс подкнопки
                progressIndex++;

                groupSum += progress;
                groupCount++;
            }

            int groupProgress = groupCount > 0 ? groupSum / groupCount : 0;
            setGroupProgress(groupIndex, groupProgress); // обновляем прогресс группы
        }
    }

    // Сеттер прогресса отдельной подкнопки
    private void setProgress(int index, int value) {
        if (index >= progressPercentPerButton.size()) progressPercentPerButton.add(value);
        else progressPercentPerButton.set(index, value);
    }

    // Сеттер прогресса группы
    private void setGroupProgress(int groupIndex, int value) {
        while (progressPercentPerGroup.size() <= groupIndex) progressPercentPerGroup.add(0);
        progressPercentPerGroup.set(groupIndex, value);
    }

    @Override
    public boolean isPauseScreen() { return false; } // экран не ставит игру на паузу

    // Класс группы с основной кнопкой и подкнопками
    private class ButtonGroup {
        private final TexturedButton mainButton; // основная кнопка группы
        private final List<TexturedButton> subButtons = new ArrayList<>(); // подкнопки
        private final BooleanSupplier visibleCondition; // условие видимости подкнопок
        private boolean open = false; // открыта ли группа
        private final int groupIndex; // индекс группы

        ButtonGroup(int x, int y, int subCount, BooleanSupplier visibleCondition, int groupIndex) {
            this.visibleCondition = visibleCondition;
            this.groupIndex = groupIndex;

            MutableComponent groupTitle = parseLegacyFormatting(GROUP_NAMES[groupIndex]);
            this.mainButton = new TexturedButton(
                    x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                    groupTitle,
                    b -> Minecraft.getInstance().setScreen(new GroupDetailScreen(
                            parseLegacyFormatting(GROUP_NAMES[groupIndex]),  // Также парсим для заголовка экрана
                            groupIndex
                    )),
                    BUTTON_CLOSED,
                    BUTTON_OPEN,
                    BUTTON_LOCKED,
                    false
            );

            // Создаем подкнопки группы
            for (int i = 0; i < subCount; i++) {
                TexturedButton sub = new TexturedButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.literal("Подкнопка " + (i + 1)),
                        btn -> {},
                        BUTTON_ICON,
                        BUTTON_ICON,
                        BUTTON_LOCKED,
                        false
                );
                sub.visible = false; // изначально скрываем
                subButtons.add(sub);
            }
        }

        void addToScreen(MyCustomScreen screen) {
            screen.addRenderableWidget(mainButton);
            subButtons.forEach(screen::addRenderableWidget); // добавляем все подкнопки на экран
        }

        // Перемещает все кнопки группы по Y
        void setPositionY(int y) {
            mainButton.setY(y);
            int currentY = y + mainButton.getHeight() + 19;
            for (TexturedButton sub : subButtons) {
                sub.setY(currentY);
                currentY += SUBBUTTON_VERTICAL_SPACING;
            }
        }

        // Сколько подкнопок видно
        int getVisibleSubButtonsCount() {
            return visibleCondition.getAsBoolean() && open ? subButtons.size() : 0;
        }

        boolean isOpen() { return open; }
        void setOpen(boolean open) { this.open = open; }
    }

    // Класс текстурированной кнопки
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
            this.active = !locked; // кнопка активна, если не заблокирована
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            boolean hovered = isHoveredOrFocused();
            float targetScale = hovered ? 1.1f : 1f; // масштаб при наведении
            scale += (targetScale - scale) * 0.2f; // плавное изменение масштаба

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f, 0);
            guiGraphics.pose().scale(scale, scale, 1f);
            guiGraphics.pose().translate(-this.width / 2f, -this.height / 2f, 0);

            ResourceLocation tex = locked ? textureLocked : (open ? textureOpen : textureClosed);
            RenderSystem.setShaderTexture(0, tex);
            guiGraphics.blit(tex, 0, 0, 0, 0, this.width, this.height, this.width, this.height); // рисуем текстуру кнопки

            int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
            int textX = (this.width - Minecraft.getInstance().font.width(this.getMessage())) / 2;
            int textY = (this.height - 8) / 2;
            guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage(), textX, textY, textColor);

            guiGraphics.pose().popPose();
        }
    }

    private void renderAnimation(GuiGraphics graphics, ResourceLocation texture, int frames, int durationMs, int frameHeight) {
        long elapsed = System.currentTimeMillis() - animStartTime;
        int frame = (int) ((elapsed * frames) / durationMs);
        frame = Math.min(frame, frames - 1);
        int texWidth = 512;
        int texHeight = frameHeight * frames;
        int posX = Math.floorDiv(this.width - texWidth, 2) + 40;
        int posY = Math.floorDiv(this.height - frameHeight, 2);

        RenderSystem.setShaderTexture(0, texture);
        graphics.blit(texture, posX, posY, 0, frame * frameHeight, texWidth, frameHeight, texWidth, texHeight);
    }
    private MutableComponent parseLegacyFormatting(String text) {
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
                    if (formatting == ChatFormatting.RESET) {
                        currentStyle = Style.EMPTY;
                    } else {
                        currentStyle = currentStyle.applyFormat(formatting);
                    }
                    i++; // Пропустить код
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
