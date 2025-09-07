package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


//[{"amount":"-1 USDT","from":"0xBcD0df11082aF0Fd144Ad9D8088ce509Bfb0DBdB","result":1,"submitTime":1731347070661,"submitted":true,"to":"0xbcd0df11082af0fd144ad9d8088ce509bfb0dbdb"}]
@TableName("transfer")
@Data
public class Transfer {
    @TableId
    private Long id;
    private String deviceId;
    private BigDecimal amount;
    private String sender;
    private String receiver;
    private String app;
    private String currency;
    private String walletName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;
}
