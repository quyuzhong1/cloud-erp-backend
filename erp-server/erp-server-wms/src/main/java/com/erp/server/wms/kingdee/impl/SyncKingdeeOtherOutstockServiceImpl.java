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
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.entity.OtherOutstockDetailEntity;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeOtherOutstockService;
import com.erp.server.wms.service.OtherOutstockDetailService;
import com.erp.server.wms.service.OtherOutstockService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @description: 同步其他入库单
 * @author Will
 * @date: 2023/5/24 18:56
 */
@Slf4j
@Service
public class SyncKingdeeOtherOutstockServiceImpl implements SyncKingdeeOtherOutstockService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OtherOutstockService otherOutstockService;

    @Resource
    private OtherOutstockDetailService otherOutstockDetailService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private WarehouseService warehouseService;


    @Override
    public void syncDataToKingdee(OtherOutstockEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
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
        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Arrays.asList(entity.getWarehouseId()));

        //员工岗位
        List<KingdeePostDTO.UserKingdeePostInfoDTO> userKingdeePostInfoList = sysUserFeign.listUserKingdeePostByUserIds(Arrays.asList(entity.getReceiverId()));

        if (CollectionUtils.isNotEmpty(userKingdeePostInfoList)) {
            //领料人
            String receiverCode = userKingdeePostInfoList.stream().filter(obj -> obj.getUserId().equals(entity.getReceiverId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeePostCode())).orElse(null);
            resultMap.put("receiverCode", receiverCode);
        }

        //仓管员编码
        if (StringUtils.isNotBlank(entity.getWarehouseKeeperId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("warehouseKeeperCode", findUserDTO.getCode());
            }
        }

        //库存方向
        resultMap.put("inventoryDirection", entity.getInventoryDirection());

        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getInventoryOrgId(),entity.getReceiveOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //库存组织编码
            String inventoryOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInventoryOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("inventoryOrgCode", inventoryOrgCode);
            //收料组织编码
            String receiveOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("receiveOrgCode", receiveOrgCode);
        }

        //部门
        if  (StringUtils.isNotBlank(entity.getDeptId())) {
            SysDepartmentDTO sysDepartmentDTO = sysUserFeign.getUserDeptById(entity.getDeptId());
            if (ObjectUtils.isNotEmpty(sysDepartmentDTO)) {
                resultMap.put("deptCode", sysDepartmentDTO.getCode());
            }
        }


        List<OtherOutstockDetailEntity> detailList = otherOutstockDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }

        List<JSONObject> list = new ArrayList<>();
        for (OtherOutstockDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            //实发数量
            jsonObject.set("actualQty", detail.getActualQty());
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
            //备注
            jsonObject.set("remark", detail.getRemark());

            list.add(jsonObject);
        }
        resultMap.put("list", list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_OTHER_OUTSTOCK_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return otherOutstockService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(), "",operate);
            }
            return Boolean.TRUE;
        });
    }
}
