package dev.ftb.mods.ftbquests.client.gui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;


public class SpriteSheetGenerator {

    public static void main(String[] args) {
        // Получаем корень проекта
        String projectRoot = System.getProperty("user.dir");
        // Путь к директории с изображениями относительно корня проекта
        String directoryPath = projectRoot + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "assets" + File.separator + "ftbquests" + File.separator + "frames";
        // Путь для сохранения спрайт-шита относительно корня проекта
        String outputPath = projectRoot + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "assets" + File.separator + "ftbquests" + File.separator + "frames" + File.separator + "animation2.png";

        try {
            // Добавляем дебаг вывод для диагностики корня проекта
            System.out.println("Current working directory (project root): " + projectRoot);

            // Список файлов в корне проекта
            System.out.println("Files and directories in root:");
            File rootDir = new File(projectRoot);
            File[] rootFiles = rootDir.listFiles();
            if (rootFiles != null) {
                Arrays.sort(rootFiles, Comparator.comparing(File::getName));
                for (File f : rootFiles) {
                    System.out.println(f.getName() + (f.isDirectory() ? " (directory)" : ""));
                }
            } else {
                System.out.println("Не удалось получить список файлов в корне.");
            }

            // Если src не найдена, проверьте на наличие 'common'
            File commonDir = new File(projectRoot, "common");
            if (commonDir.exists() && commonDir.isDirectory()) {
                System.out.println("Обнаружена директория 'common'. Изменяем путь к ресурсам.");
                directoryPath = projectRoot + File.separator + "common" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "assets" + File.separator + "ftbquests" + File.separator + "frames";
                outputPath = projectRoot + File.separator + "common" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "assets" + File.separator + "ftbquests" + File.separator + "textures" + File.separator + "animation2.png";
            }

            // Получаем список файлов изображений (предполагаем PNG)
            File dir = new File(directoryPath);
            System.out.println("Directory absolute path: " + dir.getAbsolutePath());
            System.out.println("Directory exists: " + dir.exists());
            System.out.println("Directory is directory: " + dir.isDirectory());

            File[] imageFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
            if (imageFiles == null || imageFiles.length == 0) {
                System.out.println("Нет изображений в директории. Список файлов в директории:");
                File[] allFiles = dir.listFiles();
                if (allFiles != null) {
                    for (File f : allFiles) {
                        System.out.println(f.getName());
                    }
                } else {
                    System.out.println("Не удалось получить список файлов.");
                }
                return;
            }
            System.out.println("Найдено изображений: " + imageFiles.length);

            // Сортируем файлы по имени для последовательности
            Arrays.sort(imageFiles, Comparator.comparing(File::getName));

            // Читаем изображения
            List<BufferedImage> images = new ArrayList<>();
            int maxWidth = 0;
            int totalHeight = 0;
            for (File file : imageFiles) {
                BufferedImage img = ImageIO.read(file);
                images.add(img);
                maxWidth = Math.max(maxWidth, img.getWidth());
                totalHeight += img.getHeight();
            }

            if (images.isEmpty()) {
                System.out.println("Нет изображений для обработки.");
                return;
            }

            // Создаем спрайт-шит с максимальной шириной и суммарной высотой
            BufferedImage spriteSheet = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = spriteSheet.createGraphics();

            // Начинаем рисовать с верха: первая картинка сверху
            int currentY = 0;
            for (BufferedImage img : images) {
                g2d.drawImage(img, 0, currentY, null);
                currentY += img.getHeight();
            }

            g2d.dispose();

            // Сохраняем спрайт-шит
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs(); // Создаем директории, если не существуют
            ImageIO.write(spriteSheet, "png", outputFile);
            System.out.println("Спрайт-шит сохранен: " + outputPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}