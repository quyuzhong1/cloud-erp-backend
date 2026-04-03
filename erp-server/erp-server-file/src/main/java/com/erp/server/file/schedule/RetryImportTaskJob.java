package com.erp.server.file.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.server.file.context.FileTaskContext;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.repository.FileTaskRepository;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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

    @Resource
    private FileTaskContext fileTaskContext;

    /**
     *
     * 重试导入历史任务（
     * @return
     */
    @XxlJob("retryImportTask")
    public ReturnT<String> retryImportTask() {
        XxlJobHelper.log("====开始查询超时导入任务====");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", JSONUtil.toJsonStr(jobParam));
        if (CharSequenceUtil.isBlank(jobParam)) {
            XxlJobHelper.log("任务参数不能为空,任务参数={}", JSONUtil.toJsonStr(jobParam));
            return ReturnT.FAIL;
        }
        JSONObject jsonParam = JSON.parseObject(jobParam);
        String event = jsonParam.getString("event");
        Integer hours = jsonParam.getInteger("hours");
        if (CharSequenceUtil.isBlank(event) || Objects.isNull(hours)) {
            XxlJobHelper.log("任务参数不合法,任务参数={}", JSONUtil.toJsonStr(jobParam));
            return ReturnT.FAIL;
        }

        //查询超时的导入任务
        List<FileTask> fileTaskList = fileTaskRepository.listTimeOutImportTask(event,hours);
        if (CollUtil.isNotEmpty(fileTaskList)) {
            List<String> taskIdList = fileTaskList.stream().map(FileTask::getId).distinct().collect(Collectors.toList());
            XxlJobHelper.log("重试导入历史任务，超时任务id={}", JSONUtil.toJsonStr(taskIdList));
            fileTaskRepository.updateTaskStatus(taskIdList);
        }
        XxlJobHelper.log("====开始自动重试待处理的导入任务====");

        //查询待处理的导入任务
        List<FileTask> pendingTaskList = fileTaskRepository.getExpireByStatuses(LocalDateTime.now(), FileTaskStatusEnum.PENDING);
        if (CollUtil.isEmpty(pendingTaskList)) {
            XxlJobHelper.log("无待处理的任务需要重试");
            return ReturnT.SUCCESS;
        }
        //重试待处理的导入任务
        for (FileTask fileTask : pendingTaskList) {
            String id = fileTask.getId();
            XxlJobHelper.log("重试待处理的导入任务，id={}", id);
            fileTaskContext.retry(id);
        }
        XxlJobHelper.log("====结束异步任务状态更新摊====");
        return ReturnT.SUCCESS;
    }
}
