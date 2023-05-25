package com.erp.server.wms.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.TransferInfoDetailEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeOtherInstockService;
import com.erp.server.wms.service.TransferInfoDetailService;
import com.erp.server.wms.service.TransferInfoService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @description: 同步其他入库单
 * @author Will
 * @date: 2023/5/24 18:56
 */
@Slf4j
@Service
public class SyncKingdeeOtherInstockServiceImpl implements SyncKingdeeOtherInstockService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private TransferInfoDetailService transferInfoDetailService;


    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private WarehouseService warehouseService;


    @Override
    public void syncDataToKingdee(OtherInstockEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //调拨单号
        resultMap.put("code", entity.getCode());
        //调拨类型
        resultMap.put("type", entity.getType());
        //调拨日期
        resultMap.put("billDate", entity.getBillDate());
        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Arrays.asList(entity.getWarehouseId()));

        if (StringUtils.isNotBlank(entity.getWarehouseKeeperId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                //仓管员
                resultMap.put("warehouseKeeperCode", findUserDTO.getCode());
            }
        }

        //调拨方向
        resultMap.put("inventoryDirection", entity.getInventoryDirection());

        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //组织机构编码
            String orgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("orgCode", orgCode);
        }

        List<TransferInfoDetailEntity> detailList = transferInfoDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }

        //获取sku的id集合
        List<String> skuIdList = detailList.stream().map(TransferInfoDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            return;
        }

        List<JSONObject> list = new ArrayList<>();
        for (TransferInfoDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detail.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            //产品名称
            jsonObject.set("productName", productDetailEntity.getName());
            //调拨数量
            jsonObject.set("qty", detail.getQty());
            //单位
            jsonObject.set("unit", detail.getUnit());

            if (CollectionUtils.isNotEmpty(warehouseList)) {
                //仓库编码
                String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId())).map(WarehouseEntity::getKingdeeWarehouseCode).findFirst().orElse(null);

                //调出仓库
                jsonObject.set("warehouseCode", warehouseCode);
            }
            //调出仓位
            jsonObject.set("outWarehouseLocation", detail.getOutWarehouseLocation());
            //调入仓位
            jsonObject.set("inWarehouseLocation", detail.getInWarehouseLocation());
            //备注
            jsonObject.set("remark", detail.getRemark());

            list.add(jsonObject);
        }
        resultMap.put("list", list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_TRANSFER_INFO_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return transferInfoService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(), "",operate);
            }
            return Boolean.TRUE;
        });
    }
}
