package com.erp.server.scm.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.OptChangeTypeEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.scm.entity.*;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSubcontractChangeService;
import com.erp.server.scm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeeSubcontractChangeServiceImpl implements SyncKingdeeSubcontractChangeService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private SubcontractChangeService subcontractChangeService;

    @Resource
    private SubcontractChangeDetailService subcontractChangeDetailService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(SubcontractChangeEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //采购日期
        resultMap.put("billDate",entity.getBillDate());


        //采购明细
        List<SubcontractChangeDetailEntity> details = subcontractChangeDetailService.listByMainIds(Arrays.asList(entity.getId()));
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        //父级数据
        List<SubcontractChangeDetailEntity> parentList = details.stream().filter(obj -> StringUtils.isBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }

        //仓库信息
        List<String> warehouseIds = details.stream().map(SubcontractChangeDetailEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIds);

        //供应商信息
        List<String> supplierIds = details.stream().map(SubcontractChangeDetailEntity::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = supplierService.listByIds(supplierIds);

        //根据明细skuIds查询bom
        List<String> skuIds = parentList.stream().map(SubcontractChangeDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomInfoEntity> bomInfoList = plmTaskFeign.listBomByParentSkuIds(skuIds);

        //委外订单主表数据
        SubcontractOrderEntity subcontractOrderEntity = subcontractOrderService.getById(entity.getSourceId());
        if (ObjectUtils.isEmpty(subcontractOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }


        List<String> sourceDetailIds = parentList.stream().map(SubcontractChangeDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = subcontractOrderDetailService.listByIds(sourceDetailIds);
        if (CollectionUtils.isEmpty(subcontractOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }

        List<JSONObject> list = new ArrayList<>();
        for (SubcontractChangeDetailEntity detailEntity : parentList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("skuNo",detailEntity.getSkuNo());
            jsonObject.set("qty",detailEntity.getQty());
            jsonObject.set("price",detailEntity.getPrice());
            //单据日期
            resultMap.put("billDate",entity.getBillDate());
            //仓库编码
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String kingdeeWarehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse("");
                jsonObject.set("warehouseCode",kingdeeWarehouseCode);
            }
            //供应商编码
            if (CollectionUtils.isNotEmpty(supplierList)) {
                String supplierCode = supplierList.stream().filter(obj -> obj.getId().equals(detailEntity.getSupplierId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
                jsonObject.set("supplierCode",supplierCode);
            }
            if (CollectionUtils.isNotEmpty(bomInfoList)) {
                String referenceVersion = bomInfoList.stream().filter(obj -> obj.getParentSkuId().equals(detailEntity.getSkuId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getSerialNumber()+ "_"+ obj.getVersion())).orElse(null);
                //参照版本
                jsonObject.set("referenceVersion", referenceVersion);
            }
            //金蝶委外明细id
            String kingdeeSubEntryId = subcontractOrderDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(detailEntity.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeDetailId())).orElse("");
            jsonObject.set("kingdeeSubEntryId",kingdeeSubEntryId);
            //金蝶委外id
            jsonObject.set("kingdeeSubId",subcontractOrderEntity.getSyncKingdeeId());
            //金蝶委外订单编号
            jsonObject.set("subCode",subcontractOrderEntity.getCode());
            //备注
            jsonObject.set("detailRemark",detailEntity.getRemark());
            //变更操作类型
            jsonObject.set("optType",detailEntity.getOptType());

            //添加修改后数据
            list.add(jsonObject);

            if (OptChangeTypeEnum.UPDATE.getCode().equals(detailEntity.getOptType())) {
                //添加修改前数据
                JSONObject oldJsonObject = new JSONObject();
                oldJsonObject.putAll(jsonObject);
                oldJsonObject.set("qty",detailEntity.getOldQty());
                oldJsonObject.set("price",detailEntity.getOldPrice());
                oldJsonObject.set("optType","updateBefore");
                list.add(oldJsonObject);
            }
        }
        resultMap.put("list",list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SUBCONTRACT_CHANGE_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return subcontractChangeService.updateSyncKingdeeStatus(Arrays.asList(entity.getId()), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"",operate);
            }
            return Boolean.TRUE;
        });
    }
}
