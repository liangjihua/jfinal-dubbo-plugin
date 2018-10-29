package top.yujiaxin.jfinalplugin.dubbo.support;

import top.yujiaxin.jfinalplugin.dubbo.annotation.RpcService;

@RpcService
public class DemoServiceImpl implements DemoService {

	@Override
	public String getName() {
		return "tom";
	}

}
