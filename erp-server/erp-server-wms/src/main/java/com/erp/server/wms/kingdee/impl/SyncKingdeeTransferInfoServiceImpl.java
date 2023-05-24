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
import com.erp.model.wms.entity.TransferInfoDetailEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeTransferInfoService;
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
 * 同步金蝶采购入库单单
 * @Author Luo_WG
 * @Date 2023/4/24 11:22
 **/
@Slf4j
@Service
public class SyncKingdeeTransferInfoServiceImpl implements SyncKingdeeTransferInfoService {
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
    public void syncDataToKingdee(TransferInfoEntity entity, String operate) {
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
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Arrays.asList(entity.getInWarehouseId(), entity.getOutWarehouseId()));

        if (StringUtils.isNotBlank(entity.getWarehouseKeeperId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                //仓管员
                resultMap.put("warehouseKeeperCode", findUserDTO.getCode());
            }
        }

        //调拨方向
        resultMap.put("transferDirection", entity.getTransferDirection());
        //备注
        resultMap.put("remark", entity.getRemark());

        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getInOrgId(), entity.getOutOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //调入组织机构编码
            String inOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("inOrgCode", inOrgCode);

            //调出组织机构编码
            String outOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getOutOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("outOrgCode", outOrgCode);
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
                //调入仓库编码
                String inWarehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getInWarehouseId())).map(WarehouseEntity::getKingdeeWarehouseCode).findFirst().orElse(null);
                //调出仓库编码
                String outWarehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getOutWarehouseId())).map(WarehouseEntity::getKingdeeWarehouseCode).findFirst().orElse(null);
                //调入仓库
                jsonObject.put("inWarehouseCode", inWarehouseCode);
                //调出仓库
                jsonObject.put("outWarehouseCode", outWarehouseCode);
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
