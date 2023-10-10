package com.erp.server.scm.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PushSyncStatusDTO;
import com.common.business.enums.SubcontractTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.scm.entity.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchaseChangeService;
import com.erp.server.scm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: 采购变更单推送金蝶
 * @date 2023/9/28 16:18
 */
@Slf4j
@Service
public class SyncKingdeePurchaseChangeServiceImpl implements SyncKingdeePurchaseChangeService {

    @Resource
    private PurchaseChangeService purchaseChangeService;

    @Resource
    private PurchaseChangeDetailService purchaseChangeDetailService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private MQProducerService mQProducerService;


    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDataToKingdee(PurchaseChangeEntity entity, String operate) {

        //更新同步状态为待同步
        PushSyncStatusDTO.KingdeeDTO kingdeeDTO = new PushSyncStatusDTO.KingdeeDTO(entity.getId(),operate,"", SyncStatusEnum.TO_BE_SYNC.getCode());
        purchaseChangeService.updateSyncKingdeeStatus(kingdeeDTO);

        //采购订单未同步成功则无需推送采购变更
        PurchaseOrderEntity purchaseOrderEntity = purchaseOrderService.getById(entity.getPurchaseOrderId());
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        if (!SyncStatusEnum.SUCCESS_SYNC.getCode().equals(purchaseOrderEntity.getSyncKingdeeStatus())) {
            return;
        }
        Map<String, Object> resultMap = new HashMap<>();
        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());

        //判断是标准采购还是委外采购
        if (SubcontractTypeEnum.ENUM_PARENT.getCode().equals(purchaseOrderEntity.getSubcontractType())) {
            resultMap.put("sourceType", KingdeePushModuleEnum.SUB_SUBREQORDER.getCode());
        } else {
            resultMap.put("sourceType", KingdeePushModuleEnum.PUR_PURCHASEORDER.getCode());
        }

        //变更人
        if (StringUtils.isNotBlank(entity.getChangeUserId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getChangeUserId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("changeUserCode", findUserDTO.getCode());
            }
        }

        //变更日期
        resultMap.put("changeDate",entity.getChangeDate());
        //采购日期
        resultMap.put("purchaseDate",purchaseOrderEntity.getPurchaseDate());
        //供应商
        SupplierEntity supplierEntity = supplierService.getById(entity.getSupplierId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
            log.error("未找到供应商，supplierId = {}",entity.getSupplierId());
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        resultMap.put("supplierCode",supplierEntity.getCode());
        //来源单号
        resultMap.put("sourceCode",purchaseOrderEntity.getCode());
        //来源单据金蝶id
        resultMap.put("sourceSyncKingdeeId",purchaseOrderEntity.getSyncKingdeeId());
        //采购组织
        String purchaseOrgCode = "";
        //收料组织
        String receiveOrgCode = "";
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getPurchaseOrgId(),entity.getReceiveOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //采购组织编码
            purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("purchaseOrgCode", purchaseOrgCode);
            //收料组织编码
            receiveOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
        }
        //变更原因
        resultMap.put("changeReason","采购订单变更");

        //变更明细
        List<PurchaseChangeDetailEntity> detailList = purchaseChangeDetailService.listByPurchaseChangeIds(Arrays.asList(entity.getId()));
        if (CollectionUtils.isEmpty(detailList)) {
            log.error("未找到变更明细，changeId = {}",entity.getId());
            throw new ServiceException(ApiError.ERROR_98042);
        }
        List<String> purchaseDetailIdList = detailList.stream().map(PurchaseChangeDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByIds(purchaseDetailIdList);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            log.error("未找到采购订单明细，purchaseDetailIdList = {}",purchaseDetailIdList);
            throw new ServiceException(ApiError.ERROR_98026);
        }

        //明细信息
        List<JSONObject> list = new ArrayList<>();
        for (PurchaseChangeDetailEntity detailEntity : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo",detailEntity.getSkuNo());
            //原数量
            jsonObject.set("oldQty",detailEntity.getOldQty());
            //新数量
            jsonObject.set("qty",detailEntity.getQty());
            //原单价
            jsonObject.set("oldPrice",detailEntity.getOldPrice());
            //新单价
            jsonObject.set("price",detailEntity.getPrice());
            //收料组织编码
            jsonObject.set("receiveOrgCode",receiveOrgCode);
            //结算组织编码
            jsonObject.set("purchaseOrgCode",purchaseOrgCode);
            //明细备注
            jsonObject.set("detailRemark",detailEntity.getRemark());
            //来源单号
            jsonObject.set("sourceCode",purchaseOrderEntity.getCode());
            //采购明细
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseOrderDetailEntity)) {
                log.error("未找到采购订单明细，purchaseDetailIdList = {}",detailEntity.getPurchaseOrderDetailId());
                throw new ServiceException(ApiError.ERROR_98026);
            }
            //税率
            jsonObject.set("taxRate",purchaseOrderDetailEntity.getTaxRate());
            //源单分录内码
            jsonObject.set("refKingdeeDetailId",purchaseOrderDetailEntity.getKingdeeDetailId());

            JSONObject refJsonObject = new JSONObject();
            //源单内码
            refJsonObject.set("refKingdeeId",purchaseOrderEntity.getSyncKingdeeId());
            //源单分录内码
            refJsonObject.set("refKingdeeDetailId",purchaseOrderDetailEntity.getKingdeeDetailId());

            jsonObject.set("refList",Arrays.asList(refJsonObject));
            list.add(jsonObject);
        }
        //明细信息
        resultMap.put("detailList",list);
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_PURCHASE_CHANGE_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                PushSyncStatusDTO.KingdeeDTO syncKingdeeDTO = new PushSyncStatusDTO.KingdeeDTO(entity.getId(),operate,"", SyncStatusEnum.IN_SYNC.getCode());
                return purchaseChangeService.updateSyncKingdeeStatus(syncKingdeeDTO);
            }
            return Boolean.TRUE;
        });

    }
}
