package io.renren.modules.app.service.impl;

import io.renren.modules.app.common.OcrUtils;
import io.renren.modules.app.vo.PermissionOpenPositionReq;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@ConfigurationProperties(prefix = "permission")
@Slf4j
@Data
public class PermissionOcrService {

    @Autowired
    private Tesseract tesseract;

    private List<String> words;

    public Point questPermissionOpenPosition(PermissionOpenPositionReq permissionOpenPositionReq) throws IOException {
        String uuid = UUID.randomUUID().toString();
        BufferedImage bufferedImage = OcrUtils.base64ToBufferedImage(permissionOpenPositionReq.getBase64());
        ImageIO.write(bufferedImage, "png", new File("/Users/txt/Downloads/before-" + uuid + ".png"));
        bufferedImage = OcrUtils.preprocessImage(bufferedImage);
        ImageIO.write(bufferedImage, "png", new File("/Users/txt/Downloads/after-" + uuid + ".png"));
        List<Word> wordList = tesseract.getWords(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE);
        Point result = null;
        for (Word word : wordList) {
            String text = word.getText().trim();
            log.info("{} - 识别到的文字:{}", uuid, text);
            if (StringUtils.isNotEmpty(permissionOpenPositionReq.getWord())) {
                if (result == null && text.equals(permissionOpenPositionReq.getWord())) {
                    Rectangle rectangle = word.getBoundingBox();
                    result = new Point(Double.valueOf(rectangle.getCenterX() / 2).intValue(), Double.valueOf(rectangle.getCenterY() / 2).intValue());
                }
            } else {
                if (result == null && words.contains(text)) {
                    Rectangle rectangle = word.getBoundingBox();
                    result = new Point(Double.valueOf(rectangle.getCenterX() / 2).intValue(), Double.valueOf(rectangle.getCenterY() / 2).intValue());
                }
            }


        }
        return result;

    }
}
