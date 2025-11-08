package io.renren.modules.app.vo;

import lombok.Data;

@Data
public class UnLockParams {
    private int type;
    private String value;
    private String tips;
    private String resourceId;
    private int source;
}
