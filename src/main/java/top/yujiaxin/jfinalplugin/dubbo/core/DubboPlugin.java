package top.yujiaxin.jfinalplugin.dubbo.core;

import com.jfinal.aop.Enhancer;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yujiaxin.jfinalplugin.dubbo.exception.DubboConfigException;
import top.yujiaxin.jfinalplugin.dubbo.support.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

public class DubboPlugin implements IPlugin {
    private static final Logger logger = LoggerFactory.getLogger(DubboPlugin.class);
    private String[] scanBasePackages;
    private DubboBootstrap bootstrap = DubboBootstrap.getInstance();

    /**
     * @param scanBasePackages 要扫描被{@link Service}注解的服务类的基础包
     */
    public DubboPlugin(String[] scanBasePackages) {
        Objects.requireNonNull(scanBasePackages);
        this.scanBasePackages = scanBasePackages;
    }

	@Override
	public boolean start(){
		 try {
             addConfigCenterConfigIfItBeenConfigured();
             for (String scanBasePackage : scanBasePackages) {
                 List<Class> classes = ClassUtils.scanClass(scanBasePackage);
                 for (Class<?> cl : classes) {
                     if (cl.isInterface() || Modifier.isAbstract(cl.getModifiers()))continue;
                     addServiceConfig(cl);
                     addReferenceConfig(cl);
                 }
             }
             bootstrap.start();
			return true;
		} catch (ClassNotFoundException | IOException e) {
		     logger.error("jfinal-dubbo-plugin startup failed", e);
         }
        return false;
	}

    private void addConfigCenterConfigIfItBeenConfigured() {
        ConfigCenterConfig configCenterConfig = new ConfigCenterConfig();
        configCenterConfig.refresh();
        if (StrKit.notBlank(configCenterConfig.getAddress())){
            bootstrap.configCenter(configCenterConfig);
        }
    }

    @SuppressWarnings("unchecked")
    private void addServiceConfig(Class<?> cl) {
        Service service = cl.getAnnotation(Service.class);
        if(service == null) return;
        Class<?>[] interfaces = cl.getInterfaces();
        if(interfaces==null || !(interfaces.length > 0)){
            logger.error("Service[{}] must implements a interface!", cl.getName());
            throw new DubboConfigException(String.format("Service[%s] does not implement an interface", cl.getName()));
        }
        Object serviceInstance = Enhancer.enhance(cl);
        for (Class in : interfaces) {
            ServiceConfig serviceConfig = new ServiceConfig(service);
            serviceConfig.setInterface(in);
            serviceConfig.setRef(serviceInstance);
            bootstrap.service(serviceConfig);
        }
    }

    private void addReferenceConfig(Class<?> cl){
        Field[] fields = cl.getDeclaredFields();
        for(Field f : fields){
            Reference reference= f.getAnnotation(Reference.class);
            if(reference==null)continue;
            ReferenceConfig<?> referenceConfig = new ReferenceConfig<>(reference);
            referenceConfig.setInterface(f.getType());
            bootstrap.reference(referenceConfig);//TODO 应该缓存属性相同的ReferenceConfig实例，防止重复创建Config
                                                 //TODO 导致重复get()相同的服务代理实例
                                                 //TODO {@link ReferenceAnnotationBeanPostProcessor}
        }
    }

	@Override
	public boolean stop() {
        bootstrap.stop();
		return true;
	}
}
