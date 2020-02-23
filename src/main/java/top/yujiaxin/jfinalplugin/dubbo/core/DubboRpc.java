package top.yujiaxin.jfinalplugin.dubbo.core;

import org.apache.dubbo.config.utils.ReferenceConfigCache;

import java.util.HashMap;
import java.util.Map;

public class DubboRpc {

	private DubboRpc(){}

    @Deprecated
	public static <T> T receiveService(Class<T> interfaceClass){
		return receiveService(interfaceClass, null, null);
	}

	@Deprecated
	public static <T> T receiveService(Class<T> interfaceClass,String group,String version){
		Map<String,String> config= new HashMap<>();
		config.put("group", group);
		config.put("version", version);
		return receiveService(interfaceClass, config);
	}

	@Deprecated
	public static <T> T receiveService(Class<T> interfaceClass,Map<String,String> config) {
        return ReferenceConfigCache.getCache().get(interfaceClass);//TODO 使用keyGenerator生成key来获取正确缓存的服务
	}
}
