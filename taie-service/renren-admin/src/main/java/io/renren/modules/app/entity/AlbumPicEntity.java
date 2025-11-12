package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("album_pic")
public class AlbumPicEntity extends AppBaseEntity{
    
    private String deviceId;

    private String pkg;
    
    private String base64;
    private long date;

    private String imgPath;
    

}
