package com.erp.server.wms.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.wms.entity.MachineDetailEntity;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.model.wms.entity.MachineSubComponentsEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeMachineInfoService;
import com.erp.server.wms.service.MachineDetailService;
import com.erp.server.wms.service.MachineInfoService;
import com.erp.server.wms.service.MachineSubComponentsService;
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
public class SyncKingdeeMachineInfoServiceImpl implements SyncKingdeeMachineInfoService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private MachineInfoService machineInfoService;

    @Resource
    private MachineDetailService machineDetailService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private MachineSubComponentsService machineSubComponentsService;

    @Override
    public void syncDataToKingdee(MachineInfoEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //加工明细
        List<MachineDetailEntity> detailList = machineDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }

        //子件明细
        List<String> subComponentIds = detailList.stream().map(MachineDetailEntity::getId).collect(Collectors.toList());
        List<MachineSubComponentsEntity> machineSubComponentsList = machineSubComponentsService.listByDetailIds(subComponentIds);
        if (CollectionUtils.isEmpty(machineSubComponentsList)) {
            return;
        }
        List<String> warehouseIds = machineSubComponentsList.stream().map(MachineSubComponentsEntity::getWarehouseId).collect(Collectors.toList());
        warehouseIds.add(entity.getWarehouseId());

        //所有仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);

        //人员
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(entity.getWarehouseKeeperId(),entity.getReceiverId()));

        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //其他出库单号
        resultMap.put("code", entity.getCode());
        //其他出库类型
        resultMap.put("type", entity.getType());
        //出库日期
        resultMap.put("billDate", entity.getBillDate());

        if (CollectionUtils.isNotEmpty(userList)) {
            //仓管员
            String warehouseKeeperCode = userList.stream().filter(obj -> obj.getUserId().equals(entity.getWarehouseKeeperId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("warehouseKeeperCode", warehouseKeeperCode);
            //领料人
            String receiverCode = userList.stream().filter(obj -> obj.getUserId().equals(entity.getReceiverId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("receiverCode", receiverCode);
        }

        //事务类型
        resultMap.put("workType", entity.getWorkType());

        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getInventoryOrgId(),entity.getReceiveOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //库存组织编码
            String inventoryOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInventoryOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("inventoryOrgCode", inventoryOrgCode);

            //领料组织编码
            String receiveOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("receiveOrgCode", receiveOrgCode);
        }

        List<JSONObject> list = new ArrayList<>();
        for (MachineDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            //实发数量
            jsonObject.set("qty", detail.getQty());
            //单位
            jsonObject.set("unit", detail.getUnit());

            if (CollectionUtils.isNotEmpty(warehouseList)) {
                //仓库编码
                String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse(null);
                //调出仓库
                jsonObject.set("warehouseCode", warehouseCode);
            }
            //仓位
            jsonObject.set("warehouseLocation", detail.getWarehouseLocation());
            //参照版本
            jsonObject.set("referenceVersion", detail.getReferenceVersion());
            //备注
            jsonObject.set("remark", detail.getRemark());

            //子件
            List<MachineSubComponentsEntity> componentsList = machineSubComponentsList.stream().filter(obj -> obj.getDetailId().equals(detail.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(componentsList)) {
                continue;
            }
            //子件信息录入
            List<JSONObject> subComponents = new ArrayList<>();
            for (MachineSubComponentsEntity machineSubComponents : componentsList) {
                JSONObject subObject = new JSONObject();
                subObject.set("skuNo",machineSubComponents.getSkuNo());
                subObject.set("unit",machineSubComponents.getUnit());
                subObject.set("qty",machineSubComponents.getQty());
                subObject.set("warehouseLocation",machineSubComponents.getWarehouseLocation());
                if (CollectionUtils.isNotEmpty(warehouseList)) {
                    //仓库编码
                    String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(machineSubComponents.getWarehouseId()))
                            .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse("");
                    //子件调出仓库
                    subObject.set("warehouseCode", warehouseCode);
                }
                //备注
                subObject.set("remark", machineSubComponents.getRemark());
                subComponents.add(subObject);
            }
            jsonObject.set("subComponentsList",subComponents);
            list.add(jsonObject);
        }
        resultMap.put("detailList", list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_MACHINE_INFO_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return machineInfoService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(), "",operate);
            }
            return Boolean.TRUE;
        });



    }
}
