package io.renren.modules.app.context;

public class DeviceContext {
	   private static final ThreadLocal<String> deviceHolder = new ThreadLocal<>();
	   private static final ThreadLocal<String> pkgHolder = new ThreadLocal<>();
	    
	    public static void setDeviceId(String deviceId) {
	    	deviceHolder.set(deviceId);
	    }

	    public static String getDeviceId() {
	        return deviceHolder.get();
	    }
	    
	    public static void setPkg(String pkg) {
	    	pkgHolder.set(pkg);
	    }

	    public static String getPkg() {
	        return pkgHolder.get();
	    }

	    public static void clear() {
	    	deviceHolder.remove();
	    	pkgHolder.remove();
	    	
	    }
}
