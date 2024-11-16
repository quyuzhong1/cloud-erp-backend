package com.common.business.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.RedisService;
import com.common.core.utils.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 * @Author Cloud
 * @Date 2023/4/25 18:05
 **/
@Slf4j
public class ExportUtil {

    private ExportUtil() {
    }

    public static String getFileName(RedisService redisService, String fileName) {
        StringBuilder sb = new StringBuilder();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(fileName);
        sb.append(date);
        String redisKey = CharSequenceUtil.format( "file:name:{}-{}", fileName, date);
        Integer last = redisService.getCacheObject(redisKey);
        Integer lastNo = 1;
        if (last != null) {
            lastNo = last + 1;
        }
        redisService.setCacheObject(redisKey, lastNo, (long) 1, TimeUnit.DAYS);
        return sb.append(lastNo).toString();
    }
}
