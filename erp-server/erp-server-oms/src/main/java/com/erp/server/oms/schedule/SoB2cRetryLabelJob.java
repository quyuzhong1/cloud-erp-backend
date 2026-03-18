package com.erp.server.oms.schedule;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.server.oms.service.*;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * B2C订单标记发货重试
 */
@Component
@Slf4j
public class SoB2cRetryLabelJob {

    @Resource
    private SoB2cErrorService soB2cErrorService;
    @Resource
    private SoB2cAbnormalService soB2cAbnormalService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private RuleLogisticsService ruleLogisticsService;
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoB2cLabelService soB2cLabelService;

    @Value("${spring.cloud.nacos.discovery.namespace}")
    private String namespace;

    /**
     * B2C订单面单重试任务
     *
     **/
    @XxlJob("SoB2cRetryLabelJob")
    public ReturnT<String> soB2cRetryJob() {
        XxlJobHelper.log("SoB2cRetryLabelJob traceID:"+ TraceContext.traceId());
        XxlJobHelper.log("SoB2cRetryLabelJob 执行开始");
        String jobParam = XxlJobHelper.getJobParam();
        //查询三天内没有获取到面单的数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(3);
        LocalDateTime endTime = LocalDateTime.now().minusHours(1);
        List<String> soIds = soB2cLabelService.getNotLabel(startTime,endTime);
        if(CollectionUtils.isEmpty(soIds)){
            XxlJobHelper.log("SoB2cRetryLabelJob 本次没有需要处理的数据");
            return ReturnT.SUCCESS;
        }
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(soIds);
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cLogisticsService.listByMainIds(soIds);
        XxlJobHelper.log("SoB2cRetryLabelJob 本次需要处理的数据："+soIds);
        for (SoB2cEntity soB2cEntity : soB2cEntityList) {
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.stream()
                    .filter(e -> e.getMainId().equals(soB2cEntity.getId()))
                    .findFirst()
                    .orElse(null);
            if(soB2cLogisticsEntity == null){
                XxlJobHelper.log("SoB2cRetryLabelJob 订单【"+soB2cEntity.getCode()+"】没有找到对应的物流信息，跳过处理");
                continue;
            }
            try {
                BatchResultDTO batchResultDTO = soB2cService.getLogisticsLabel(soB2cEntity,soB2cLogisticsEntity, false);
                if(!batchResultDTO.getSuccess()) {
                    String errorMsg = CharSequenceUtil.sub(batchResultDTO.getMsg(), 0, 2000);
                    XxlJobHelper.log("SoB2cRetryLabelJob 订单【"+soB2cEntity.getCode()+"】获取面单失败："+errorMsg);
                    log.error("SoB2cRetryLabelJob 订单【{}】获取面单失败：{}", soB2cEntity.getCode(), errorMsg);
                }
            } catch (Exception e) {
                String errorMsg = CharSequenceUtil.sub(ExceptionUtil.getRootCauseMessage(e), 0, 2000);
                XxlJobHelper.log("SoB2cRetryLabelJob 订单【"+soB2cEntity.getCode()+"】处理异常："+errorMsg);
                log.error("SoB2cRetryLabelJob 订单【{}】处理异常：{}", soB2cEntity.getCode(), errorMsg, e);
            }
        }
        XxlJobHelper.log("SoB2cRetryLabelJob 执行任务列表结束");
        return ReturnT.SUCCESS;
    }

}
