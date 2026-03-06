package com.shang.vl.util;


import com.shang.vl.model.Coordinate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by shangwei2009@hotmail.com on 2025/8/27 18:25
 */
public class ImageUtil {

    public static void addAxesAndPointToImage(InputStream inputImage, Coordinate coordinate, OutputStream outputImage, String outputFormat) {
        try {
            // 读取原始图片
            BufferedImage originalImage = ImageIO.read(inputImage);
            if (originalImage == null) {
                throw new IOException("无法读取图片");
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // 计算新图片的尺寸（添加padding）
            int padding = 50;
            int newWidth = originalWidth + 2 * padding;
            int newHeight = originalHeight + 2 * padding;

            // 创建一个新的图片，包含padding
            BufferedImage imageWithAxes = new BufferedImage(
                    newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

            // 设置背景色为白色
            Graphics2D g2d = imageWithAxes.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, newWidth, newHeight);

            // 绘制原始图片到中心位置
            g2d.drawImage(originalImage, padding, padding, null);

            // 设置抗锯齿，使线条更平滑
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 首先绘制网格（在坐标轴下方）
            g2d.setColor(new Color(200, 200, 200, 150)); // 浅灰色，半透明
            g2d.setStroke(new BasicStroke(1));

            // 绘制水平网格线（每50像素一条）
            for (int y = padding + 50; y <= padding + originalHeight; y += 50) {
                g2d.drawLine(padding, y, padding + originalWidth, y);
            }

            // 绘制垂直网格线（每50像素一条）
            for (int x = padding + 50; x <= padding + originalWidth; x += 50) {
                g2d.drawLine(x, padding, x, padding + originalHeight);
            }

            // 设置坐标轴颜色和粗细
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));

            // 计算原点位置（左上角padding处）
            int originX = padding;
            int originY = padding;

            // 绘制x轴（从原点向右延伸）
            g2d.drawLine(originX, originY, originX + originalWidth + 10, originY);

            // 绘制y轴（从原点向下延伸）
            g2d.drawLine(originX, originY, originX, originY + originalHeight + 10);

            // 设置刻度颜色和字体
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(1));
            g2d.setFont(new Font("黑体", Font.PLAIN, 14));

            // 添加x轴刻度（每50像素一个刻度）
            for (int x = 0; x <= originalWidth; x += 50) {
                int tickX = originX + x;

                // 绘制刻度线（向下延伸，在图片外侧）
                g2d.drawLine(tickX, originY, tickX, originY - 5);

                // 绘制刻度值（在刻度线上方）
                if (x > 0) { // 原点不显示0刻度
                    g2d.drawString(String.valueOf(x), tickX - 8, originY - 10);
                }
            }

            // 添加y轴刻度（每50像素一个刻度）
            for (int y = 0; y <= originalHeight; y += 50) {
                int tickY = originY + y;

                // 绘制刻度线（向左延伸，在图片外侧）
                g2d.drawLine(originX, tickY, originX - 5, tickY);

                // 绘制刻度值（在刻度线左侧）
                if (y > 0) { // 原点不显示0刻度
                    g2d.drawString(String.valueOf(y), originX - 30, tickY + 5);
                }
            }

            // 标记原点
            g2d.setColor(Color.GREEN);
            g2d.fillOval(originX - 3, originY - 3, 6, 6);
            g2d.drawString("O(0,0)", originX - 20, originY - 10);

            // 添加目标点
            g2d.fillOval(padding + coordinate.getX() - 3, padding + coordinate.getY() - 3, 6, 6);
            g2d.drawString("P(%s,%s)".formatted(coordinate.getX(), coordinate.getY()), padding + coordinate.getX() - 20, padding + coordinate.getY() - 10);

            // 添加坐标轴标签
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("黑体", Font.BOLD, 14));

            // X轴标签（在x轴末端下方）
            g2d.drawString("X轴", originX + originalWidth + 25, originY + 5);

            // Y轴标签（在y轴末端左侧）
            g2d.drawString("Y轴", originX - 10, originY + originalHeight + 40);

            // 添加箭头
            drawArrow(g2d, originX + originalWidth + 20, originY, 10, 0); // X轴箭头
            drawArrow(g2d, originX, originY + originalHeight + 20, 0, 10); // Y轴箭头

            // 释放资源
            g2d.dispose();

            // 保存图片
            ImageIO.write(imageWithAxes, outputFormat, outputImage);

            System.out.println("坐标系建立完成: ");
            System.out.println("原始尺寸: " + originalWidth + "x" + originalHeight);
        } catch (IOException e) {
            throw new RuntimeException("添加坐标系出错", e);
        }
    }

    public static void addAxesToImage(InputStream inputImage, OutputStream outputImage, String outputFormat) {
        try {
            // 读取原始图片
            BufferedImage originalImage = ImageIO.read(inputImage);
            if (originalImage == null) {
                throw new IOException("无法读取图片");
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // 计算新图片的尺寸（添加padding）
            int padding = 50;
            int newWidth = originalWidth + 2 * padding;
            int newHeight = originalHeight + 2 * padding;

            // 创建一个新的图片，包含padding
            BufferedImage imageWithAxes = new BufferedImage(
                    newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

            // 设置背景色为白色
            Graphics2D g2d = imageWithAxes.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, newWidth, newHeight);

            // 绘制原始图片到中心位置
            g2d.drawImage(originalImage, padding, padding, null);

            // 设置抗锯齿，使线条更平滑
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 首先绘制网格（在坐标轴下方）
            g2d.setColor(new Color(200, 200, 200, 150)); // 浅灰色，半透明
            g2d.setStroke(new BasicStroke(1));

            // 绘制水平网格线（每50像素一条）
            for (int y = padding + 50; y <= padding + originalHeight; y += 50) {
                g2d.drawLine(padding, y, padding + originalWidth, y);
            }

            // 绘制垂直网格线（每50像素一条）
            for (int x = padding + 50; x <= padding + originalWidth; x += 50) {
                g2d.drawLine(x, padding, x, padding + originalHeight);
            }

            // 设置坐标轴颜色和粗细
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));

            // 计算原点位置（左上角padding处）
            int originX = padding;
            int originY = padding;

            // 绘制x轴（从原点向右延伸）
            g2d.drawLine(originX, originY, originX + originalWidth + 10, originY);

            // 绘制y轴（从原点向下延伸）
            g2d.drawLine(originX, originY, originX, originY + originalHeight + 10);

            // 设置刻度颜色和字体
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(1));
            g2d.setFont(new Font("黑体", Font.PLAIN, 14));

            // 添加x轴刻度（每50像素一个刻度）
            for (int x = 0; x <= originalWidth; x += 50) {
                int tickX = originX + x;

                // 绘制刻度线（向下延伸，在图片外侧）
                g2d.drawLine(tickX, originY, tickX, originY - 5);

                // 绘制刻度值（在刻度线上方）
                if (x > 0) { // 原点不显示0刻度
                    g2d.drawString(String.valueOf(x), tickX - 8, originY - 10);
                }
            }

            // 添加y轴刻度（每50像素一个刻度）
            for (int y = 0; y <= originalHeight; y += 50) {
                int tickY = originY + y;

                // 绘制刻度线（向左延伸，在图片外侧）
                g2d.drawLine(originX, tickY, originX - 5, tickY);

                // 绘制刻度值（在刻度线左侧）
                if (y > 0) { // 原点不显示0刻度
                    g2d.drawString(String.valueOf(y), originX - 30, tickY + 5);
                }
            }

            // 标记原点
            g2d.setColor(Color.GREEN);
            g2d.fillOval(originX - 3, originY - 3, 6, 6);
            g2d.drawString("O(0,0)", originX - 20, originY - 10);

            // 添加坐标轴标签
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("黑体", Font.BOLD, 14));

            // X轴标签（在x轴末端下方）
            g2d.drawString("X轴", originX + originalWidth + 25, originY + 5);

            // Y轴标签（在y轴末端左侧）
            g2d.drawString("Y轴", originX - 10, originY + originalHeight + 40);

            // 添加箭头
            drawArrow(g2d, originX + originalWidth + 20, originY, 10, 0); // X轴箭头
            drawArrow(g2d, originX, originY + originalHeight + 20, 0, 10); // Y轴箭头

            // 释放资源
            g2d.dispose();

            // 保存图片
            ImageIO.write(imageWithAxes, outputFormat, outputImage);

            System.out.println("坐标系建立完成: ");
            System.out.println("原始尺寸: " + originalWidth + "x" + originalHeight);
        } catch (IOException e) {
            throw new RuntimeException("添加坐标系出错", e);
        }
    }

    // 绘制箭头的方法
    private static void drawArrow(Graphics2D g2d, int x, int y, int dx, int dy) {
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(1)); // 不要边缘

        // 绘制箭头线
        g2d.drawLine(x - dx * 2, y - dy * 2, x, y);

        // 绘制箭头头部
        Polygon arrowHead = new Polygon();
        if (dx != 0) { // 水平箭头
            arrowHead.addPoint(x, y);
            arrowHead.addPoint(x - dx - 3, y - 3);
            arrowHead.addPoint(x - dx - 3, y + 3);
        } else { // 垂直箭头
            arrowHead.addPoint(x, y);
            arrowHead.addPoint(x - 3, y - dy - 3);
            arrowHead.addPoint(x + 3, y - dy - 3);
        }
        g2d.fillPolygon(arrowHead);
        g2d.drawPolygon(arrowHead);
    }

    /**
     * 截取图片的正方形区域
     *
     * @param inputImage   输入图片路径
     * @param outputImage  输出图片路径
     * @param centerX      中心点X坐标（Web页面坐标系）
     * @param centerY      中心点Y坐标（Web页面坐标系）
     * @param squareSize   正方形边长
     * @param outputFormat 输出格式（如："jpg", "png"）
     */
    public static void cropSquareArea(InputStream inputImage, OutputStream outputImage,
                                      int centerX, int centerY, int squareSize, String outputFormat) {
        try {
            // 读取原始图片
            BufferedImage originalImage = ImageIO.read(inputImage);
            if (originalImage == null) {
                throw new IOException("无法读取图片");
            }

            // 获取原始图片尺寸
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // 计算正方形区域的左上角坐标
            int halfSize = squareSize / 2;
            int startX = centerX - halfSize;
            int startY = centerY - halfSize;

            // 创建目标图像（白色背景）
            BufferedImage croppedImage = new BufferedImage(squareSize, squareSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = croppedImage.createGraphics();

            // 设置白色背景
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, squareSize, squareSize);

            // 设置渲染质量
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 计算需要从原图复制的区域
            int srcX = Math.max(0, startX);
            int srcY = Math.max(0, startY);
            int srcWidth = Math.min(originalWidth - srcX, squareSize);
            int srcHeight = Math.min(originalHeight - srcY, squareSize);

            // 计算目标图像中的位置
            int destX = Math.max(0, -startX);
            int destY = Math.max(0, -startY);

            // 如果存在可复制的区域，则进行复制
            if (srcWidth > 0 && srcHeight > 0) {
                g.drawImage(originalImage,
                        destX, destY, destX + srcWidth, destY + srcHeight,
                        srcX, srcY, srcX + srcWidth, srcY + srcHeight,
                        null);
            }

            g.dispose();

            // 保存裁剪后的图片
            ImageIO.write(croppedImage, outputFormat, outputImage);

            System.out.println("图片裁剪完成: ");
            System.out.println("原始尺寸: " + originalWidth + "x" + originalHeight);
            System.out.println("裁剪区域: 中心点(" + centerX + "," + centerY + "), 大小" + squareSize + "x" + squareSize);

        } catch (IOException e) {
            throw new RuntimeException("裁剪失败", e);
        }
    }

    public static Coordinate imageSize(InputStream inputImage) {
        try {
            // 读取原始图片
            BufferedImage originalImage = ImageIO.read(inputImage);
            if (originalImage == null) {
                throw new IOException("无法读取图片");
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            final Coordinate coordinate = new Coordinate();
            coordinate.setX(originalWidth);
            coordinate.setY(originalHeight);
            return coordinate;
        } catch (IOException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    public static void addCubesToImage(InputStream inputImage, OutputStream outputImage, String outputFormat) {
        try {
            // 读取原始图片
            BufferedImage originalImage = ImageIO.read(inputImage);
            if (originalImage == null) {
                throw new IOException("无法读取图片");
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            int newWidth = originalWidth;
            int newHeight = originalHeight;

            // 创建一个新的图片，包含padding
            BufferedImage imageWithAxes = new BufferedImage(
                    newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

            // 设置背景色为白色
            Graphics2D g2d = imageWithAxes.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, newWidth, newHeight);

            // 绘制原始图片到起始点位置
            g2d.drawImage(originalImage, 0, 0, null);

            // 设置抗锯齿，使线条更平滑
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 首先绘制网格（在坐标轴下方）
            g2d.setColor(new Color(0, 0, 0, 96)); // 浅灰色，半透明
            g2d.setStroke(new BasicStroke(1));

            // 绘制水平网格线（每50像素一条）
            for (int y = 0; y <= originalHeight; y += 50) {
                g2d.drawLine(0, y, originalWidth, y);
            }

            // 绘制垂直网格线（每50像素一条）
            for (int x = 0; x <= originalWidth; x += 50) {
                g2d.drawLine(x, 0, x, originalHeight);
            }

            // 绘制文字边缘，避免文字溶于背景
            /*g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.setFont(new Font("黑体", Font.BOLD, 14));
            int index = 1;
            for (int y = 0; y < originalHeight; y += 50) {
                for (int x = 0; x < originalWidth; x += 50) {
                    final String str = String.valueOf(index++);
                    final long nums = str.codePoints().count();
                    g2d.drawString(str, x + 25 - (nums - 1) * 5, y + 30);
                }
            }*/

            // 设置刻度颜色和字体
//            g2d.setColor(new Color(0, 0, 0, 96));
//            g2d.setStroke(new BasicStroke(2));
            final Font font = new Font("黑体", Font.BOLD, 14);
            final Color textColor = new Color(0, 0, 0, 96);
            final Color backColor = new Color(255, 255, 255, 128);
            final BasicStroke stroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2d.setFont(font);
            g2d.setColor(textColor);

            int index = 1;
            for (int y = 0; y < originalHeight; y += 50) {
                for (int x = 0; x < originalWidth; x += 50) {
                    final String str = String.valueOf(index++);
                    final long nums = str.codePoints().count();

                    /*// 创建文字形状
                    Shape textShape = font.createGlyphVector(g2d.getFontRenderContext(), str)
                            .getOutline(x + 25 - (nums - 1) * 5, y + 30);

                    // 先绘制边缘
                    g2d.setColor(backColor);
                    g2d.setStroke(stroke);
                    g2d.draw(textShape);

                    // 再填充文字
                    g2d.setColor(textColor);
                    g2d.fill(textShape);*/
                    g2d.drawString(str, x + 25 - (nums - 1) * 5, y + 30);
                }
            }

            // 释放资源
            g2d.dispose();

            // 保存图片
            ImageIO.write(imageWithAxes, outputFormat, outputImage);

            System.out.println("方块编号完成: ");
            System.out.println("原始尺寸: " + originalWidth + "x" + originalHeight);
        } catch (IOException e) {
            throw new RuntimeException("方块编号出错", e);
        }
    }

    /**
     * 高级文字描边效果
     * @param image 目标图片
     * @param text 文字内容
     * @param x X坐标
     * @param y Y坐标
     * @param textColor 文字颜色
     * @param borderColor 边缘颜色
     * @param borderSize 边缘大小
     * @param font 字体
     */
    private static void drawAdvancedTextBorder(BufferedImage image, String text, int x, int y,
                                              Color textColor, Color borderColor, float borderSize, Font font) {
        Graphics2D g = image.createGraphics();

        // 设置高质量渲染
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();

        // 创建文字形状
        Shape textShape = font.createGlyphVector(g.getFontRenderContext(), text).getOutline(x, y);

        // 先绘制边缘
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(borderSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(textShape);

        // 再填充文字
        g.setColor(textColor);
        g.fill(textShape);

        g.dispose();
    }
}
