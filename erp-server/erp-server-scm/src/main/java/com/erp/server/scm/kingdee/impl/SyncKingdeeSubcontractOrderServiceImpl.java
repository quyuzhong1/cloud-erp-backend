package com.erp.server.scm.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSubcontractOrderService;
import com.erp.server.scm.service.SubcontractOrderDetailService;
import com.erp.server.scm.service.SubcontractOrderService;
import com.erp.server.scm.service.SupplierService;
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

 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeeSubcontractOrderServiceImpl implements SyncKingdeeSubcontractOrderService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(SubcontractOrderEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //采购日期
        resultMap.put("billDate",entity.getBillDate());

        //采购员编码
        if (StringUtils.isNotBlank(entity.getPurchaserId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getPurchaserId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("purchaseUserCode", findUserDTO.getCode());
            }
        }
        //是否是新品首批
        resultMap.put("isFirstMassProduct",entity.getIsFirstMassProduct());


        //采购明细
        List<SubcontractOrderDetailEntity> details = subcontractOrderDetailService.listByMainId(entity.getId());
        //父级数据
        List<SubcontractOrderDetailEntity> parentList = details.stream().filter(obj -> StringUtils.isBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }

        //仓库信息
        List<String> warehouseIds = details.stream().map(SubcontractOrderDetailEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIds);

        //供应商信息
        List<String> supplierIds = details.stream().map(SubcontractOrderDetailEntity::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = supplierService.listByIds(supplierIds);

        //组织机构
        List<String> orgIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            List<String> orgIds = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
            orgIdList.addAll(orgIds);
        }
        orgIdList.add(entity.getSubcontractOrgId());
        orgIdList.add(entity.getPurchaseOrgId());
        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //委外组织编码
            String subcontractOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSubcontractOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("subcontractOrgCode", subcontractOrgCode);
        }

        List<JSONObject> list = new ArrayList<>();
        for (SubcontractOrderDetailEntity detailEntity : parentList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("detailId",detailEntity.getId());
            jsonObject.set("skuNo",detailEntity.getSkuNo());
            jsonObject.set("qty",detailEntity.getQty());
            jsonObject.set("planDeliveryDate",detailEntity.getPlanDeliveryDate());
            jsonObject.set("price",detailEntity.getPrice());
            //单据日期
            jsonObject.set("billDate",entity.getBillDate());

            //仓库编码
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).findFirst().orElse(null);
                jsonObject.set("kingdeeWarehouseCode",updateDTO.getKingdeeWarehouseCode());
                //库存组织
                if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                    String inStockOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(updateDTO.getOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
                    jsonObject.set("inStockOrgCode",inStockOrgCode);
                }
            }
            //采购组织编码
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                String purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
                jsonObject.set("purchaseOrgCode", purchaseOrgCode);
            }
            //供应商编码
            if (CollectionUtils.isNotEmpty(supplierList)) {
                String supplierCode = supplierList.stream().filter(obj -> obj.getId().equals(detailEntity.getSupplierId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
                jsonObject.set("supplierCode",supplierCode);
            }
            String referenceVersion = detailEntity.getSkuNo() + "_" + detailEntity.getBomVersion();
            //参照版本
            jsonObject.set("referenceVersion", referenceVersion);
            jsonObject.set("detailRemark",detailEntity.getRemark());
            list.add(jsonObject);
        }
        resultMap.put("list",list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SUBCONTRACT_ORDER_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return subcontractOrderService.updateSyncKingdeeStatus(Arrays.asList(entity.getId()), SyncStatusEnum.IN_SYNC.getCode(),"",operate);
            }
            return Boolean.TRUE;
        });
    }
}
