package io.renren.modules.app.entity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Date;

/**
 * 时间戳反序列化器
 * 支持秒级和毫秒级时间戳自动转换为Date类型
 */
public class TimestampDeserializer extends JsonDeserializer<Date> {
    
    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        long timestamp = p.getValueAsLong();
        
        // 判断是秒级还是毫秒级时间戳
        // 秒级时间戳通常是10位数字，毫秒级是13位数字
        if (timestamp < 10000000000L) {
            // 秒级时间戳，转换为毫秒
            timestamp = timestamp * 1000;
        }
        
        return new Date(timestamp);
    }
}
