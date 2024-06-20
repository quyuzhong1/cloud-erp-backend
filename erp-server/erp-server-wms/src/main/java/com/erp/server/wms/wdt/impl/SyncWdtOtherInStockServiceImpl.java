package com.erp.server.wms.wdt.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        //查询推送任务表，如果有了相同的来源单据号，则序号累加
        DmpSyncTaskDTO.ListDTO param = new DmpSyncTaskDTO.ListDTO(Collections.singletonList(entity.getId()), PlatformEnum.WANGDIAN.getDesc(), PlatformEnum.ERP.getDesc());
        List<DmpPushTaskEntity> taskList = dmpMqFeign.listByParam(param);
        Optional<CreateOtherStockinRequest> optional = taskList.stream()
                .filter(task -> task.getSyncOperate().equalsIgnoreCase(operateCode))
                .map(task -> JSON.parseObject(task.getMqData(), CreateOtherStockinRequest.class))
                .max((o1, o2) -> ObjectUtil.compare(o1.getOuterNo(), o2.getOuterNo()));
        if(optional.isPresent()){
            String maxOuterNo = optional.get().getOuterNo();
            if(maxOuterNo.contains("_")){
                String[] split = maxOuterNo.split("_");
                Integer seq = Integer.parseInt(split[1]) + 1;
                request.setOuterNo(split[0] + "_" + String.format("%03d", seq));
            }else {
                request.setOuterNo(entity.getCode() + "_001");
            }
        }

        //根据收货仓库ID查询旺店通仓库编号
        ThirdWarehouseEntity thirdWarehouse = dmpThirdMappingFeign.getBySysId(entity.getWarehouseId(), "wdt");
        request.setWarehouseNo(Optional.ofNullable(thirdWarehouse).orElse(new ThirdWarehouseEntity()).getCode());
        request.setisCheck(Boolean.TRUE);
        request.setGoodsList(goodsList);
        request.setSourceId(entity.getId());
        request.setOperateCode(operateCode);
        request.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        request.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        request.setCreateTime(LocalDateTime.now());
        request.setRemark("原始单据号：" + sourceCode);

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
