package top.yujiaxin.jfinalplugin.dubbo;

import java.io.IOException;

import com.alibaba.dubbo.config.ProtocolConfig;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;

import top.yujiaxin.jfinalplugin.dubbo.core.DubboRpc;
import top.yujiaxin.jfinalplugin.dubbo.exception.DubboConfigException;

public class DubboPlugin implements IPlugin {

	private Prop prop;
	public DubboPlugin(String fileName){
		prop=PropKit.use(fileName);
		
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
