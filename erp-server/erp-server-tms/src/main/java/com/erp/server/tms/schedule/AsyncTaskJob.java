package com.erp.server.tms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.entity.AsyncTaskRecordEntity;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.AsyncTaskRecordStatusEnum;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.server.tms.service.AsyncTaskDetailRecordService;
import com.erp.server.tms.service.AsyncTaskRecordService;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.FirstMileCostAllocationService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * @author jack
 * @ClassName AsyncTaskJob
 * @description: 异步任务状态更新
 * @date 2026-01-30
 */
@Component
@Slf4j
@EnableScheduling
public class AsyncTaskJob {

    @Resource
    private AsyncTaskRecordService asyncTaskRecordService;

    /**
     *
     *异步任务状态更新
     * @return
     */
    @XxlJob("AsyncTaskJob")
    public ReturnT<String> AsyncTaskJob() {
        XxlJobHelper.log("====开始异步任务状态更新====");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", JSONUtil.toJsonStr(jobParam));

        List<AsyncTaskRecordEntity> list = asyncTaskRecordService.lambdaQuery()
                .eq(AsyncTaskRecordEntity::getStatus, AsyncTaskRecordStatusEnum.ING.getCode())
                .list();
        if(CollUtil.isNotEmpty(list)){
            for (AsyncTaskRecordEntity asyncTaskRecordEntity : list) {
                asyncTaskRecordService.updateTaskFinally(asyncTaskRecordEntity.getId());
            }
        }


        XxlJobHelper.log("====结束异步任务状态更新摊====");
        return ReturnT.SUCCESS;
    }
}
