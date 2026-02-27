package com.erp.server.tms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
    private TmsAsyncTaskRecordService asyncTaskRecordService;

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

        List<TmsAsyncTaskRecordEntity> list = asyncTaskRecordService.lambdaQuery()
                .eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
                .list();
        if(CollUtil.isNotEmpty(list)){
            for (TmsAsyncTaskRecordEntity asyncTaskRecordEntity : list) {
                asyncTaskRecordService.updateTaskFinally(asyncTaskRecordEntity.getId());
            }
        }


        XxlJobHelper.log("====结束异步任务状态更新摊====");
        return ReturnT.SUCCESS;
    }
}
