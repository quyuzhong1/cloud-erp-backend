package com.common.business.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.LuaScript;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.LocalDateUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * redis单号生成器
 * @CreateTime: 2023-07-10  10:18
 * @Author: zhangchunlin
 */
@Slf4j
@Component
public class DocNoGenHelper implements InitializingBean {

    private static DefaultRedisScript<Long> redisScript;

    private static RedisSerializer stringRedisSerializer = new StringRedisSerializer();

    /**
     * 单位秒
     */
    public static long ONE_DAY_CACHE_TIME = 24 * 60 * 60L;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptText(LuaScript.GEN_DOC_NO_SCRIPT);
    }

    /**
     * 按单据类型+日期获取递增单号
     * 单据前缀+6位日期+5位顺序
     * 注意事项：需在枚举类BusinessNoTypeEnum定义单号前缀，上线切换时需手工把最新的最大值放入到数据库
     * @param businessNoTypeEnum
     * @return
     */
    public String generateCode(BusinessNoTypeEnum businessNoTypeEnum){
        String currentDateStr = LocalDateUtil.formatTime(LocalDateTime.now(), "yyMMdd");
        //注意，不保证绝对有序，有可能中间某个单生成了单号，但是后面数据库报错不会回收
        String docNoKey = BusinessNoTypeEnum.REDIS_GEN_KEY + ":" + businessNoTypeEnum.getName() +  ":" + currentDateStr;
        Long currentIndex = redisTemplate.execute(redisScript, stringRedisSerializer, stringRedisSerializer, Lists.newArrayList(docNoKey),String.valueOf(1),String.valueOf(ONE_DAY_CACHE_TIME));
        int fillZeroDigit = BusinessNoTypeEnum.FILL_0_DIGIT;
        if(BusinessCommonConstants.hasProfile("test") || BusinessCommonConstants.hasProfile("dev")){
            fillZeroDigit = fillZeroDigit +1;
        }
        // 单据前缀+6位日期+5位顺序位
        String docNo = CharSequenceUtil.format("{}{}{}",StrUtils.null2EmptyWithTrim(businessNoTypeEnum.getPrefix()), currentDateStr, StrUtils.leftPadding(String.valueOf(currentIndex),fillZeroDigit,"0"));
        log.info("单据类型：【{}】生成的单号为【{}】", businessNoTypeEnum.getName(), docNo);
        return docNo;
    }


    /**
     * 按单据类型+日期获取递增单号
     * 单据前缀+6位日期+5位顺序
     * 注意事项：需在枚举类BusinessNoTypeEnum定义单号前缀，上线切换时需手工把最新的最大值放入到数据库
     * @return
     */
    public String generateCode(BusinessNoTypeEnum businessNoTypeEnum, LocalDate date){
        String currentDateStr = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        //注意，不保证绝对有序，有可能中间某个单生成了单号，但是后面数据库报错不会回收
        String docNoKey = BusinessNoTypeEnum.REDIS_GEN_KEY + ":" + businessNoTypeEnum.getName() +  ":" + currentDateStr;
        Long currentIndex = redisTemplate.execute(redisScript, stringRedisSerializer, stringRedisSerializer, Lists.newArrayList(docNoKey),String.valueOf(1),String.valueOf(ONE_DAY_CACHE_TIME));
        int fillZeroDigit = BusinessNoTypeEnum.FILL_0_DIGIT;
        if(BusinessCommonConstants.hasProfile("test") || BusinessCommonConstants.hasProfile("dev")){
            fillZeroDigit = fillZeroDigit +1;
        }
        // 单据前缀+6位日期+5位顺序位
        String docNo = CharSequenceUtil.format("{}{}{}",StrUtils.null2EmptyWithTrim(businessNoTypeEnum.getPrefix()), currentDateStr, StrUtils.leftPadding(String.valueOf(currentIndex),fillZeroDigit,"0"));
        log.info("单据类型：【{}】生成的单号为【{}】", businessNoTypeEnum.getName(), docNo);
        return docNo;
    }

    /**
     * 按code获取递增单号
     * code+2顺序
     * 注意事项：需在枚举类BusinessNoTypeEnum定义单号前缀，上线切换时需手工把最新的最大值放入到数据库
     */
    public String generateMouldCode(String code){
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        // 拼接结果
        String currentDateStr = currentDate.getYear() % 100 + Integer.toHexString(currentDate.getMonthValue()).toUpperCase();
        //注意，不保证绝对有序，有可能中间某个单生成了单号，但是后面数据库报错不会回收
        String docNoKey = BusinessNoTypeEnum.REDIS_GEN_KEY + ":" + BusinessNoTypeEnum.CODE_MOULD + ":" + code + ":"+ currentDateStr;
        Long currentIndex = redisTemplate.execute(redisScript, stringRedisSerializer, stringRedisSerializer, Lists.newArrayList(docNoKey),String.valueOf(1),String.valueOf(ONE_DAY_CACHE_TIME));
        int fillZeroDigit = 2;
        if(BusinessCommonConstants.hasProfile("test") || BusinessCommonConstants.hasProfile("dev")){
            fillZeroDigit = fillZeroDigit + 1;
        }
        // 单据前缀+6位日期+5位顺序位
        return CharSequenceUtil.format("{}{}{}{}",StrUtils.null2EmptyWithTrim(BusinessNoTypeEnum.CODE_MOULD.getPrefix()), code, currentDateStr, StrUtils.leftPadding(String.valueOf(currentIndex),fillZeroDigit,"0"));
    }


    /**
     * 按code获取递增单号
     * code+2顺序
     */
    public String generateMouldDetailCode(String code) {

        //注意，不保证绝对有序，有可能中间某个单生成了单号，但是后面数据库报错不会回收
        String docNoKey = BusinessNoTypeEnum.REDIS_GEN_KEY + ":" + code;
        Long currentIndex = redisTemplate.execute(redisScript, stringRedisSerializer, stringRedisSerializer, Lists.newArrayList(docNoKey), String.valueOf(1), String.valueOf(ONE_DAY_CACHE_TIME));
        int fillZeroDigit = 2;
        if (BusinessCommonConstants.hasProfile("test") || BusinessCommonConstants.hasProfile("dev")) {
            fillZeroDigit = fillZeroDigit + 1;
        }
        // 单据前缀+6位日期+5位顺序位
        return CharSequenceUtil.format("{}{}{}", code, StrUtils.leftPadding(String.valueOf(currentIndex), fillZeroDigit, "0"));
    }

}