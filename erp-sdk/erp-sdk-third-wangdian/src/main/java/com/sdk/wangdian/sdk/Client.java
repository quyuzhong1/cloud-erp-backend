package com.sdk.wangdian.sdk;

import java.io.IOException;
import java.lang.reflect.Type;

public interface Client
{
	/**
	 * 设置超时时间, 毫秒
	 * 
	 * @return
	 */
	void setTimeout(int ms);

	Object execute(String method, Object[] args, Pager pager, Type returnType) throws WdtErpException, IOException;
	
	String execute(String method, String body, Pager pager) throws WdtErpException, IOException;
}
