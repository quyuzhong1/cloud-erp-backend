package com.erp.server.file.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.FileTaskEventEnum;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.repository.FileTaskRepository;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 重试导入任务
 *
 * @author will
 * @date 2026/04/03 15:00
 */
@Component
@Slf4j
@EnableScheduling
public class RetryImportTaskJob {

    @Resource
    private FileTaskRepository fileTaskRepository;

    /**
     *
     * 重试导入历史任务（物流费用excel导入）
     * @return
     */
    @XxlJob("retryTmsImportHistoryTask")
    public ReturnT<String> retryTmsImportHistoryTask() {
        XxlJobHelper.log("====开始异步任务状态更新====");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", JSONUtil.toJsonStr(jobParam));
        //查询超时的导入任务
        List<FileTask> fileTaskList = fileTaskRepository.listTimeOutImportTask(FileTaskEventEnum.IMPORT_TMS_IMPORT_HISTORY_RECORD.getCode(),4);
        if (CollUtil.isNotEmpty(fileTaskList)) {
            List<String> taskIdList = fileTaskList.stream().map(FileTask::getId).distinct().collect(Collectors.toList());
            log.warn("重试导入历史任务，超时任务id={}", JSONUtil.toJsonStr(taskIdList));
            fileTaskRepository.updateTaskStatus(taskIdList);
        }
        XxlJobHelper.log("====结束异步任务状态更新摊====");
        return ReturnT.SUCCESS;
    }
}
