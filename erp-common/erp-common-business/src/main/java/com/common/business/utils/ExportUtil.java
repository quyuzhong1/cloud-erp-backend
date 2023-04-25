package com.common.business.utils;

import cn.hutool.core.util.StrUtil;
import com.common.business.service.RedisService;
import com.common.core.utils.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2023/4/25 18:05
 **/
public class ExportUtil {


    public static String getFileName(RedisService redisService, String fileName) {
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(fileName);
        sb.append(date);
        String redisKey = StrUtil.format( "file:name:{}-{}", fileName, date);
        Integer last = redisService.getCacheObject(redisKey);
        Integer lastNo = 1;
        if (last != null) {
            lastNo = last + 1;
        }
        redisService.setCacheObject(redisKey, lastNo, (long) 1, TimeUnit.DAYS);
        return sb.append(lastNo).toString();
    }
}
