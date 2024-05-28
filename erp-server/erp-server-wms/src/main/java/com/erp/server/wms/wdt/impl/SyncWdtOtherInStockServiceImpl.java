package com.erp.server.wms.wdt.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.server.wms.wdt.SyncWdtOtherInStockService;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 同步其他入库单到旺店通
 * @author tanmujin
 * @date 2024-05-15
 */
@Slf4j
@Service
public class SyncWdtOtherInStockServiceImpl implements SyncWdtOtherInStockService {

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    public DmpPushTaskEntity saveTask(List<CreateOtherStockinRequest.GoodsList> goodsList, OtherInstockEntity entity, String operateCode, String sourceCode){
        CreateOtherStockinRequest request = new CreateOtherStockinRequest();
        request.setOuterNo(entity.getCode());
        //根据收货仓库ID查询旺店通仓库编号
        ThirdMappingEntity thirdMappingEntity = dmpThirdMappingFeign.getBySysId(entity.getWarehouseId());
        request.setWarehouseNo(Optional.ofNullable(thirdMappingEntity).orElse(new ThirdMappingEntity("")).getThirdInfoId());
        request.setisCheck(Boolean.TRUE);
        request.setGoodsList(goodsList);
        request.setSourceId(entity.getId());
        request.setOperateCode(operateCode);
        request.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        request.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        request.setCreateTime(LocalDateTime.now());

        //添加推送任务
        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(entity.getId());
        dmpSyncTaskDTO.setSourceCode(sourceCode);
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.OTHER_INSTOCK.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.WDT_OTHER_IN_STOCK_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(request));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operateCode);
        return dmpMqFeign.saveTask(dmpSyncTaskDTO);
    }
}
