/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.utils;

import cn.hutool.core.map.MapUtil;
import freemarker.template.Template;
import io.renren.common.exception.RenException;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * 代码生成器工具类
 *
 * @author Mark sunlightcs@gmail.com
 */
public class GenUtils {

    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 获取模板渲染后的内容
     * @param content   模板内容
     * @param dataModel 数据模型
     */
    public static String getTemplateContent(String content, Map<String, Object> dataModel) {
        if(MapUtil.isEmpty(dataModel)){
            return content;
        }

        StringReader reader = new StringReader(content);
        StringWriter sw = new StringWriter();
        try {
            //渲染模板
            String templateName = dataModel.getOrDefault("templateName", "generator").toString();
            Template template = new Template(templateName, reader, null, "utf-8");
            template.process(dataModel, sw);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RenException("渲染模板失败，请检查模板语法", e);
        }

        content = sw.toString();

        IOUtils.closeQuietly(reader);
        IOUtils.closeQuietly(sw);

        return content;
    }
}