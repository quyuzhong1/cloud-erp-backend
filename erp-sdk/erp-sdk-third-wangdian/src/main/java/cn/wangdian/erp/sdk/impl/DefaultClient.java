package cn.wangdian.erp.sdk.impl;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import com.google.gson.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

public class DefaultClient implements Client
{
	private static final String WDT_SERVICE_URL = "http://wdt.wangdian.cn/";
	// 协议版本号
	public static final String version = "1.0";
	private static final int WDT_UNIX_TIMESTAMP_DIFF = 1325347200;

	private String sid; // 卖家标识
	private String url; // 服务器URL
	private String key; // openapi key
	private String secret; // openapi secret
	private String salt; // openapi salt
	private int timeout = 30000; // http超时时间, 默认30秒
	private boolean isMultiTenantMode = false ;

	private static class BooleanJsonDeserializer implements JsonDeserializer<Boolean>
	{
		@Override
		public Boolean deserialize(JsonElement json, Type type, JsonDeserializationContext context)
				throws JsonParseException
		{
			if (json.isJsonPrimitive())
			{
				JsonPrimitive primitive = (JsonPrimitive) json;
				if (primitive.isBoolean())
					return primitive.getAsBoolean();

				if (primitive.isNumber())
				{
					Number num = primitive.getAsNumber();
					if (num instanceof Integer || num instanceof Long || num instanceof Short || num instanceof Byte)
						return num.intValue() != 0;

					return num.doubleValue() != 0;
				}
			}

			return !json.isJsonNull();
		}

		static final BooleanJsonDeserializer INSTANCE = new BooleanJsonDeserializer();
	}

	private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.registerTypeAdapter(boolean.class, BooleanJsonDeserializer.INSTANCE)
			.registerTypeAdapter(Boolean.class, BooleanJsonDeserializer.INSTANCE).create();

	private static JsonParser parser = new JsonParser();

	private DefaultClient(String sid, String url, String key, String secret, boolean isMultiTenantMode)
	{
		if (url.endsWith("/"))
			url += "openapi";
		else
			url += "/openapi";
		this.url = url;

		this.sid = sid;
		this.key = key;

		int pos = secret.indexOf(':');
		this.secret = secret.substring(0, pos);
		this.salt = secret.substring(pos + 1);
		this.isMultiTenantMode = isMultiTenantMode;
	}

	private String urlEncode(String data) throws UnsupportedEncodingException
	{
		return URLEncoder.encode(data, StandardCharsets.UTF_8.name());
	}

	private String ToQueryString(Map<String, String> args) throws UnsupportedEncodingException
	{
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (Map.Entry<String, String> item : args.entrySet())
		{
			if (!first)
				sb.append('&');
			else
				first = false;

			sb.append(urlEncode(item.getKey()));
			sb.append('=');
			sb.append(urlEncode(item.getValue()));
		}

		return sb.toString();
	}

	private static String md5(String info)
	{
		try
		{
			// 获取 MessageDigest 对象，参数为 MD5 字符串，表示这是一个 MD5 算法（其他还有 SHA1 算法等）：
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			// update(byte[])方法，输入原数据
			// 类似StringBuilder对象的append()方法，追加模式，属于一个累计更改的过程
			md5.update(info.getBytes(StandardCharsets.UTF_8));
			// digest()被调用后,MessageDigest对象就被重置，即不能连续再次调用该方法计算原数据的MD5值。可以手动调用reset()方法重置输入源。
			// digest()返回值16位长度的哈希值，由byte[]承接
			byte[] md5Array = md5.digest();
			// byte[]通常我们会转化为十六进制的32位长度的字符串来使用,本文会介绍三种常用的转换方法
			return bytesToHex(md5Array);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static String bytesToHex(byte[] md5Array)
	{
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < md5Array.length; i++)
		{
			int temp = 0xff & md5Array[i];
			String hexString = Integer.toHexString(temp);
			if (hexString.length() == 1)
			{// 如果是十六进制的0f，默认只显示f，此时要补上0
				strBuilder.append("0").append(hexString);
			}
			else
			{
				strBuilder.append(hexString);
			}
		}

		return strBuilder.toString();
	}

	/**
	 * 计算签名
	 * @param args 请求参数
	 * @param secret secret
	 * @return 签名
	 */
	private String sign(Map<String, String> args, String secret)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(secret);
		for (Map.Entry<String, String> item : args.entrySet())
		{
			if (item.getKey().equals("sign"))
				continue;

			sb.append(item.getKey());
			sb.append(item.getValue());
		}
		sb.append(secret);

		return md5(sb.toString());
	}

	/**
	 *
	 * @param method 方法名称
	 * @param args 请求参数
	 * @param pager 分页参数
	 * @param returnType 返回值类型
	 * @return 响应信息
	 * @throws WdtErpException wdt返回的错误信息
	 * @throws IOException 请求异常
	 */
	@Override
	public Object execute(String method, Object[] args, Pager pager, Type returnType)
			throws WdtErpException, IOException
	{
		Map<String, String> requestParams = new TreeMap<>();
		requestParams.put("sid", this.sid);
		requestParams.put("key", this.key);
		requestParams.put("method", method);
		requestParams.put("salt", this.salt);
		requestParams.put("timestamp",
				Integer.toString((int) (System.currentTimeMillis() / 1000 - WDT_UNIX_TIMESTAMP_DIFF)));
		requestParams.put("v", version);

		if (pager != null)
		{
			requestParams.put("page_size", Integer.toString(pager.getPageSize()));
			requestParams.put("page_no", Integer.toString(pager.getPageNo()));
			requestParams.put("calc_total", pager.isCalcTotal() ? "1" : "0");
		}

		// 请求参数
		String body = gson.toJson(args);
		requestParams.put("body", body);

		// 计算签名
		String sign = this.sign(requestParams, this.secret);
		requestParams.remove("body");
		requestParams.put("sign", sign);

		String requestUrl = this.url + "?" + this.ToQueryString(requestParams);

		PrintWriter outWriter = null;
		BufferedReader inReader = null;
		String responseBody;
		try
		{
			URL url = new URL(requestUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);

			if (this.isMultiTenantMode)
				conn.setRequestProperty("Connection", "close");

			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);

			OutputStream outStream = conn.getOutputStream();
			outWriter = new PrintWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8));

			outWriter.write(body);
			outWriter.flush();

			InputStream in = conn.getInputStream();
			inReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

			StringBuilder sb = new StringBuilder();
			int len;
			char[] tmp = new char[256];
			while ((len = inReader.read(tmp)) > 0)
			{
				sb.append(tmp, 0, len);
			}
			responseBody = sb.toString();
		}
		finally
		{
			if (outWriter != null)
				outWriter.close();

			if (inReader != null)
				inReader.close();
		}

		JsonObject jsonResponse = parser.parse(responseBody).getAsJsonObject();

		int status = jsonResponse.get("status").getAsInt();
		if (status > 0)
		{
			String message = jsonResponse.get("message").getAsString();
			throw new WdtErpException(status, message);
		}

		if (returnType.equals(void.class) || returnType.equals(Void.class))
			return null;

		Object result;
		if (!jsonResponse.has("data"))
			result = null;
		else if (jsonResponse.get("data").isJsonArray())
			result = gson.fromJson(jsonResponse, returnType);
		else
			result = gson.fromJson(jsonResponse.get("data"), returnType);

		if (pager == null || !pager.isCalcTotal() || result == null || !(returnType instanceof Class))
			return result;

		if (!jsonResponse.has("total"))
			return result;

		Class<?> returnClass = (Class<?>) returnType;
		try
		{
			Method[] methods = returnClass.getDeclaredMethods();
			for (Method m : methods)
			{
				if (m.getName().equals("setTotal"))
				{
					Class<?>[] argsType = m.getParameterTypes();
					if (argsType.length == 1)
					{
						Class<?> argType = argsType[0];
						if (argType == Integer.class || argType == int.class)
							m.invoke(result, jsonResponse.get("total").getAsInt());
						else if (argType == Long.class || argType == long.class)
							m.invoke(result, jsonResponse.get("total").getAsLong());
						else
							continue;

						break;
					}
				}
			}
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			e.printStackTrace();
		}

		return result;

	}

	@Override
	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	public static Client get(String sid, String url, String key, String secret, boolean isMultiTenantMode)
	{
		return new DefaultClient(sid, url, key, secret, isMultiTenantMode);
	}

	public static Client get(String sid, String url, String key, String secret)
	{
		return new DefaultClient(sid, url, key, secret, false);
	}

	public static Client get(String sid, String key, String secret)
	{
		return new DefaultClient(sid, WDT_SERVICE_URL, key, secret, false);
	}
}
