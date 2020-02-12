package top.yujiaxin.jfinalplugin.dubbo.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;

import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import top.yujiaxin.jfinalplugin.dubbo.annotation.ReferenceService;
import top.yujiaxin.jfinalplugin.dubbo.annotation.RpcService;
import top.yujiaxin.jfinalplugin.dubbo.exception.RpcServiceReferenceException;
import top.yujiaxin.jfinalplugin.dubbo.support.DubboConfigsFactory;

import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.StrKit;

public class DubboRpc {
	private static final Logger logger=LoggerFactory.getLogger(DubboRpc.class);
	
	private static DubboConfigsFactory dubboConfigsFactory;
	
	private static ConcurrentHashMap<Object, Object> servicesCache= new ConcurrentHashMap<>();
	
	private static String classPath=PathKit.getRootClassPath();
	
	private static String dubbo_base_package = "";
	
	private static Boolean initLoad = false;
	
	private DubboRpc(){}
	
	public static void init(Prop prop) { 
		dubboConfigsFactory=new DubboConfigsFactory(prop.get("dubbo.application.name"),prop.get("dubbo.registry.address"),prop.get("dubbo.protocol.name"));
		dubboConfigsFactory.setApplicationVersionSafety(prop.get("dubbo.application.version"));
		dubboConfigsFactory.setRegistryUsernameSafety(prop.get("dubbo.registry.username"));
		dubboConfigsFactory.setRegistryPasswordSafety(prop.get("dubbo.registry.password"));
		dubboConfigsFactory.setProtocolPortSafety(prop.getInt("dubbo.protocol.port"));
		dubboConfigsFactory.setProtocolThreadsSafety(prop.getInt("dubbo.protocol.threads"));
		dubboConfigsFactory.setProviderTokenSafety(prop.get("dubbo.provider.token"));
		if(StrKit.notBlank(prop.get("dubbo_base_package"))){
			dubbo_base_package=prop.get("dubbo_base_package");
		}
		if(StrKit.notBlank(prop.get("initLoad"))){
			initLoad =  prop.getBoolean("initLoad");
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static <T> void scanRpcServices() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
		List<Class> classList= new ArrayList<>();
		scanClass(classList, classPath);
		for (Class cl : classList) {
			if (cl.isInterface() || Modifier.isAbstract(cl.getModifiers()))continue;
			exportService(cl);
			if(initLoad)loadReferenceServices(cl);
		}
	}
	
	private static <T> void exportService(Class<?> cl) throws InstantiationException, IllegalAccessException{
		RpcService rpcService= cl.getAnnotation(RpcService.class);
		if(rpcService==null)return;
		Class<?>[] interfaces = cl.getInterfaces();
		if(interfaces==null||!(interfaces.length>0)){
			logger.error("RpcService must implements a interface");
			throw new RpcServiceReferenceException("RpcService must implements a interface");
		}
		for (Class<?> in : interfaces) {
			ServiceConfig<T> service = dubboConfigsFactory.createServiceConfig(cl, rpcService, in);
			service.export();
		}
	}
	
	private static <T> void loadReferenceServices(Class<?> cl) {
		Field[] fields = cl.getDeclaredFields();
        for(Field f : fields){
            ReferenceService referenceService= f.getAnnotation(ReferenceService.class);
            if(referenceService==null)continue;
            receiveService(f.getType(), buildPara(referenceService));
        }
	}
	
	public static <T> T receiveService(Class<T> interfaceClass){
		return receiveService(interfaceClass, null, null);
	}
	
	public static <T> T receiveService(Class<T> interfaceClass,String group,String version){
		Map<String,String> config= new HashMap<>();
		config.put("group", group);
		config.put("version", version);
		return receiveService(interfaceClass, config);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T receiveService(Class<T> interfaceClass,Map<String,String> config) {
		String configKey = JsonKit.toJson(config);
		T service=(T) servicesCache.get(interfaceClass.getSimpleName()+":"+configKey);
		if(service!=null)return service;
		ReferenceConfig<T> referenceConfig = dubboConfigsFactory.createReferenceConfig(interfaceClass, config);
		service=referenceConfig.get();
		if(service==null){
			logger.error("Did not get rpc service：{}",interfaceClass.getName());
			throw new RpcServiceReferenceException("Did not get rpc:"+interfaceClass.getName());
		}
		servicesCache.put(interfaceClass.getSimpleName()+":"+configKey, service);
		return service;
	}
	
	@SuppressWarnings("rawtypes")
	private static void scanClass(List<Class> classList,String path) throws ClassNotFoundException, IOException{
		File[] files=new File(path).listFiles();
		if(files!=null&&files.length>0){
			for (File file : files) {
				if(file.isDirectory()){
					scanClass(classList, file.getAbsolutePath());
				}else if(file.getName().endsWith(".class")){
					int start = classPath.length();
		            int end = file.toString().length() - ".class".length();
		            String classFile = file.toString().substring(start + 1, end);
		            String className = classFile.replace(File.separator, ".");
					classList.add(Class.forName(className));
				}
			}
		}
		Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(dubbo_base_package.replace(".", "/"));
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();
            if ("jar".equalsIgnoreCase(url.getProtocol())) {
                //转换为JarURLConnection
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                if (connection == null) return;
                if (connection.getJarFile() == null) return;
                //得到该jar文件下面的类实体
                Enumeration<JarEntry> jarEntryEnumeration = connection.getJarFile().entries();
                while (jarEntryEnumeration.hasMoreElements()) {
                    String jarEntryName = jarEntryEnumeration.nextElement().getName();
                    //这里我们需要过滤不是class文件和不在basePack包名下的类
                    if (jarEntryName.contains(".class") && jarEntryName.replaceAll("/",".").startsWith(dubbo_base_package)) {
                        String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
                        classList.add(Class.forName(className));
                    }
                }
            }
        }
	}
	
	public static Map<String,String> buildPara(ReferenceService reService) {
		Map<String,String> config= new HashMap<>();
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
	
	public static DubboConfigsFactory getDubboConfigsFactory(){
		return dubboConfigsFactory;
	}
}
