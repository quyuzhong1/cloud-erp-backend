package com.common.core.utils;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年10月27日 下午9:59:27
 */
public class R extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	private static final String KEY_CODE = "code";
	private static final String KEY_MSG = "msg";


	public R() {
		put(KEY_CODE, 0);
		put(KEY_MSG, "success");
	}

	public boolean isOk() {
		return ("0").equals(super.get(KEY_CODE).toString());
	}

	public static R error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}

	public static R error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}

	public static R error(int code, String msg) {
		R r = new R();
		r.setCode(code);
		r.setMsg(msg);
		return r;
	}

	public static R ok(String msg) {
		R r = new R();
		r.put(KEY_MSG, msg);
		return r;
	}

	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}

	public static R ok() {
		return new R();
	}

	public static R okResponse(Object response) {
		final String RESPONSE="response";
		return new R().put(RESPONSE, response);
	}

	public R setCode(int code) {
		super.put(KEY_CODE, code);
		return this;
	}

	public R setMsg(String msg) {
		super.put(KEY_MSG, msg);
		return this;
	}

	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public static void runError(Throwable e) {
		throw new RuntimeException(e);
	}

	public static void runError(String message) {
		throw new RuntimeException(message);
	}

	public static void runError(String message, Throwable e) {
		throw new RuntimeException(message, e);
	}
}
