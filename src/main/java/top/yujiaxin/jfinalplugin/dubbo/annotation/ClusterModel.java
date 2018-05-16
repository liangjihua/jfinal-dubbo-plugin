package top.yujiaxin.jfinalplugin.dubbo.annotation;
/**
 * 集群容错模式枚举值
 * @author yujiaxin
 * @since 1.0.1
 * 2018年5月7日
 */
public enum ClusterModel {
	FAILOVER,FAILFAST,FAILSAFE,FAILBACK,FORKING,BROADCAST;
}
