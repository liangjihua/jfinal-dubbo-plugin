package top.yujiaxin.jfinalplugin.dubbo.exception;

public class DubboConfigException extends RuntimeException  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 650566764038616023L;

	public DubboConfigException(){
		super();
	}
	
	public DubboConfigException(String message){
		super(message);
	}
	
	public DubboConfigException(Throwable cause){
		super(cause);
	}
	
	public DubboConfigException(String message,Throwable cause){
		super(message, cause);
	}
	
	public DubboConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace){
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
