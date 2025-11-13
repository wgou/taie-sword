package io.renren.modules.app.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代理信息查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProxyInfoParam extends PkgParam {
    
    /**
     * 代理用户
     */
    private String proxyUser;
}

