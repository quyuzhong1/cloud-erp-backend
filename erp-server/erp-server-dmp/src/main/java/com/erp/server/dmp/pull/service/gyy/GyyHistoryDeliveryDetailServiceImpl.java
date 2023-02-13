package com.erp.server.dmp.pull.service.gyy;

import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.server.dmp.pull.service.IReportHistoryService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 管易云出库详情
 */
@Slf4j
@Component
public class GyyHistoryDeliveryDetailServiceImpl implements IReportHistoryService<GyyDeliveryDetailEntity> {
    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    @Qualifier("gyyDeliveryDetailServiceImpl")
    private IReportSaveService reportSaveService;


    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pullHistoryOrderInfo(RequestDTO requestDTO) throws Exception {
        //拉取数据 存库
        reportSaveService.pullDataSave(requestDTO);
        // 修改任务执行结果信息
        Boolean aBoolean = platformApiTaskService.updateTaskStateById(requestDTO.getJobTaskDTO(), 3);
        if (!aBoolean) {
            throw new RuntimeException("修改任务下次执行时间失败！");
        }
    }
}
