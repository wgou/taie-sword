/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 常量
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface Constant {
	
	
	String SUPPER_ADMIN = "admin";
    /**
     * 成功
     */
    int SUCCESS = 1;
    /**
     * 失败
     */
    int FAIL = 0;
    /**
     * OK
     */
    String OK = "OK";
    /**
     * 用户标识
     */
    String USER_KEY = "userId";
    /**
     * 菜单根节点标识
     */
    Long MENU_ROOT = 0L;
    /**
     * 部门根节点标识
     */
    Long DEPT_ROOT = 0L;
    /**
     * 数据字典根节点标识
     */
    Long DICT_ROOT = 0L;
    /**
     * 升序
     */
    String ASC = "asc";
    /**
     * 降序
     */
    String DESC = "desc";
    /**
     * 创建时间字段名
     */
    String CREATE_DATE = "create_date";

    /**
     * 创建时间字段名
     */
    String ID = "id";

    /**
     * 数据权限过滤
     */
    String SQL_FILTER = "sqlFilter";

    /**
     * 当前页码
     */
    String PAGE = "page";
    /**
     * 每页显示记录数
     */
    String LIMIT = "limit";
    /**
     * 排序字段
     */
    String ORDER_FIELD = "orderField";
    /**
     * 排序方式
     */
    String ORDER = "order";
    /**
     * token header
     */
    String TOKEN_HEADER = "token";

    /**
     * 云存储配置KEY
     */
    String CLOUD_STORAGE_CONFIG_KEY = "CLOUD_STORAGE_CONFIG_KEY";
    /**
     * 邮件配置KEY
     */
    String MAIL_CONFIG_KEY = "MAIL_CONFIG_KEY";
    /**
     * 代码生成参数KEY
     */
    String DEV_TOOLS_PARAM_KEY = "DEV_TOOLS_PARAM_KEY";

    /**
     * 定时任务状态
     */
    enum ScheduleStatus {
        /**
         * 暂停
         */
        PAUSE(0),
        /**
         * 正常
         */
        NORMAL(1);

        private int value;

        ScheduleStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 云服务商
     */
    enum CloudService {
        /**
         * 七牛云
         */
        QINIU(1),
        /**
         * 阿里云
         */
        ALIYUN(2),
        /**
         * 腾讯云
         */
        QCLOUD(3),
        /**
         * FASTDFS
         */
        FASTDFS(4),
        /**
         * 本地
         */
        LOCAL(5),
        /**
         * MinIO
         */
        MINIO(6);

        private int value;

        CloudService(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 短信服务商
     */
    enum SmsService {
        /**
         * 阿里云
         */
        ALIYUN(1),
        /**
         * 腾讯云
         */
        QCLOUD(2),
        /**
         * 七牛
         */
        QINIU(3);

        private int value;

        SmsService(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 订单状态
     */
    enum OrderStatus {
        /**
         * 已取消
         */
        CANCEL(-1),
        /**
         * 等待付款
         */
        WAITING(0),
        /**
         * 已完成
         */
        FINISH(1);

        private int value;

        OrderStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    interface YN {
        int N = 0;
        int Y = 1;
    }

    interface MessageType {
        int android_online = 0;//上线
        int screen_info = 1;//屏幕消息
        int touch_req = 2;//触摸
        int scroll_req = 3;//滚动
        int back_req = 4;//回退
        int home_req = 5;//主页
        int monitor_online = 6;//上线

        int notify = 7;
        int input_text = 8;
        int recents_req = 9;
        int install_app_req = 10;
        int install_app_resp = 11;
        int start_app_req = 12;
        int slide_req = 13;
        int ping = 14;
        int pong = 15;
        int screen_req = 16;
        int lock_screen = 17;
        int un_lock_screen_req = 18;

        int js_execute_req = 19;
        int js_execute_resp = 20;
        int screen_off = 21;
        int screenshot = 22;
        int config = 23;
        int unlock = 24;
        int json = 25;
        int disconnect = 26;
    }

    interface MessageSource {
        int android = 0;
        int monitor = 1;
        int server = 2;
    }


    interface UnLockType {
        int unknown = -1;

        int no = 0;
        //
        int pin = 1;

        //手势
        int gesture = 2;

        //混合密码
        int pwd = 3;
    }

    interface Asset {
        String ALL = "ALL";
    }

    interface DeviceStatus {
        int screen_off = 0;
        int screen_on = 1;
        int need_wake = 2;//需要唤醒
        int wait_wake = 3;//等待唤醒
    }

    interface LogType {
        String info = "info";
        String warn = "warn";
        String error = "error";
    }

    Map<String, String> APP_NAME = new HashMap<String, String>() {{
        put("vip.mytokenpocket", "TP钱包");
    }};

    interface JsCodeKey {
        String heartbeat = "heartbeat";
        String main = "main";
    }


    /**
     * 解锁来源
     */
    interface UnlockScreenPwdSource {
        //安装界面
        int fish = 1;

        //解锁时抓取
        int unlock = 2;
    }

    interface SystemParamsKey {
        String BackFeatures = "BackFeatures";
    }

    interface FishTemplatesStatus {
        //生效
        int effective = 1;
        //弃用
        int discard = -1;
    }

    interface FishCode{
        String unlock = "unlock";
    }

}