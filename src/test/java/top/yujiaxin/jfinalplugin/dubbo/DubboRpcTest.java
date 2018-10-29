package top.yujiaxin.jfinalplugin.dubbo;


import java.io.IOException;
import java.util.HashMap;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

import junit.framework.TestCase;
import top.yujiaxin.jfinalplugin.dubbo.core.DubboRpc;
import top.yujiaxin.jfinalplugin.dubbo.support.DemoService;

public class DubboRpcTest extends TestCase {

	private static Prop prop=PropKit.use("jfinal.properties");
	
	protected void setUp() throws Exception {
		DubboRpc.init(prop);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testReceiveService() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
		DubboRpc.scanRpcServices();
		DemoService service=DubboRpc.receiveService(DemoService.class);
		assertNotNull(service);
		assertEquals("tom", service.getName());
	}
	
	public void testScanRpcServices() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
		DubboRpc.scanRpcServices();
		ReferenceConfig<DemoService> referenceConfig = DubboRpc.buildReferenceConfig(DemoService.class,new HashMap<String,String>());
		DemoService service=referenceConfig.get();
		assertNotNull(service);
		assertEquals("tom", service.getName());
	}
}
