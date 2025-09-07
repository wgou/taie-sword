package io.renren.modules.app.context;

public class DeviceContext {
	  private static final ThreadLocal<String> walletHolder = new ThreadLocal<>();
	    
	    public static void setDeviceId(String deviceId) {
	        walletHolder.set(deviceId);
	    }

	    public static String getDeviceId() {
	        return walletHolder.get();
	    }

	    public static void clear() {
	        walletHolder.remove();
	    }
}
