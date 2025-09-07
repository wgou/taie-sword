package io.renren.modules.app.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;

import io.renren.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * APP 内部自发转账记录
 * @author Administrator
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "app_transfer_record", autoResultMap = true)
public class AppTransferRecord extends BaseEntity {
    private String deviceId;
    private String appName;
    private String packageName;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String remark;
}
