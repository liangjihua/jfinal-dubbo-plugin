package top.yujiaxin.jfinalplugin.dubbo.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
public @interface ReferenceService {
	
	Class<?> interfaceClass() default void.class;
	
	String interfaceName() default "";

	String version() default "";

	String group() default "";

	int retries() default 1;

	ClusterModel cluster() default ClusterModel.FAILOVER;

	String stub() default "";

	String mock() default "";

	LoadBalance loadbalance() default LoadBalance.RANDOM;
	
	int timeout() default 1000;
	
	int connections() default 100;
	
	boolean async() default false;
	
	boolean generic() default false;
	
	boolean check() default false;
	
	String url() default "";
	
	String cache() default "";
	
	boolean validation() default false;
	
	String proxy() default "";
	
	String client() default "";
	
	String owner() default "";
	
	int actives() default 0;
	
	String filter() default "";
	
	String listener() default "";
	
	String layer() default "";
	
	boolean init() default false;
	
	String protocol() default "";
}
