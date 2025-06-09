package com.common.core.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
     * java获取国家法定节假日和周末,调休
     * @param year /
     * @param month /
     * @return /
     *
     */
    public static List<JSONObject> JJRRemarkMap(int year, int month) {
        //获取所有的周末
        Set<LocalDate> dateList = getMonthWekDay(year, month);
        Map jjr = getJjr(year, month);
        Integer code = (Integer) jjr.get("code");
        List<JSONObject> resultJson = new ArrayList<>();
        resultJson = dateList.stream().map(x -> {
            JSONObject data = new JSONObject();
            data.put("calendarDate", x);
            data.put("isWorkDay", Boolean.FALSE);
            data.put("remark", "周末");
            return data;
        }).collect(Collectors.toList());
        if (code != 0) {
            return resultJson;
        }
        Map<String, Map<String, Object>> holiday = (Map<String, Map<String, Object>>) jjr.get("holiday");
        Set<String> strings = holiday.keySet();
        for (String str : strings) {
            Map<String, Object> stringObjectMap = holiday.get(str);
            LocalDate date = LocalDate.parse((String) stringObjectMap.get("date")) ;
            String remark = (String) stringObjectMap.get("name");
            Boolean isWork = !(Boolean)stringObjectMap.get("holiday") ;

            //筛选掉 补 班
            JSONObject data = new JSONObject();
            data.put("calendarDate", date);
            data.put("remark", remark);
            data.put("isWorkDay", isWork);
            resultJson.add(data);
        }
        return resultJson;
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
            url = CharSequenceUtil.format("{}{}-{}", url, year, month);
        }else if(year > 0){
            url = CharSequenceUtil.format("{}{}",url, year);
        }

        //解密数据
        String rsa = HttpUtil.get(url);
        return JSONUtil.toBean(rsa, LinkedHashMap.class);
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

    public static void main(String[] args) {
        List<JSONObject> jsonObjects = JJRRemarkMap(2023, 0);
        Set<LocalDate> jjr = JJR(2023, 0);
    }
}

