package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.util.ObjectUtil;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.service.IReportHistoryService;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class PullMabangShopJob {
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    @Qualifier("kingdeeCustomerServiceImpl")
    private IReportHistoryService historyDeliveryService;

    @XxlJob("kingdeeCustomerJob")
    public ReturnT<String> kingdeeCustomer() throws Exception {
        XxlJobHelper.log("kingdeeCustomerJob 任务开始执行！");
        // 查询对应任务配置
        PlatformApiTaskEntity entity = platformApiTaskService.getByApiCode(PlatformApiEnum.BD_CUSTOMER.getTaskName());
        if(ObjectUtil.isEmpty(entity)){
            XxlJobHelper.log("{}任务task记录为空异常", PlatformApiEnum.BD_CUSTOMER.getTaskName());
            return ReturnT.SUCCESS;
        }
        JobTaskDTO jobTaskDTO = new JobTaskDTO(entity);
        // 执行拉取任务
        //通过枚举获取对应service
        RequestDTO requestDTO = new RequestDTO(jobTaskDTO, PlatformApiEnum.BD_CUSTOMER);
        historyDeliveryService.pullHistoryOrderInfo(requestDTO);
        XxlJobHelper.log("kingdeeCustomerJob 任务执行结束！");
        return ReturnT.SUCCESS;
    }
}
