package top.yujiaxin.jfinalplugin.dubbo;


import java.io.IOException;
import java.util.HashMap;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

import junit.framework.TestCase;
import org.apache.dubbo.config.ReferenceConfig;
import top.yujiaxin.jfinalplugin.dubbo.core.DubboRpc;
import top.yujiaxin.jfinalplugin.dubbo.support.DemoService;

public class DubboRpcTest extends TestCase {

	private static Prop prop=PropKit.use("jfinal.properties");
	
	protected void setUp() {
		DubboRpc.init(prop);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testScanRpcServices() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
		DubboRpc.scanRpcServices();
		ReferenceConfig<DemoService> referenceConfig = DubboRpc.getDubboConfigsFactory().createReferenceConfig(DemoService.class,new HashMap<>());
		DemoService service=referenceConfig.get();
		assertNotNull(service);
		assertEquals("tom", service.getName());
	}
	
	public void testReceiveService() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
		DubboRpc.scanRpcServices();
		DemoService service=DubboRpc.receiveService(DemoService.class);
		assertNotNull(service);
		assertEquals("tom", service.getName());
	}
	
	
}
