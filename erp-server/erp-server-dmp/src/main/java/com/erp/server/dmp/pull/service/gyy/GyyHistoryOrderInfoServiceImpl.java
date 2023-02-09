package com.erp.server.dmp.pull.service.gyy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.server.dmp.pull.service.IReportHistoryService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import com.erp.server.dmp.utils.MapCountUtils;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * 管易云订单
 */
@Slf4j
@Component
public class GyyHistoryOrderInfoServiceImpl implements IReportHistoryService<GyyOrderEntity> {
    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;
    @Resource
    @Qualifier("gyyOrderInfoServiceImpl")
    private IReportSaveService reportSaveService;

    /**
     * 拉取订单数据
     *
     * @param dto 任务信息
     * @return
     */
    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
    }

    @Override
    public void pullHistoryOrderInfo(RequestDTO requestDTO) {
        try {
            //拉取数据 存库
            reportSaveService.pullDataSave(requestDTO);
            // 修改任务执行结果信息
            Boolean aBoolean = platformApiTaskService.updateTaskStateById(requestDTO.getJobTaskDTO(), 3);
            if (!aBoolean) {
                throw new RuntimeException("修改任务下次执行时间失败！");
            }
        }catch (Exception e) {
            XxlJobHelper.log(" 管易云拉取数据错误dto={}", JSONUtil.toJsonStr(requestDTO), e);
            String message = e.getMessage();
            DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity(requestDTO.getJobTaskDTO().getId(), JSONUtil.toJsonStr(requestDTO),message, JSONUtil.toJsonStr(e.getStackTrace()));
            dmpErrorLogService.save(dmpErrorLogEntity);
        }
    }

    public static void main(String[] args) {
        HashMap<String, Integer> skuCountMap = new HashMap<>();
        for (int i = 0; i < 10; i++ ) {
            String temp = String.valueOf(i%3);
            skuCountMap.compute(temp, (k, v) -> (v == null) ? 1 : v + 1);
            String erpOrderItemId = "key"+temp;
            erpOrderItemId = StrUtil.format("{}_{}", erpOrderItemId, skuCountMap.get(temp));
            System.out.println("erpOrderItemId = " + erpOrderItemId);
        }
        HashMap<String, Integer> skuCountMap2 = new HashMap<>();
        for (int i = 0; i < 10; i++ ) {
            String sku = String.valueOf(i%3);
            String erpOrderItemId = "key"+sku;
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap2, sku, erpOrderItemId);
            System.out.println("erpOrderItemId2 = " + erpOrderItemId);
        }
    }
}
