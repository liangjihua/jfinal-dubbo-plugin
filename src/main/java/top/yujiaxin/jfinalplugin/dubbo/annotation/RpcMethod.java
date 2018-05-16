package top.yujiaxin.jfinalplugin.dubbo.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
/**
 * 用来对rpc方法独立配置，可以与{@link RpcService}联合使用，覆盖RpcService对该方法的相关配置
 * @author yujiaxin
 * @since 1.0.1
 * 2018年5月7日
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface RpcMethod {
	
}
