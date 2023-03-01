package com.common.core.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * @ClassName: Holiday
 * @Author: lxh
 * @Description: 节假日
 * @Date: 2022/3/19 17:29
 */
public class HolidayUtils {
    /**
     * java获取国家法定节假日和周末
     * @param year /
     * @param month /
     * @return /
     */
    public static Set<LocalDate> JJR(int year, int month) {
        //获取所有的周末
        Set<LocalDate> monthWekDay = getMonthWekDay(year, month);
        //http://timor.tech/api/holiday api文档地址
        Map jjr = getJjr(year, month);
        Integer code = (Integer) jjr.get("code");
        if (code != 0) {
            return monthWekDay;
        }
        Map<String, Map<String, Object>> holiday = (Map<String, Map<String, Object>>) jjr.get("holiday");
        Set<String> strings = holiday.keySet();
        for (String str : strings) {
            Map<String, Object> stringObjectMap = holiday.get(str);
            Integer wage = (Integer) stringObjectMap.get("wage");
            LocalDate date = LocalDate.parse((String) stringObjectMap.get("date")) ;
            //筛选掉补班
            if (wage.equals(1)) {
                monthWekDay.remove(date);
            } else {
                monthWekDay.add(date);
            }
        }
        return monthWekDay;
    }

    /**
     * 获取节假日不含周末
     * @param year /
     * @param month /
     * @return /
     */
    private static LinkedHashMap getJjr(int year, int month) {
        String url = "http://timor.tech/api/holiday/year/";
        if(year > 0 && month > 0){
            url = StrUtil.format("{}{}-{}", url, year, month);
        }else if(year > 0){
            url = StrUtil.format("{}{}",url, year);
        }

        //解密数据
        String rsa = HttpUtil.get(url);
        return JSONObject.parseObject(rsa, LinkedHashMap.class);
    }

    /**
     * 获取周末  月从0开始
     * @param year /
     * @param month /
     * @return /
     */
    public static Set<LocalDate> getMonthWekDay(int year, int month) {
        Set<LocalDate> dateList= new LinkedHashSet<>();
        SimpleDateFormat simdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = new GregorianCalendar(year, month, 1);
        int i = 1;
        while (calendar.get(Calendar.YEAR) < year + 1) {
            calendar.set(Calendar.WEEK_OF_YEAR, i++);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            if (calendar.get(Calendar.YEAR) == year) {
                dateList.add(LocalDate.parse(simdf.format(calendar.getTime())));
            }
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            if (calendar.get(Calendar.YEAR) == year) {
                dateList.add(LocalDate.parse(simdf.format(calendar.getTime())));
            }
        }
        return dateList;
    }

}

