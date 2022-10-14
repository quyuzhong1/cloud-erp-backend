package com.common.core.utils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tools {
	private static Logger logger = LoggerFactory.getLogger(Tools.class);


	/**
	 * 检测字符串是否不为空(null,"","null")
	 *
	 * @param s
	 * @return 不为空则返回true，否则返回false
	 */
	public static boolean notEmpty(String s) {
		return s != null && !"".equals(s.trim());
	}

	/**
	 * 检测字符串是否为空(null,"","null")
	 *
	 * @param s
	 * @return 为空则返回true，不否则返回false
	 */
	public static boolean isEmpty(String s) {
		return s == null || "".equals(s.trim());
	}

	/**
	 * 字符串转换为字符串数组
	 *
	 * @param str        字符串
	 * @param splitRegex 分隔符
	 * @return
	 */
	public static String[] str2StrArray(String str, String splitRegex) {
		if (isEmpty(str)) {
			return null;
		}
		return str.split(splitRegex);
	}

	/**
	 * 用默认的分隔符(,)将字符串转换为字符串数组
	 *
	 * @param str 字符串
	 * @return
	 */
	public static String[] str2StrArray(String str) {
		return str2StrArray(str, ",\\s*");
	}

	/**
	 * 按照yyyy-MM-dd HH:mm:ss的格式，字符串转日期
	 *
	 * @param date
	 * @return
	 */
	public static Date str2Date(String date) {
		if (notEmpty(date)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				return sdf.parse(date);
			} catch (ParseException ignored) {
			}
			return new Date();
		} else {
			return null;
		}
	}

	/**
	 * 按照参数format的格式，日期转字符串
	 *
	 * @param date
	 * @param format
	 * @return
	 */
	public static String date2Str(Date date, String format) {
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(date);
		} else {
			return "";
		}
	}

	/**
	 * 把时间根据时、分、秒转换为时间段
	 *
	 * @param StrDate
	 */
	public static String getTimes(String StrDate) {
		String resultTimes = "";

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now;

		try {
			now = new Date();
			Date date = df.parse(StrDate);
			long times = now.getTime() - date.getTime();
			long day = times / (24 * 60 * 60 * 1000);
			long hour = (times / (60 * 60 * 1000) - day * 24);
			long min = ((times / (60 * 1000)) - day * 24 * 60 - hour * 60);
			long sec = (times / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

			StringBuilder sb = new StringBuilder();
			//sb.append("发表于：");
			if (hour > 0) {
				sb.append(hour).append("小时前");
			} else if (min > 0) {
				sb.append(min).append("分钟前");
			} else {
				sb.append(sec).append("秒前");
			}

			resultTimes = sb.toString();
		} catch (ParseException ignored) {
		}

		return resultTimes;
	}

	/**
	 * 写txt里的单行内容
	 *
	 * @param path 文件路径
	 * @param content  写入的内容
	 */
	public static void writeFile(String path, String content) {
		String filePath = String.valueOf(Thread.currentThread().getContextClassLoader().getResource("")) + "../../";    //项目路径
		filePath = (filePath.trim() + path.trim()).substring(6).trim();
		if (filePath.indexOf(":") != 1) {
			filePath = File.separator + filePath;
		}
		try {
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8);
			BufferedWriter writer = new BufferedWriter(write);
			writer.write(content);
			writer.close();


		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 验证邮箱
	 *
	 * @param email
	 * @return
	 */
	public static boolean checkEmail(String email) {
		boolean flag = false;
		try {
			String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		} catch (Exception ignored) {
		}
		return flag;
	}

	/**
	 * 验证手机号码
	 *
	 * @param mobileNumber
	 * @return
	 */
	public static boolean checkMobileNumber(String mobileNumber) {
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
			Matcher matcher = regex.matcher(mobileNumber);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 读取txt里的单行内容
	 *
	 * @param path 文件路径
	 */
	public static String readTxtFile(String path) {
		try {

			String filePath = String.valueOf(Thread.currentThread().getContextClassLoader().getResource("")) + "../../";    //项目路径
			filePath = filePath.replaceAll("file:/", "");
			filePath = filePath.replaceAll("%20", " ");
			filePath = filePath.trim() + path.trim();
			if (filePath.indexOf(":") != 1) {
				filePath = File.separator + filePath;
			}
			String encoding = "utf-8";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) {        // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);    // 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					return lineTxt;
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件,查看此路径是否正确:" + filePath);
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
		}
		return "";
	}

	public static String[] getNullPropertyNames(Object source) {
		final BeanWrapper src = new BeanWrapperImpl(source);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

		Set<String> emptyNames = new HashSet<String>();
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (srcValue == null) emptyNames.add(pd.getName());
		}
		String[] result = new String[emptyNames.size()];
		return emptyNames.toArray(result);
	}

	/**
	 * 将一个对象的相同属性复制到另一对象中（忽略null）
	 *
	 * @param source           源对象
	 * @param target           目标对象
	 * @param ignoreProperties 忽略的属性
	 */
	public static void copyPropertiesIgnoreNull(Object source, Object target, String... ignoreProperties) {
		String[] nullPropertyNames = getNullPropertyNames(source);
		String[] ignore = ArrayUtils.addAll(nullPropertyNames, ignoreProperties);

		org.springframework.beans.BeanUtils.copyProperties(source, target, ignore);
	}

	/**
	 * 将一个对象的相同属性复制到另一对象中
	 *
	 * @param source           源对象
	 * @param target           目标对象
	 * @param ignoreProperties 忽略的属性
	 */
	public static void copyProperties(Object source, Object target, String... ignoreProperties) {
		org.springframework.beans.BeanUtils.copyProperties(source, target, ignoreProperties);
	}

	public static void cloneBean(Object source, Object target) {
		try {
			target = BeanUtils.cloneBean(source);
		} catch (Exception e) {
			throw new RuntimeException("Clone Bean Failed", e);
		}
	}

	/**
	 * 获取错误跟踪信息(当长度有限制时，优先显示底层错误)
	 *
	 * @param e           错误信息
	 * @param limitLength 限定长度
	 * @return
	 */
	public static String getErrorTrace(Throwable e, int limitLength) {
		String errMsg = getErrorTrace(e);
		if (limitLength > 0 && errMsg.length() > limitLength) {
			return errMsg.substring(errMsg.toCharArray().length - limitLength);
		} else {
			return errMsg;
		}
	}

	/**
	 * 获取最后导致错误的跟踪信息
	 *
	 * @param e           错误信息
	 * @param limitLength 限定长度
	 * @return
	 */
	public static String getErrorLastTrace(Throwable e, int limitLength) {
		String errMsg = (e.getMessage() == null ? e.toString() : e.getMessage());
		try {
			errMsg = errMsg + ":\r\n " + getErrorTrace(getErrorLastCause(e));
			if (limitLength > 0 && errMsg.length() > limitLength) {
				return errMsg.substring(0, limitLength);
			}
		} catch (Exception e2) {
		}
		return errMsg;
	}

	/**
	 * 获取错误跟踪信息(全部)
	 *
	 * @param e 错误信息
	 * @return
	 */
	public static String getErrorTrace(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		e.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}

	/**
	 * 获取最后一个导致错误的跟踪信息
	 *
	 * @param e 错误信息
	 * @return
	 */
	public static String getErrorLastTrace(Throwable e) {
		String errMsg = (e.getMessage() == null ? e.toString() : e.getMessage());
		try {
			errMsg = errMsg + ":\r\n" + getErrorTrace(getErrorLastCause(e));
		} catch (Exception e2) {
		}
		return errMsg;
	}

	/**
	 * 获取最后一个导致错误的跟踪信息
	 *
	 * @param e 错误信息
	 * @return
	 */
	public static Throwable getErrorLastCause(Throwable e) {
		Throwable e1 = e.getCause();
		if (e1 != null) {
			//e1.printStackTrace();
			return getErrorLastCause(e1);
		} else {
			//e.printStackTrace();
			return e;
		}
	}

	public static String getMD5String(String str) {
		try {
			// 生成一个MD5加密计算摘要
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 计算md5函数
			md.update(str.getBytes("GBK"));
			// digest()最后确定返回md5 hash值，返回值为8位字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
			// BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
			//一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方）
			return new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			throw new RuntimeException("MD5加密字符串数据异常。", e);
		}
	}

	/**
	 * 判断Object对象为空或空字符串
	 * @param obj
	 * @return
	 *
	 */
	public static Boolean isObjectNotEmpty(Object obj) {
		String str = ObjectUtils.toString(obj, "");

		return StringUtils.isNotBlank(str);
	}

	public static void main(String[] args) {
	}

}
