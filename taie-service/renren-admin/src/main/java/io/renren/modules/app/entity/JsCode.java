package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Date;

@TableName("ast_js_code")
@Data
public class JsCode {
    @TableId
    private Long id;

    /**
     * 脚本可读的名字 例如:心跳执行脚本
     */
    private String name;
    /**
     * 脚本 标识 例如: heartbeat
     */
    private String identification;
    /**
     * 代码
     */
    private String code;
    /**
     * 代码的 md5  主要用于代码是否更改 减少传输
     */
    private String codeMd5;
    /**
     * 功能说明
     */
    private String remark;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date updateDate;
}
