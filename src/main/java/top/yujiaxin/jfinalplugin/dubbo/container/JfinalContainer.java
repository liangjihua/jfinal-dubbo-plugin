package top.yujiaxin.jfinalplugin.dubbo.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.container.Container;
import com.jfinal.config.Constants;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.kit.PropKit;

public class JfinalContainer implements Container {
	private static final Logger logger=LoggerFactory.getLogger(JfinalContainer.class);
	private Constants constants = new Constants();
	private Plugins plugins = new Plugins();
	private Interceptors interceptors = new Interceptors();
	private JFinalConfig jfianlConfig= getJfinalConfig();
	@Override
	public void start() {
		jfianlConfig.configConstant(constants);
		jfianlConfig.configPlugin(plugins);
		startPulgins();
		jfianlConfig.configInterceptor(interceptors);
		jfianlConfig.afterJFinalStart();

	}

	@Override
	public void stop() {
		jfianlConfig.beforeJFinalStop();
		stopPulgins();
	}
	private  void startPulgins(){
		plugins.getPluginList().stream().forEach(plugin->{
			try {
				// process ActiveRecordPlugin devMode
				if (plugin instanceof com.jfinal.plugin.activerecord.ActiveRecordPlugin) {
					com.jfinal.plugin.activerecord.ActiveRecordPlugin arp = (com.jfinal.plugin.activerecord.ActiveRecordPlugin)plugin;
					if (arp.getDevMode() == null) {
						arp.setDevMode(constants.getDevMode());
					}
				}
				if (plugin.start() == false) {
					String message = "Plugin start error: " + plugin.getClass().getName();
					logger.error(message);
					throw new RuntimeException(message);
				}
			}
			catch (Exception e) {
				String message = "Plugin start error: " + plugin.getClass().getName() + ". \n" + e.getMessage();
				System.out.println(message);
				logger.error(message, e);
				throw new RuntimeException(message, e);
			}
		});
	}
	
	private void stopPulgins(){
		plugins.getPluginList().stream().forEach(plugin->{
			try {
				plugin.stop();
			} catch (Exception e) {
				String message="Plugin stop error: {}.\n{}";
				logger.error(message, plugin.getClass().getName(),e.getMessage());
			}
			
		});
	}
	private static JFinalConfig getJfinalConfig(){
		JFinalConfig config=null;;
		try {
			config = (JFinalConfig)Class.forName(PropKit.use("jfinal.properties").get("configClass")).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			logger.error("Failed to create JFinalConfig",e);
		}
		return config;
	}
}
