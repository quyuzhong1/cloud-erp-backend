package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.business.constant.TaskConstant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IReportSaveService;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.server.dmp.pull.thread.PullErpDateThread;
import com.erp.server.dmp.service.DmpErrorLogService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 领星相关任务
 *
 * @Author Jim
 * @Date 2024/02/18
 **/

@Component
@Slf4j
public class PullLingxingJob {

    @Resource
    private PullErpDateThread pullErpDateThread;
    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    private DmpErrorLogService dmpErrorLogService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    @Qualifier("lxShopInfoServiceImpl")
    private IReportSaveService lxShopInfoService;


    /**
     * 领星相关任务
     */
    @XxlJob("lingxingExecute")
    public ReturnT<String> lingxingExecute(){
        threadPoolTaskExecutor.execute(() -> {
            pullErpDateThread.executeTask(TaskConstant.LX_PULL_DATA_TASK);
        });
        return ReturnT.SUCCESS;
    }

    /**
     * 领星清洗相关任务
     */
    @XxlJob("lingxingCleanExecute")
    public void lingxingCleanExecute() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("领星清洗任务参数：{}", jobParam);
        List<String> taskList = new ArrayList<>();
        if (StrUtil.isNotBlank(jobParam)) {
            taskList = Arrays.asList(jobParam.split(","));
        }
        pullErpDateThread.executeCleanTask(TaskConstant.LX_PULL_DATA_TASK, taskList);
    }

}
