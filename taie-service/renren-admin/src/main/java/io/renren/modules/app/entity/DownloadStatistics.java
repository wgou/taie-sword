package io.renren.modules.app.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 下载统计实体类
 */
@Data
@TableName("ast_statistics")
public class DownloadStatistics extends AppBaseEntity{
    
    
    /**
     * 页面标识（如：sa, dingdong等）
     */
    private String pageCode;
    
    /**
     * 统计类型：1-页面访问，2-下载点击
     */
    private Integer type;
    
    /**
     * 用户IP地址
     */
    private String ip;
    
    /**
     * IP地址所在城市
     */
    private String addr;
    
    /**
     * 用户代理（User-Agent）
     */
    private String userAgent;
    
    /**
     * 来源URL（Referer）
     */
    private String referer;
     
}

