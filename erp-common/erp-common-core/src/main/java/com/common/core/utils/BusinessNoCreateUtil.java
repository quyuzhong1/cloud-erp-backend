package com.common.core.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Administrator
 * @Classname businessNoCreateUtil
 * @Description TODO
 * @Date 2023-01-09 17:56
 * @Created by yl
 */
public class BusinessNoCreateUtil {


    /**
     * 获取到业务的编号
     *
     * @param
     * @return
     */
    public static String getBusinessNo(String businessStr, Integer lastNo) {
        synchronized(BusinessNoCreateUtil.class){
            StringBuffer result = new StringBuffer(businessStr);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            result.append(LocalDateTime.now().format(formatter));
            String no = String.format("%0" + 4+"d", lastNo + 1);
            result.append(no);
            return result.toString();
        }

    }


}
