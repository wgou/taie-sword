package io.renren;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public class Tess4jDemo {

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






    public static void main(String[] args) throws IOException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/Users/txt/Documents/code/taie-sword/taie-service/tessdata");
        tesseract.setLanguage("chi_sim+eng");
        // 核心优化参数
        tesseract.setPageSegMode(6);           // 统一文本块模式
        tesseract.setOcrEngineMode(1);         // LSTM 引擎
        tesseract.setVariable("preserve_interword_spaces", "1");  // 保留词间空格
//        tesseract.setVariable("tessedit_do_invert", "0");
        tesseract.setVariable("user_defined_dpi", "300");
//        tesseract.setVariable("tessedit_char_whitelist",
//                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
//                        "仅充电用于传输文件照片网络共享"); // 可选：限制识别的字符集，提升速度和准确率
//                        "仅在使用中允许"); // 可选：限制识别的字符集，提升速度和准确率

        BufferedImage image = ImageIO.read(new File("/Users/txt/Downloads/WechatIMG1128.jpeg"));
        BufferedImage bufferedImage = preprocessImage(image);
        ImageIO.write(bufferedImage, "png", new File("/Users/txt/Downloads/WechatIMG1128_out.png"));
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            List<Word> words = tesseract.getWords(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE);
            for (Word word : words) {
                Rectangle bbox = word.getBoundingBox();
                String text = word.getText();
                System.out.println("文字: " + text + ", 位置: " + bbox);
            }
            log.info("耗时:{} ms", System.currentTimeMillis() - start);
        }

    }
}
