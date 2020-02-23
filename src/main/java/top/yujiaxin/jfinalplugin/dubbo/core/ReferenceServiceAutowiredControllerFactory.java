package top.yujiaxin.jfinalplugin.dubbo.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.jfinal.core.Controller;
import com.jfinal.core.ControllerFactory;

import org.apache.dubbo.config.annotation.Reference;

public class ReferenceServiceAutowiredControllerFactory extends ControllerFactory {
	
	private ThreadLocal<Map<Class<? extends Controller>, Controller>> buffers = ThreadLocal.withInitial(HashMap::new);
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
			if(field.isAnnotationPresent(Reference.class)){
			    Reference reference = field.getAnnotation(Reference.class);
			    field.setAccessible(true);
			    field.set(ret, DubboRpc.receiveService(field.getType()));
            }
		}
	}
	
}
