package com.common.core.utils;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.google.common.collect.Maps;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ValidatorUtil {

	private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	/**
	 * 正则表达式：验证用户名
	 */
	public static final String REGEX_USERNAME = "^[a-zA-Z]\\w{5,20}$";

	/**
	 * 正则表达式：验证密码
	 */
	public static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,20}$";

	/**
	 * 正则表达式：验证手机号
	 */
	public static final String REGEX_MOBILE = "^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";

	/**
	 * 正则表达式：验证邮箱
	 */
	public static final String REGEX_EMAIL = "^[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?$";

	/**
	 * 正则表达式：验证汉字
	 */
	public static final String REGEX_CHINESE = "^[\u4e00-\u9fa5],{0,}$";

	/**
	 * 正则表达式：验证身份证
	 */
	public static final String REGEX_ID_CARD = "(^\\d{18}$)|(^\\d{15}$)";

	/**
	 * 正则表达式：验证URL
	 */
	public static final String REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";

	/**
	 * 正则表达式：验证IP地址
	 */
	public static final String REGEX_IP_ADDR = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";

	/**
	 * 校验用户名
	 *
	 * @param username
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isUsername(String username) {
		return Pattern.matches(REGEX_USERNAME, username);
	}

	/**
	 * 校验密码
	 *
	 * @param password
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isPassword(String password) {
		return Pattern.matches(REGEX_PASSWORD, password);
	}

	/**
	 * 校验手机号
	 *
	 * @param mobile
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isMobile(String mobile) {
		return Pattern.matches(REGEX_MOBILE, mobile);
	}

	/**
	 * 校验邮箱
	 *
	 * @param email
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isEmail(String email) {
		return Pattern.matches(REGEX_EMAIL, email);
	}

	/**
	 * 校验汉字
	 *
	 * @param chinese
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isChinese(String chinese) {
		return Pattern.matches(REGEX_CHINESE, chinese);
	}

	/**
	 * 校验身份证
	 *
	 * @param idCard
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isIDCard(String idCard) {
		return Pattern.matches(REGEX_ID_CARD, idCard);
	}

	/**
	 * 校验URL
	 *
	 * @param url
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isUrl(String url) {
		return Pattern.matches(REGEX_URL, url);
	}

	/**
	 * 校验IP地址
	 *
	 * @param ipAddr
	 * @return
	 */
	public static boolean isIPAddr(String ipAddr) {
		return Pattern.matches(REGEX_IP_ADDR, ipAddr);
	}

	/**
	 * 功能：检测当前URL是否可连接或是否有效,
	 * 描述：最多连接网络 3 次, 如果 3 次都不成功，视为该地址不可用
	 *
	 * @param urlStr 指定URL网络地址
	 * @return URL
	 */
	public static boolean isConnect(String urlStr) {
		int counts = 0;
		if (urlStr == null || urlStr.length() <= 0) {
			return false;
		}
		while (counts < 3) {
			try {
				URL url = new URL(urlStr);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				int responseCode = con.getResponseCode();
				if (responseCode == 200) {
					return true;
				}
				break;
			} catch (Exception ex) {
				counts++;
			}
		}
		return false;
	}

	/**
	 *
	 * @return
	 */
	public static ObjectError getPermanentError(List<ObjectError> allErrors) {
		//此处按字段排序，以免每次报出来的错误不一致
		ObjectError objectError = allErrors.get(0);
		long fieldErrorCnt = allErrors.stream().filter(r->r instanceof FieldError).count();
		if(allErrors.size() == fieldErrorCnt) {
			Map<String,ObjectError> fieldErrorMap = Maps.newLinkedHashMap();
			allErrors.stream().forEach(objError -> {
				if(objError instanceof FieldError) {
					fieldErrorMap.put(((FieldError) objError).getField(),objError);
				}
			});
			Collection<String> fieldKeySet = fieldErrorMap.keySet();
			List<String> fieldKeys = new ArrayList<>(fieldKeySet);
			Collections.sort(fieldKeys);

			objectError = fieldErrorMap.get(fieldKeys.get(0));
		}
		return objectError;
	}


	/**
	 * 是否正确（不正确报错）
	 * @param expression
	 * @param exceptionSupplier
	 * @param <X>
	 */
	public static<X extends Throwable> void isTrue(boolean expression, Supplier<? extends X> exceptionSupplier) throws X {
		if(!expression) {
			throw exceptionSupplier.get();
		}
	}

	/**
	 * 如果条件成立则执行方法（如果是需要检测然后抛异常请勿调用该方法，请调用isTrue方法）
	 * @param expression
	 * @param function
	 */
	public static void isTrueCall(boolean expression, VoidFunc function) {
		if(expression) {
			function.callWithRuntimeException();
		}
	}

	/**
	 * 校验对象
	 *
	 * @param object 待校验对象
	 * @param groups 待校验的组
	 * @throws ServiceException 校验不通过，则报ServiceException异常
	 */
	public static void validateEntity(Object object, Class<?>... groups)
			throws ServiceException {
		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
		if (!constraintViolations.isEmpty()) {
			List<ConstraintViolation<Object>> sortedConstraintViolations = new ArrayList<>(constraintViolations);
			sortedConstraintViolations = sortedConstraintViolations.stream().sorted(Comparator.comparing(ConstraintViolation::getMessage)).collect(Collectors.toList());
			ConstraintViolation<Object> constraint = sortedConstraintViolations.iterator().next();
			throw new ServiceException(ApiError.ERROR_99999.code, constraint.getMessage());
		}
	}

}