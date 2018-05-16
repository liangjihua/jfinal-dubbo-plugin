package top.yujiaxin.jfinalplugin.dubbo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RpcService {
	String version() default "";

	String group() default "";

	boolean needEnhancer() default true;

	int retries() default 1;

	ClusterModel cluster() default ClusterModel.FAILOVER;

	String token() default "";

	String stub() default "";

	String mock() default "";

	LoadBalance loadbalance() default LoadBalance.RANDOM;

	boolean dynamic() default true;

	String path() default "";

	int delay() default 0;

	int timeout() default 1000;

	int connections() default 100;

	boolean async() default false;

	boolean deprecated() default false;

	String accesslog() default "";

	String owner() default "";

	String document() default "";

	int weight() default 100;

	int executes() default 0;

	int actives() default 0;

	String proxy() default "";

	String filter() default "";

	String listener() default "";

	String layer() default "";

	boolean register() default true;
}
