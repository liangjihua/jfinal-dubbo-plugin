package top.yujiaxin.jfinalplugin.dubbo.exception;

public class RpcServiceReferenceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7541631454711117444L;

	public RpcServiceReferenceException(){
		super();
	}
	public RpcServiceReferenceException(String message){
		super(message);
	}
	public RpcServiceReferenceException(Throwable throwable){
		super(throwable);
	}
	public RpcServiceReferenceException(String message,Throwable throwable){
		super(message,throwable);
	}
	public RpcServiceReferenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace){
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
