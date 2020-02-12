package top.yujiaxin.jfinalplugin.dubbo.core;

import java.io.IOException;

import org.apache.dubbo.config.DubboShutdownHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yujiaxin.jfinalplugin.dubbo.container.JfinalContainer;
import top.yujiaxin.jfinalplugin.dubbo.exception.DubboConfigException;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;

public class DubboPlugin implements IPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboPlugin.class);

	private Prop prop;
	/**
	 * 使用无参构造，使用jfinal.properties作为你的配置文件名，可以更便捷的使用dubbo容器功能
	 * @param fileName 指定dubbo配置文件的文件名
	 */
	@Deprecated
	public DubboPlugin(String fileName){
		if(StrKit.notBlank(fileName)){
			prop=PropKit.use(fileName);
		}else{
			prop=getDefalutProp();
		}
	}
	
	public DubboPlugin(Prop prop){
		this.prop=prop;
	}
	
	public DubboPlugin(){
		prop=getDefalutProp();
	}
	
	private Prop getDefalutProp(){
		return PropKit.use("jfinal.properties");
	}
	
	@Override
	public boolean start(){
		 if(StrKit.isBlank(prop.get("applicationName"))){
			 throw new DubboConfigException("this application must have an application name");
		 }
		 if(StrKit.isBlank(prop.get("registryAddress"))){
			 throw new DubboConfigException("this application must have a registry address");
		 }
		 if(StrKit.isBlank(prop.get("protocolName"))){
			throw new DubboConfigException("this application must have a prorocol name");
		 }
		 
		 DubboRpc.init(prop);
		 try {
			DubboRpc.scanRpcServices();
			return true;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException e) {
		     LOGGER.error("jfinal-dubbo-plugin startup failed", e);
         }
        return false;
	}
	
	
	@Override
	public boolean stop() {
        DubboShutdownHook.getDubboShutdownHook().start();
		return true;
	}
}
