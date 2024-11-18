package com.common.business.utils;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 时间时区转换工具类
 * Created by xcf on 2022/1/20.
 */
@Slf4j
public class TimeZoneUtil {

    private TimeZoneUtil() {
    }

    /**
     * 时区转换
     * @param date              时间
     * @param originalTimeZone  原时区
     * @param returnTimeZone    返回时区
     * @return
     */
    public static Date conversion(Date date, String originalTimeZone, String returnTimeZone){
        //西十二区 .............................................. 西一区 中时区 东一区 ..............................................东十二区
        //W12   W11   W10   W9   W8   W7   W6   W5   W4   W3   W2   W1   0   E1   E2   E3   E4   E5   E6   E7   E8   E9   E10   E11   E12
        if(originalTimeZone == null || "".equals(originalTimeZone.trim())
                || returnTimeZone == null || "".equals(returnTimeZone.trim()) ||
                originalTimeZone.equalsIgnoreCase(returnTimeZone)){
            return date;
        }
        originalTimeZone = originalTimeZone.toUpperCase();
        returnTimeZone = returnTimeZone.toUpperCase();

        int appendHours = 0; //追加小时数，可为负数
        Integer originalNum = Integer.parseInt(originalTimeZone.replace("W", "").replace("E", ""));
        Integer returnNum = Integer.parseInt(returnTimeZone.replace("W", "").replace("E", ""));

        if((originalTimeZone.contains("W") && returnTimeZone.contains("W"))
                || (originalTimeZone.contains("E") && returnTimeZone.contains("E"))){
            //同西区或同东区情况
            appendHours = returnNum - originalNum;
        }else{
            //不同时区情况
            appendHours = returnNum + originalNum;
            if(returnTimeZone.contains("W") || "0".equalsIgnoreCase(returnTimeZone)){
                appendHours = -appendHours;
            }
        }

        log.info("原："+originalTimeZone+", 现："+returnTimeZone);
        log.info("追加小时数："+appendHours);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, appendHours);

        return cal.getTime();
    }

    /**
     * 获取请求的时区
     * @return
     */
    public static String getRequestTimeZone(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getParameter("timeZone");
    }

}
