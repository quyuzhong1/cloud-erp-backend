package com.erp.server.oms.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.server.oms.service.SoB2cAbnormalService;
import com.erp.server.oms.service.SoB2cErrorService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * B2C订单标记发货重试
 */
@Component
@Slf4j
public class SoB2cRetryJob {

    @Resource
    private SoB2cErrorService soB2cErrorService;
    @Resource
    private SoB2cAbnormalService soB2cAbnormalService;
    @Resource
    private RedisTemplate redisTemplate;


    /**
     * B2C订单异常重试任务
     *
     * @Author Jim
     **/
    @XxlJob("SoB2cRetryJob")
    public ReturnT<String> SoB2cRetryJob() {
        String redisKey = RedisKeyConstant.SOB2C_RETRY_JOB;
        if (redisTemplate.opsForValue().setIfAbsent(RedisKeyConstant.SOB2C_RETRY_JOB, DateUtil.now(), 600, TimeUnit.SECONDS)) {
            XxlJobHelper.log("SoB2cRetryJob 执行中,当前跳过");
            return ReturnT.FAIL;
        }
        XxlJobHelper.log("SoB2cRetryJob 执行开始");
        try {
            String jobParam = XxlJobHelper.getJobParam();
            int count = 1;
            String message = "";
            String type = "";
            Integer maxVersion = 3;
            List<String> messageList = new ArrayList<>();
            if (StringUtils.isNotBlank(jobParam)) {
                JSONObject jsonObject = new JSONObject(jobParam);
                count = jsonObject.getInt("count", 1);
                messageList = jsonObject.getJSONArray("messageList").stream().map(Object::toString).collect(Collectors.toList());
                type = jsonObject.getStr("type", "");
                maxVersion = jsonObject.getInt("maxVersion", 3);
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime todayNoon;
            if (now.getHour() < 12) {
                todayNoon = LocalDate.now().atTime(12, 0); // 今天12点的时间
            } else {
                todayNoon = LocalDate.now().atStartOfDay().minusDays(1); // 取前一天的0点
            }

            LambdaQueryWrapper<SoB2cErrorEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SoB2cErrorEntity::getType, type) // 添加 type 条件
                    .le(SoB2cErrorEntity::getUpdateTime, todayNoon) // 添加更新时间条件
                    .lt(SoB2cErrorEntity::getVersion, maxVersion); // 添加 version 条件

            if (CollectionUtils.isNotEmpty(messageList)) {
                for (String keyword : messageList) {
                    queryWrapper.or().like(SoB2cErrorEntity::getMessage, keyword); // 添加模糊查询条件
                }
            }

            // 按 updateTime 正序排列，并限制返回数量
            queryWrapper.orderByAsc(SoB2cErrorEntity::getUpdateTime);
            List<SoB2cErrorEntity> list = soB2cErrorService.list(queryWrapper.last(" LIMIT " + count));
            if (CollectionUtil.isEmpty(list)) {
                XxlJobHelper.log("SoB2cRetryJob 需要执行任务列表为空");
                return ReturnT.SUCCESS;
            }

            for (SoB2cErrorEntity soB2cErrorEntity : list) {
                try {
                    List<BatchResultDTO> resultDTOS = soB2cAbnormalService.batchRetry(soB2cErrorEntity.getMainId());
                    XxlJobHelper.log("SoB2cRetryJob 当前任务执行成功：{}", JSONUtil.toJsonStr(resultDTOS));
                } catch (Exception e) {
                    log.error("SoB2cRetryJob 当前任务执行成功异常：soId={}, error={}",
                            soB2cErrorEntity.getMainId(),
                            ExceptionUtil.stacktraceToString(e)
                    );
                    XxlJobHelper.log("SoB2cRetryJob 当前任务执行成功异常：handleType={}, error={}",
                            soB2cErrorEntity.getMainId(),
                            ExceptionUtil.stacktraceToString(e)
                    );
                }
            }
            XxlJobHelper.log("SoB2cRetryJob 执行任务列表结束");
        } finally {
            redisTemplate.delete(redisKey);
        }
        return ReturnT.SUCCESS;
    }

}
