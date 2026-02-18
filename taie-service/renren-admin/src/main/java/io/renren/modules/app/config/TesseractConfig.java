package io.renren.modules.app.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesseractConfig {
    @Value("${tess4j.datapath}")
    private String dataPath;

    @Bean
    public Tesseract tesseract() {

        Tesseract tesseract = new Tesseract();
        // 设置训练数据文件夹路径
        tesseract.setDatapath(dataPath);
        // 设置为中文简体
        tesseract.setLanguage("chi_sim+eng");
        tesseract.setPageSegMode(6);           // 统一文本块模式
        tesseract.setOcrEngineMode(1);         // LSTM 引擎
        tesseract.setVariable("preserve_interword_spaces", "1");  // 保留词间空格
        tesseract.setVariable("user_defined_dpi", "300");
        return tesseract;
    }
}
