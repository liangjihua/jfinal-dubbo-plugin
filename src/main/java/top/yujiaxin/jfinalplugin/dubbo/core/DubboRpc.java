package top.yujiaxin.jfinalplugin.dubbo.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.jfinal.aop.Enhancer;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.StrKit;

import top.yujiaxin.jfinalplugin.dubbo.annotation.RpcService;
import top.yujiaxin.jfinalplugin.dubbo.exception.RpcServiceReferenceException;

public class DubboRpc {
	
	private static ApplicationConfig applicationConfig=new ApplicationConfig();
	
	private static RegistryConfig registryConfig=new RegistryConfig();
	
	private static ProtocolConfig protocolConfig=new ProtocolConfig();
	
	private static Map<Object,Object> serviceCache=new HashMap<Object,Object>();
	
	private static String classPath=PathKit.getRootClassPath();
	
	private static String dubbo_base_package = "";
	
	private static ProviderConfig providerConfig=new ProviderConfig();
	
	private DubboRpc(){};
	
	public static void init(Prop prop) { 
		applicationConfig.setName(prop.get("applicationName"));
		registryConfig.setAddress(prop.get("registryAddress"));
		protocolConfig.setName(prop.get("protocolName"));
		if(StrKit.notBlank(prop.get("applicationVersion"))){
			applicationConfig.setVersion(prop.get("applicationVersion"));
		}
		if(StrKit.notBlank(prop.get("registryUsername"))){
			 registryConfig.setUsername(prop.get("registryUsername"));
		 }
		if(StrKit.notBlank(prop.get("registryPassword"))){
			 registryConfig.setPassword(prop.get("registryPassword"));
		}
		if(StrKit.notBlank(prop.get("protocolPort"))){
			 protocolConfig.setPort(prop.getInt("protocolPort"));
		 }
		 if(StrKit.notBlank(prop.get("protocolThreads"))){
			 protocolConfig.setThreads(prop.getInt("protocolThreads"));
		 }
		 if(StrKit.notBlank(prop.get("token"))){
			 providerConfig.setToken(prop.get("token"));
		 }
		 if(StrKit.notBlank(prop.get("dubbo_base_package"))){
			 dubbo_base_package=prop.get("dubbo_base_package");
		 }
	}
	
	public static <T> T receiveService(Class<T> interfaceClass){
		return receiveService(interfaceClass, null, null);
	}
	
	public static <T> T receiveService(Class<T> interfaceClass,String group,String version){
		Map<String,String> config=new HashMap<String,String>();
		config.put("group", group);
		config.put("version", version);
		return receiveService(interfaceClass, config);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T receiveService(Class<T> interfaceClass,Map<String,String> config) {
		T service=(T) serviceCache.get(interfaceClass.getSimpleName()+":"+JsonKit.toJson(config));
		if(service!=null){
			return service;
		}
		ReferenceConfig<T> referenceConfig = buildReferenceConfig(interfaceClass, config);
		service=referenceConfig.get();
		if(service==null){
			throw new RpcServiceReferenceException("Did not get rpc:"+interfaceClass.getName());
		}
		serviceCache.put(interfaceClass.getSimpleName()+":"+JsonKit.toJson(config), service);
		return service;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> void exportServices() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
		List<Class> classList=new ArrayList<Class>();
		scanClass(classList, classPath);
		for (Class cl : classList) {
			if (cl.isInterface() || Modifier.isAbstract(cl.getModifiers())) {
                continue;
            }
			RpcService rpcService=(RpcService) cl.getAnnotation(RpcService.class);
			if(rpcService==null)continue;
			Class[] interfaces = cl.getInterfaces();
			if(interfaces==null||!(interfaces.length>0)){
				throw new RpcServiceReferenceException("RpcService must implements a interface");
			}
			for (Class in : interfaces) {
				ServiceConfig<T> service = buildServiceConfig(cl, rpcService, in);
				service.setProvider(providerConfig);
				service.export();
			}
		}
		
	}
	
	public static <T> ReferenceConfig<T> buildReferenceConfig(Class<T> interfaceClass, Map<String, String> config) {
		ReferenceConfig<T> referenceConfig=new ReferenceConfig<T>();
		referenceConfig.setApplication(applicationConfig);
		referenceConfig.setRegistry(registryConfig);
		referenceConfig.setInterface(interfaceClass);
		if(StrKit.notBlank(config.get("group"))){
			referenceConfig.setGroup(config.get("group"));
		}
		if(StrKit.notBlank(config.get("version"))){
			referenceConfig.setVersion(config.get("version"));
		}
		if(StrKit.notBlank(config.get("cache"))){
			referenceConfig.setCache(config.get("cache"));
		}
		if(StrKit.notBlank(config.get("check"))){
			referenceConfig.setCheck(Boolean.valueOf(config.get("check")));
		}
		if(StrKit.notBlank(config.get("retries"))){
			referenceConfig.setRetries(Integer.valueOf(config.get("retries")));
		}
		if(StrKit.notBlank(config.get("cluster"))){
			referenceConfig.setCluster(config.get("cluster"));
		}
		if(StrKit.notBlank(config.get("stub"))){
			referenceConfig.setStub(config.get("stub"));
		}
		if(StrKit.notBlank(config.get("mock"))){
			referenceConfig.setMock(config.get("mock"));
		}
		if(StrKit.notBlank(config.get("loadbalance"))){
			referenceConfig.setLoadbalance(config.get("loadbalance"));
		}
		if(StrKit.notBlank(config.get("timeout"))){
			referenceConfig.setTimeout(Integer.valueOf(config.get("timeout")));
		}
		if(StrKit.notBlank(config.get("connections"))){
			referenceConfig.setConnections(Integer.valueOf(config.get("connections")));
		}
		if(StrKit.notBlank(config.get("async"))){
			referenceConfig.setAsync(Boolean.valueOf(config.get("async")));
		}
		if(StrKit.notBlank(config.get("generic"))){
			referenceConfig.setGeneric(config.get("generic"));
		}
		if(StrKit.notBlank(config.get("url"))){
			referenceConfig.setUrl(config.get("url"));
		}
		if(StrKit.notBlank(config.get("validation"))){
			referenceConfig.setValidation(config.get("validation"));
		}
		if(StrKit.notBlank(config.get("proxy"))){
			referenceConfig.setProxy(config.get("proxy"));
		}
		if(StrKit.notBlank(config.get("client"))){
			referenceConfig.setClient(config.get("client"));
		}
		if(StrKit.notBlank(config.get("owner"))){
			referenceConfig.setOwner(config.get("owner"));
		}
		if(StrKit.notBlank(config.get("actives"))){
			referenceConfig.setActives(Integer.valueOf(config.get("actives")));
		}
		if(StrKit.notBlank(config.get("filter"))){
			referenceConfig.setFilter(config.get("filter"));
		}
		if(StrKit.notBlank(config.get("listener"))){
			referenceConfig.setListener(config.get("listener"));
		}
		if(StrKit.notBlank(config.get("layer"))){
			referenceConfig.setLayer(config.get("layer"));
		}
		if(StrKit.notBlank(config.get("init"))){
			referenceConfig.setInit(Boolean.valueOf(config.get("init")));
		}
		if(StrKit.notBlank(config.get("protocol"))){
			referenceConfig.setProtocol(config.get("protocol"));
		}
		return referenceConfig;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ServiceConfig<T> buildServiceConfig(Class<?> cl, RpcService rpcService, Class<?> in)
			throws InstantiationException, IllegalAccessException {
		ServiceConfig<T> service = new ServiceConfig<T>();
		service.setApplication(applicationConfig);
		service.setRegistry(registryConfig);
		service.setProtocol(protocolConfig);
		service.setInterface(in);
		if(rpcService.needEnhancer()){
			service.setRef((T)Enhancer.enhance(cl));
		}else{
			service.setRef((T)cl.newInstance());
		}
		if(StrKit.notBlank(rpcService.token())){
			service.setToken(rpcService.token());
		}
		if(StrKit.notBlank(rpcService.stub())){
			service.setStub(rpcService.stub());
		}
		if(StrKit.notBlank(rpcService.mock())){
			service.setMock(rpcService.mock());
		}
		if(StrKit.notBlank(rpcService.accesslog())){
			if(rpcService.accesslog().toLowerCase().equals("true")||rpcService.accesslog().toLowerCase().equals("false")){
				service.setAccesslog(Boolean.valueOf(rpcService.accesslog()));
			}else{
				service.setAccesslog(rpcService.accesslog());
			}
		}
		if(StrKit.notBlank(rpcService.document())){
			service.setDocument(rpcService.document());
		}
		if(StrKit.notBlank(rpcService.filter())){
			service.setFilter(rpcService.filter());
		}
		if(StrKit.notBlank(rpcService.layer())){
			service.setFilter(rpcService.filter());
		}
		if(StrKit.notBlank(rpcService.listener())){
			service.setListener(rpcService.listener());
		}
		if(StrKit.notBlank(rpcService.owner())){
			service.setOwner(rpcService.owner());
		}
		if(StrKit.notBlank(rpcService.path())){
			service.setPath(rpcService.path());
		}
		if(StrKit.notBlank(rpcService.proxy())){
			service.setProxy(rpcService.proxy());
		}
		if(StrKit.notBlank(rpcService.version())){
			service.setVersion(rpcService.version());
		}
		if(StrKit.notBlank(rpcService.group())){
			service.setGroup(rpcService.group());
		}
		service.setCluster(rpcService.cluster().toString().toLowerCase());
		service.setLoadbalance(rpcService.loadbalance().toString().toLowerCase());
		service.setTimeout(rpcService.timeout());
		service.setExecutes(rpcService.executes());
		service.setDynamic(rpcService.dynamic());
		service.setDeprecated(rpcService.deprecated());
		service.setDelay(rpcService.delay());
		service.setConnections(rpcService.connections());
		service.setAsync(rpcService.async());
		service.setActives(rpcService.actives());
		service.setWeight(rpcService.weight());
		service.setRegister(rpcService.register());
		service.setRetries(rpcService.retries());
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
            String protocol = url.getProtocol();//大概是jar
            if ("jar".equalsIgnoreCase(protocol)) {
                //转换为JarURLConnection
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                if (connection != null) {
                    JarFile jarFile = connection.getJarFile();
                    if (jarFile != null) {
                        //得到该jar文件下面的类实体
                        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                        while (jarEntryEnumeration.hasMoreElements()) {
                            JarEntry entry = jarEntryEnumeration.nextElement();
                            String jarEntryName = entry.getName();
                            //这里我们需要过滤不是class文件和不在basePack包名下的类
                            if (jarEntryName.contains(".class") && jarEntryName.replaceAll("/",".").startsWith(dubbo_base_package)) {
                                String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
                                classList.add(Class.forName(className));
                            }
                        }
                    }
                }
            }
        }
	}
}
