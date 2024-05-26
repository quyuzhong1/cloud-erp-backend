package com.erp.server.wms.wdt.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import com.erp.server.wms.wdt.SyncWdtOtherOutStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 将erp其他出库单同步至旺店通
 *
 * @author tanmujin
 * @date 2024-05-16
 */
@Slf4j
@Service
public class SyncWdtOtherOutStockServiceImpl implements SyncWdtOtherOutStockService {

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Override
    public DmpPushTaskEntity saveTask(List<CreateOtherStockoutRequest.GoodsList> goodsList, OtherOutstockEntity entity, String operateCode) {
        CreateOtherStockoutRequest request = new CreateOtherStockoutRequest();
        request.setOuterNo(entity.getCode());
        request.setWarehouseNo("wjkj03-test");  //todo 根据出货仓库匹配旺店通仓库编号, @仓库数据任务
        request.setisCheck(Boolean.TRUE);
        request.setGoodsList(goodsList);

        //添加推送任务
        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(entity.getId());
        dmpSyncTaskDTO.setSourceCode(entity.getCode());
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.OTHER_OUTSTOCK.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.WDT_OTHER_OUT_STOCK_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(request));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operateCode);
        return dmpMqFeign.saveTask(dmpSyncTaskDTO);
    }
}
