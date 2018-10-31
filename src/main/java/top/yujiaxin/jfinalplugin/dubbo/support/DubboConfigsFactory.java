package top.yujiaxin.jfinalplugin.dubbo.support;

import java.util.Map;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.jfinal.aop.Enhancer;
import com.jfinal.kit.StrKit;

import top.yujiaxin.jfinalplugin.dubbo.annotation.RpcService;

public class DubboConfigsFactory {
	
	private  ApplicationConfig applicationConfig=new ApplicationConfig();
	
	private  RegistryConfig registryConfig=new RegistryConfig();
	
	private  ProtocolConfig protocolConfig=new ProtocolConfig();
	
	private  ProviderConfig providerConfig=new ProviderConfig();
	
	private  ConsumerConfig consumerConfig=new ConsumerConfig();
	
	public DubboConfigsFactory(String applicationName,String registryAddress,String protocolName) {
		applicationConfig.setName(applicationName);
		registryConfig.setAddress(registryAddress);
		protocolConfig.setName(protocolName);
		consumerConfig.setApplication(applicationConfig);
		providerConfig.setApplication(applicationConfig);
	}
	
	@SuppressWarnings("unchecked")
	public <T> ServiceConfig<T> createServiceConfig(Class<?> cl, RpcService rpcService, Class<?> in)
			throws InstantiationException, IllegalAccessException {
		ServiceConfig<T> serviceConfig = new ServiceConfig<T>();
		serviceConfig.setApplication(applicationConfig);
		serviceConfig.setRegistry(registryConfig);
		serviceConfig.setProtocol(protocolConfig);
		serviceConfig.setProvider(providerConfig);
		serviceConfig.setInterface(in);
		if(rpcService.needEnhancer()){
			serviceConfig.setRef((T)Enhancer.enhance(cl));
		}else{
			serviceConfig.setRef((T)cl.newInstance());
		}
		if(StrKit.notBlank(rpcService.token())){
			serviceConfig.setToken(rpcService.token());
		}
		if(StrKit.notBlank(rpcService.stub())){
			serviceConfig.setStub(rpcService.stub());
		}
		if(StrKit.notBlank(rpcService.mock())){
			serviceConfig.setMock(rpcService.mock());
		}
		if(StrKit.notBlank(rpcService.accesslog())){
			serviceConfig.setAccesslog(rpcService.accesslog());
		}
		if(StrKit.notBlank(rpcService.document())){
			serviceConfig.setDocument(rpcService.document());
		}
		if(StrKit.notBlank(rpcService.filter())){
			serviceConfig.setFilter(rpcService.filter());
		}
		if(StrKit.notBlank(rpcService.layer())){
			serviceConfig.setFilter(rpcService.filter());
		}
		if(StrKit.notBlank(rpcService.listener())){
			serviceConfig.setListener(rpcService.listener());
		}
		if(StrKit.notBlank(rpcService.owner())){
			serviceConfig.setOwner(rpcService.owner());
		}
		if(StrKit.notBlank(rpcService.path())){
			serviceConfig.setPath(rpcService.path());
		}
		if(StrKit.notBlank(rpcService.proxy())){
			serviceConfig.setProxy(rpcService.proxy());
		}
		if(StrKit.notBlank(rpcService.version())){
			serviceConfig.setVersion(rpcService.version());
		}
		if(StrKit.notBlank(rpcService.group())){
			serviceConfig.setGroup(rpcService.group());
		}
		serviceConfig.setCluster(rpcService.cluster().toString().toLowerCase());
		serviceConfig.setLoadbalance(rpcService.loadbalance().toString().toLowerCase());
		serviceConfig.setTimeout(rpcService.timeout());
		serviceConfig.setExecutes(rpcService.executes());
		serviceConfig.setDynamic(rpcService.dynamic());
		serviceConfig.setDeprecated(rpcService.deprecated());
		serviceConfig.setDelay(rpcService.delay());
		serviceConfig.setConnections(rpcService.connections());
		serviceConfig.setAsync(rpcService.async());
		serviceConfig.setActives(rpcService.actives());
		serviceConfig.setWeight(rpcService.weight());
		serviceConfig.setRegister(rpcService.register());
		serviceConfig.setRetries(rpcService.retries());
		return serviceConfig;
	}
	
	
	public <T> ReferenceConfig<T> createReferenceConfig(Class<?> interfaceClass, Map<String, String> config) {
		ReferenceConfig<T> referenceConfig=new ReferenceConfig<T>();
		referenceConfig.setApplication(applicationConfig);
		referenceConfig.setRegistry(registryConfig);
		referenceConfig.setConsumer(consumerConfig);
		referenceConfig.setInterface(interfaceClass);
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
	
	public void setApplicationVersionSafety(String applicationVersion){
		if(StrKit.notBlank(applicationVersion)){
			applicationConfig.setVersion(applicationVersion);
		}
	}
	
	public void setRegistryUsernameSafety(String registryUsername){
		if(StrKit.notBlank(registryUsername)){
			 registryConfig.setUsername(registryUsername);
		 }
	}
	
	public void setRegistryPasswordSafety(String registryPassword){
		if(StrKit.notBlank(registryPassword)){
			 registryConfig.setPassword(registryPassword);
		}
	}
	
	public void setProtocolPortSafety(Integer protocolPort){
		if(protocolPort!=null){
			protocolConfig.setPort(protocolPort);
		}
	}
	
	public void setProtocolThreadsSafety(Integer protocolThreads){
		if(protocolThreads!=null){
			protocolConfig.setThreads(protocolThreads);
		}
	}
	
	public void setProviderTokenSafety(String providerToken){
		if(StrKit.notBlank(providerToken)){
			providerConfig.setToken(providerToken);
		}
	}
	
	public ApplicationConfig getApplicationConfig(){
		return applicationConfig;
	}
	
	public RegistryConfig getRegistryConfig(){
		return registryConfig;
	}
	
	public ProtocolConfig getProtocolConfig(){
		return protocolConfig;
	}
	
	public ProviderConfig getProviderConfig(){
		return providerConfig;
	}
}
