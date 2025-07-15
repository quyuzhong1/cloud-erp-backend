package com.erp.server.workflow.job;

import cn.hutool.core.util.ObjectUtil;
import com.erp.model.workflow.entity.ProcessDelegateEntity;
import com.erp.server.workflow.service.ProcessDelegateService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 委托审批xxjob
 * @author will
 * @date 2025/5/27 16:49
 */

@Slf4j
@Component
public class ProcessDelegateJob {
    @Resource
    private ProcessDelegateService processDelegateService;


    /**
     * 状态处理任务
     * @author will
     * @date 2025/5/27 16:57
     * @return void
     */
    @XxlJob("statusHandleJob")
    public void statusHandleJob() {
        XxlJobHelper.log("statusHandleJob start.");
        LocalDateTime now = LocalDateTime.now();
        List<ProcessDelegateEntity> processDelegateList = processDelegateService.listNotEnded(now);
        if (ObjectUtil.isEmpty(processDelegateList)) {
            XxlJobHelper.log("未找到需要处理的数据");
        }
        // 更新状态
        processDelegateList.forEach(entity -> {
            try {
                processDelegateService.updateStatusJob(entity,now);
            } catch (Exception e) {
                XxlJobHelper.log("statusHandleJob error.", e);
            }
        });
        XxlJobHelper.log("statusHandleJob end.");
    }
}
