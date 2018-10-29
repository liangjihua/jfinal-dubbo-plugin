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
import java.util.jar.JarEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import top.yujiaxin.jfinalplugin.dubbo.annotation.ReferenceService;
import top.yujiaxin.jfinalplugin.dubbo.annotation.RpcService;
import top.yujiaxin.jfinalplugin.dubbo.exception.RpcServiceReferenceException;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
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

public class DubboRpc {
	private static final Logger logger=LoggerFactory.getLogger(DubboRpc.class);
	
	private static ApplicationConfig applicationConfig=new ApplicationConfig();
	
	private static RegistryConfig registryConfig=new RegistryConfig();
	
	private static ProtocolConfig protocolConfig=new ProtocolConfig();
	
	private static ProviderConfig providerConfig=new ProviderConfig();
	
	private static Map<Object,Object> serviceCache=new HashMap<Object,Object>();
	
	private static String classPath=PathKit.getRootClassPath();
	
	private static String dubbo_base_package = "";
	
	private static Boolean initLoad = false;
	
	private static Prop config;
	
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
		 if(StrKit.notBlank(prop.get("initLoad"))){
			 initLoad =  prop.getBoolean("initLoad");
		 }
		 config=prop;
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
		String configKey = JsonKit.toJson(config);
		T service=(T) serviceCache.get(interfaceClass.getSimpleName()+":"+configKey);
		if(service!=null)return service;
		ReferenceConfig<T> referenceConfig = buildReferenceConfig(interfaceClass, config);
		service=referenceConfig.get();
		if(service==null){
			logger.error("Did not get rpc service：{}",interfaceClass.getName());
			throw new RpcServiceReferenceException("Did not get rpc:"+interfaceClass.getName());
		}
		serviceCache.put(interfaceClass.getSimpleName()+":"+configKey, service);
		return service;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static <T> void scanRpcServices() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
		List<Class> classList=new ArrayList<Class>();
		scanClass(classList, classPath);
		for (Class cl : classList) {
			if (cl.isInterface() || Modifier.isAbstract(cl.getModifiers()))continue;
			exportService(cl);
			if(initLoad)loadReferenceServices(cl);
		}
	}
	
	private static <T> void exportService(Class<?> cl) throws InstantiationException, IllegalAccessException{
		RpcService rpcService=(RpcService) cl.getAnnotation(RpcService.class);
		if(rpcService==null)return;
		Class<?>[] interfaces = cl.getInterfaces();
		if(interfaces==null||!(interfaces.length>0)){
			logger.error("RpcService must implements a interface");
			throw new RpcServiceReferenceException("RpcService must implements a interface");
		}
		for (Class<?> in : interfaces) {
			ServiceConfig<T> service = buildServiceConfig(cl, rpcService, in);
			service.setProvider(providerConfig);
			service.export();
		}
	}
	
	private static <T> void loadReferenceServices(Class<?> cl) throws InstantiationException, IllegalAccessException{
		Field[] fields = cl.getDeclaredFields();
        for(Field f : fields){
            ReferenceService referenceService=(ReferenceService) f.getAnnotation(ReferenceService.class);
            if(referenceService==null)continue;
            receiveService(f.getType(), buildPara(referenceService));
        }
	}
	
	public static ConsumerConfig buildConsumerConfig(){
		ConsumerConfig consumerConfig=new ConsumerConfig();
		if (StrKit.notBlank(config.get("dubbo.consumer.actives"))) {
			consumerConfig.setActives(config.getInt("dubbo.consumer.actives"));
		}
		consumerConfig.setApplication(applicationConfig);
		if (StrKit.notBlank(config.get("dubbo.consumer.async"))) {
			consumerConfig.setAsync(config.getBoolean("dubbo.consumer.async"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.cache"))) {
			consumerConfig.setCache(config.get("dubbo.consumer.cache"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.callbacks"))) {
			consumerConfig.setCallbacks(config.getInt("dubbo.consumer.callbacks"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.check"))) {
			consumerConfig.setCheck(config.getBoolean("dubbo.consumer.check"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.client"))) {
			consumerConfig.setClient(config.get("dubbo.consumer.client"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.cluster"))) {
			consumerConfig.setCluster(config.get("dubbo.consumer.cluster"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.connections"))) {
			consumerConfig.setConnections(config.getInt("dubbo.consumer.connections"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.isDefault"))) {
			consumerConfig.setDefault(config.getBoolean("dubbo.consumer.isDefault"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.filter"))) {
			consumerConfig.setFilter(config.get("dubbo.consumer.filter"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.generic"))) {
			consumerConfig.setGeneric(config.get("dubbo.consumer.generic"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.group"))) {
			consumerConfig.setGroup(config.get("dubbo.consumer.group"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.id"))) {
			consumerConfig.setId(config.get("dubbo.consumer.id"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.init"))) {
			consumerConfig.setInit(config.getBoolean("dubbo.consumer.init"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.layer"))) {
			consumerConfig.setLayer(config.get("dubbo.consumer.layer"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.lazy"))) {
			consumerConfig.setLazy(config.getBoolean("dubbo.consumer.lazy"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.listener"))) {
			consumerConfig.setListener(config.get("dubbo.consumer.listener"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.loadbalance"))) {
			consumerConfig.setLoadbalance(config.get("dubbo.consumer.loadbalance"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.merger"))) {
			consumerConfig.setMerger(config.get("dubbo.consumer.merger"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.mock"))) {
			consumerConfig.setMock(config.get("dubbo.consumer.mock"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.monitor"))) {
			consumerConfig.setMonitor(config.get("dubbo.consumer.monitor"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.onconnect"))) {
			consumerConfig.setOnconnect(config.get("dubbo.consumer.onconnect"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.ondisconnect"))) {
			consumerConfig.setOndisconnect(config.get("dubbo.consumer.ondisconnect"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.owner"))) {
			consumerConfig.setOwner(config.get("dubbo.consumer.owner"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.proxy"))) {
			consumerConfig.setProxy(config.get("dubbo.consumer.proxy"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.reconnect"))) {
			consumerConfig.setReconnect(config.get("dubbo.consumer.reconnect"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.retries"))) {
			consumerConfig.setRetries(config.getInt("dubbo.consumer.retries"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.scope"))) {
			consumerConfig.setScope(config.get("dubbo.consumer.scope"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.sent"))) {
			consumerConfig.setSent(config.getBoolean("dubbo.consumer.sent"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.sticky"))) {
			consumerConfig.setSticky(config.getBoolean("dubbo.consumer.sticky"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.stub"))) {
			consumerConfig.setStub(config.get("dubbo.consumer.stub"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.timeout"))) {
			consumerConfig.setTimeout(config.getInt("dubbo.consumer.timeout"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.validation"))) {
			consumerConfig.setValidation(config.get("dubbo.consumer.validation"));
		}
		if (StrKit.notBlank(config.get("dubbo.consumer.version"))) {
			consumerConfig.setVersion(config.get("dubbo.consumer.version"));
		}
		return consumerConfig;
	}
	

	public static <T> ReferenceConfig<T> buildReferenceConfig(Class<?> interfaceClass, Map<String, String> config) {
		ReferenceConfig<T> referenceConfig=new ReferenceConfig<T>();
		referenceConfig.setApplication(applicationConfig);
		referenceConfig.setRegistry(registryConfig);
		referenceConfig.setInterface(interfaceClass);
		referenceConfig.setConsumer(buildConsumerConfig());
		if(StrKit.notBlank(config.get("interfaceName"))){
			referenceConfig.setInterface(config.get("interfaceName"));
		}
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
