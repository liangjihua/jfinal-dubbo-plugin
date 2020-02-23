package top.yujiaxin.jfinalplugin.dubbo.container;

import org.apache.dubbo.common.utils.ConfigUtils;
import org.apache.dubbo.container.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.config.Constants;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;

public class JfinalContainer implements Container {
    private static final String JFINAL_CONFIG_CLASS = "dubbo.jfinal.config-class";

	private static final Logger logger = LoggerFactory.getLogger(JfinalContainer.class);
	private Constants constants = new Constants();
	private Plugins plugins = new Plugins();
	private Interceptors interceptors = new Interceptors();
	private JFinalConfig jfianlConfig= getJfinalConfig();

    public JfinalContainer() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
    }

    @Override
	public void start() {
		jfianlConfig.configConstant(constants);
		jfianlConfig.configPlugin(plugins);
		startPulgins();
		jfianlConfig.configInterceptor(interceptors);
		jfianlConfig.onStart();
	}

	@SuppressWarnings("deprecation")
    @Override
	public void stop() {
        try{
            jfianlConfig.onStop();
            jfianlConfig.beforeJFinalStop();
            stopPulgins();
        } catch (Throwable e){
            logger.error(e.getMessage(), e);
        }

	}
	private void startPulgins(){
		plugins.getPluginList().forEach(plugin->{
			try {
				// process ActiveRecordPlugin devMode
				if (plugin instanceof com.jfinal.plugin.activerecord.ActiveRecordPlugin) {
					com.jfinal.plugin.activerecord.ActiveRecordPlugin arp = (com.jfinal.plugin.activerecord.ActiveRecordPlugin)plugin;
					if (arp.getDevMode() == null) {
						arp.setDevMode(constants.getDevMode());
					}
				}
				if (!plugin.start()) {
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
		plugins.getPluginList().forEach(plugin->{
			try {
				plugin.stop();
			} catch (Exception e) {
				String message="Plugin stop error: {}.\n{}";
				logger.error(message, plugin.getClass().getName(),e.getMessage());
			}
		});
	}

	private static JFinalConfig getJfinalConfig() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		try {
			return (JFinalConfig)Class.forName(ConfigUtils.getProperty(JFINAL_CONFIG_CLASS)).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			logger.error("Failed to create JFinalConfig",e);
			throw e;
		}
	}
}
