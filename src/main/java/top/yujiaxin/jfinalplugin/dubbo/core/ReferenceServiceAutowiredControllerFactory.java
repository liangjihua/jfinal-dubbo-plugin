package top.yujiaxin.jfinalplugin.dubbo.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.jfinal.core.Controller;
import com.jfinal.core.ControllerFactory;

import top.yujiaxin.jfinalplugin.dubbo.annotation.ReferenceService;

public class ReferenceServiceAutowiredControllerFactory extends ControllerFactory {
	
	private ThreadLocal<Map<Class<? extends Controller>, Controller>> buffers = new ThreadLocal<Map<Class<? extends Controller>, Controller>>() {
		protected Map<Class<? extends Controller>, Controller> initialValue() {
			return new HashMap<Class<? extends Controller>, Controller>();
		}
	};
	@Override
	public Controller getController(Class<? extends Controller> controllerClass)
			throws InstantiationException, IllegalAccessException {
		Controller ret=buffers.get().get(controllerClass);
		if(ret==null){
			ret=controllerClass.newInstance();
			inject(ret);
			buffers.get().put(controllerClass, ret);
		}
		return ret;
	}
	private void inject(Controller ret) throws IllegalAccessException {
		Field[] fields=ret.getClass().getDeclaredFields();
		for (Field field : fields) {
			if(field.isAnnotationPresent(ReferenceService.class)){
				ReferenceService reService=field.getAnnotation(ReferenceService.class);
				Map<String,String> config=buildPara(reService);
				field.setAccessible(true);
				field.set(ret, DubboRpc.receiveService(field.getType(), config));
			}
		}
	}
	public Map<String,String> buildPara(ReferenceService reService) {
		Map<String,String> config=new HashMap<String,String>();
		config.put("version", reService.version());
		config.put("group", reService.group());
		config.put("retries",String.valueOf(reService.retries()));
		config.put("cluster", reService.cluster().toString().toLowerCase());
		config.put("stub", reService.stub());
		config.put("mock", reService.mock());
		config.put("loadbalance", reService.loadbalance().toString().toLowerCase());
		config.put("timeout", String.valueOf(reService.timeout()));
		config.put("connections", String.valueOf(reService.connections()));
		config.put("async", String.valueOf(reService.async()));
		config.put("generic", String.valueOf(reService.generic()));
		config.put("check", String.valueOf(reService.check()));
		config.put("url", reService.url());
		config.put("cache", reService.cache());
		config.put("validation", String.valueOf(reService.validation()));
		config.put("proxy", reService.proxy());
		config.put("client", reService.client());
		config.put("owner", reService.owner());
		config.put("actives", String.valueOf(reService.actives()));
		config.put("filter", reService.filter());
		config.put("listener", reService.listener());
		config.put("layer", reService.layer());
		config.put("init", String.valueOf(reService.init()));
		config.put("protocol", reService.protocol());
		return config;
	}
	
}
