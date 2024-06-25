package com.erp.server.oms.schedule;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.SyncOperateEnum;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.server.oms.service.SoB2cService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class BiOrderCompensateJob {
    @Resource
    private SoB2cService soB2cService;

    /**
     * bi订单补偿推送
     */
    @XxlJob("BiOrderCompensateJob")
    public void biOrderCompensateJob() {
        XxlJobHelper.log("bi订单补偿推送开始执行");
        String jobParam = XxlJobHelper.getJobParam();

        LocalDateTime createStartTime = null;
        LocalDateTime createEndTime = null;
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            createStartTime = jsonParam.getLocalDateTime("createStartTime", LocalDateTime.now().minusMonths(1));
            createEndTime = jsonParam.getLocalDateTime("createEndTime", LocalDateTime.now());
        }

        XxlJobHelper.log("bi订单补偿推送定时任务参数，开始时间：{}，结束时间：{}", createStartTime, createEndTime);

        if (createStartTime != null && createEndTime != null) {
            List<SoB2cEntity> soB2cEntities = soB2cService.listByCreateTime(createStartTime, createEndTime);
            soB2cEntities.forEach(req -> {
                soB2cService.syncOrderToDmp(req.getId(), SyncOperateEnum.OPERATE_APPROVE.getCode());
            });
        }

        XxlJobHelper.log("bi订单补偿推送执行结束");
    }
}
