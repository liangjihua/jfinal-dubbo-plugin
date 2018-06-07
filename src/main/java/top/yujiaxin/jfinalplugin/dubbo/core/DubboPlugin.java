package top.yujiaxin.jfinalplugin.dubbo.core;

import java.io.IOException;

import top.yujiaxin.jfinalplugin.dubbo.exception.DubboConfigException;

import com.alibaba.dubbo.config.ProtocolConfig;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;

public class DubboPlugin implements IPlugin {

	private Prop prop;
	/**
	 * 使用无参构造，使用jfinal.properties作为你的配置文件名，可以更便捷的使用dubbo容器功能
	 * @param fileName
	 */
	@Deprecated
	public DubboPlugin(String fileName){
		if(StrKit.notBlank(fileName)){
			prop=PropKit.use(fileName);
		}else{
			prop=PropKit.use("jfinal.properties");
		}
	}
	
	public DubboPlugin(Prop prop){
		this.prop=prop;
	}
	
	public DubboPlugin(){
		prop=PropKit.use("jfinal.properties");
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
			DubboRpc.exportServices();
			return true;
		} catch (ClassNotFoundException e) {
			//TODO 异常处理
			e.printStackTrace();
		} catch (InstantiationException e) {
			//TODO 异常处理
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			//TODO 异常处理
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 异常处理
			e.printStackTrace();
		}
		 return false;
	}
	
	
	@Override
	public boolean stop() {
		ProtocolConfig.destroyAll();
		return true;
	}
}
