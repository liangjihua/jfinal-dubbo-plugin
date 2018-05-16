package top.yujiaxin.jfinalplugin.dubbo.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
/**
 * 提供一组默认值的RpcService
 * @author yujiaxin
 * @since 1.0.1
 * 2018年5月7日
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE })
public @interface SimpleRpcService {
	String version() default "1.0";
	String group() default "simple";
	boolean needEnhancer() default true;
	int retries() default 1;
	ClusterModel cluster() default ClusterModel.FAILOVER;
	String token() default "true";
	String stub() default "true";
	String mock() default "true";
}
