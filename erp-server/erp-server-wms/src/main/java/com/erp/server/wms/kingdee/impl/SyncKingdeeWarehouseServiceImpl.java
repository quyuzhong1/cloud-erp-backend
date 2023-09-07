package com.erp.server.wms.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeWarehouseService;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.WarehouseLocationService;
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
 * 同步金蝶采购退货单
 * @Author Luo_WG
 * @Date 2023/4/24 11:22
 **/
@Slf4j
@Service
public class SyncKingdeeWarehouseServiceImpl implements SyncKingdeeWarehouseService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private WarehouseLocationService warehouseLocationService;


    /**
     * 发送消息同步金蝶
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     * @param entity
     * @param operate
     * @return void
     **/
    @Override
    public void syncDataToKingdee(WarehouseEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //更新同步状态为待同步
        warehouseService.updateSyncKingdeeStatus(entity.getId(), SyncStatusEnum.TO_BE_SYNC.getCode(),"",operate);

        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id",entity.getId());
        //仓库名称
        resultMap.put("name",entity.getName());
        //金蝶编号
        resultMap.put("code",entity.getKingdeeWarehouseCode());

        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String orgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
            String orgKingdeeId = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getFlagId())).orElse("");

            //仓库组织
            resultMap.put("orgCode",orgCode);

            //组织的金蝶id
            resultMap.put("createOrgId",orgKingdeeId);
        }

        //仓库地址
        resultMap.put("address",entity.getAddress());
        //仓库电话
        resultMap.put("tel",entity.getContactTelNumber());
        //是否禁用
        resultMap.put("disabled",entity.getDisabled());
        //是否启用仓位
        resultMap.put("isEnableLocation",entity.getIsEnableLocation());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //审核未通过、非反审核不推送
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus().getStatus()) && !SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            return;
        }

        //仓库类型
        DictBasicEntity type = dictBasicService.getById(entity.getTypeId());
        if (ObjectUtils.isNotEmpty(type)) {
            resultMap.put("type",type.getValue());
        }
        //仓库负责人
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getChargeId());
        if (ObjectUtils.isNotEmpty(findUserDTO)) {
            resultMap.put("chargeCode",findUserDTO.getCode());
        }
        //查询仓位
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIds(Arrays.asList(entity.getId()));
        if (CollectionUtils.isNotEmpty(warehouseLocationList) && entity.getIsEnableLocation()) {
            //区域
            List<WarehouseLocationEntity> areaList = warehouseLocationList.stream().filter(obj -> obj.getWarehouseId().equals(obj.getWarehouseId()) && WarehouseLocationTypeEnum.AREA.getCode().equals(obj.getType())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(areaList)) {
                log.error("仓库对应区域未找到，code = {},name = {}",entity.getKingdeeWarehouseCode(),entity.getName());
                return;
            }
            //仓位
            List<WarehouseLocationEntity> locationList = warehouseLocationList.stream().filter(obj -> obj.getWarehouseId().equals(obj.getWarehouseId()) && WarehouseLocationTypeEnum.LOCATION.getCode().equals(obj.getType())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(locationList)) {
                log.error("仓库对应仓位未找到，code = {},name = {}",entity.getKingdeeWarehouseCode(),entity.getName());
                return;
            }
            List<JSONObject> areaJsonList = new ArrayList<>();
            for (WarehouseLocationEntity area : areaList) {
                JSONObject areaJson = new JSONObject();
                areaJson.set("code",area.getCode());

                //区域下仓位
                List<WarehouseLocationEntity> childLocationList = locationList.stream().filter(obj -> area.getId().equals(obj.getParentId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(locationList)) {
                    log.error("仓库对应区域下仓位未找到，code = {},name = {},area = {}",entity.getKingdeeWarehouseCode(),entity.getName(),area.getCode());
                    return;
                }
                List<JSONObject> locationJsonList = new ArrayList<>();
                for (WarehouseLocationEntity childLocation : childLocationList) {
                    JSONObject childLocationJson = new JSONObject();
                    childLocationJson.set("code",childLocation.getCode());
                    locationJsonList.add(childLocationJson);
                }
                areaJson.set("locationJsonList",locationJsonList);
                areaJsonList.add(areaJson);
            }
            resultMap.put("areaJsonList",areaJsonList);
        }

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_WAREHOUSE_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return warehouseService.updateSyncKingdeeStatus(entity.getId(), SyncStatusEnum.IN_SYNC.getCode(),"",operate);
            }
            return Boolean.TRUE;
        });

    }
}
