package io.renren.modules.app.common;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class OcrUtils {
    public static BufferedImage base64ToBufferedImage(String base64) {
        try {
            // 移除 data URL 前缀（如果有）
            String base64Image = base64.replaceFirst("^data:image/[^;]+;base64,", "");

            // 解码并转换
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert base64 to BufferedImage", e);
        }
    }

    /**
     * 图片增强
     * @param image
     * @return
     * @throws IOException
     */
    public static BufferedImage preprocessImage(BufferedImage image) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();

        // 1. 放大 2 倍（提升小字识别率）
        int scaledW = width * 2;
        int scaledH = height * 2;
        BufferedImage scaledImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(image, 0, 0, scaledW, scaledH, null);
        g2d.dispose();

        // 2. 转灰度图
        BufferedImage grayImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(scaledImage, 0, 0, null);
        g.dispose();

        // 3. 针对性对比度拉伸 + 反色 + 二值化
        //    文字 #f3f3f3 → 灰度≈243 (亮)
        //    背景 #828282 → 灰度≈130 (暗)
        //    步骤: 把 [120,250] 区间线性拉伸到 [0,255]，再反色，再二值化
        //    拉伸后: 背景(130)→~20, 文字(243)→~241
        //    反色后: 背景→~235(白), 文字→~14(黑)  → 完美的「黑字白底」
        int lowClip = 120;   // 略低于背景灰度130
        int highClip = 250;  // 略高于文字灰度243
        int range = highClip - lowClip;

        BufferedImage resultImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < scaledH; y++) {
            for (int x = 0; x < scaledW; x++) {
                int gray = grayImage.getRaster().getSample(x, y, 0);

                // 对比度拉伸: [120, 250] → [0, 255]
                int stretched = (gray - lowClip) * 255 / range;
                stretched = Math.max(0, Math.min(255, stretched));

                // 反色: 亮(文字)→暗, 暗(背景)→亮
                int inverted = 255 - stretched;

                // 二值化: 彻底分离
                int binary = inverted < 128 ? 0 : 255;

                resultImage.getRaster().setSample(x, y, 0, binary);
            }
        }

        return resultImage;
    }

}
