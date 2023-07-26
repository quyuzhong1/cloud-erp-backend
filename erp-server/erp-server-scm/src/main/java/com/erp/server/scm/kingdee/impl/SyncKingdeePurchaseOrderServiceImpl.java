package com.erp.server.scm.kingdee.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.scm.entity.*;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchaseOrderService;
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

 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeePurchaseOrderServiceImpl implements SyncKingdeePurchaseOrderService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private SubcontractChangeService subcontractChangeService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;


    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDataToKingdee(PurchaseOrderEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //更新同步状态为待同步
        purchaseOrderService.updateSyncKingdeeStatus(Arrays.asList(entity.getId()),SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(),"",operate);

        //如果上游单据未发送成功则无需发送
        SubcontractOrderEntity subcontractOrderEntity = null;
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = null;
        if (StringUtils.isNotBlank(entity.getSubcontractType())) {
            //委外订单
            subcontractOrderEntity = subcontractOrderService.getById(entity.getSourceId());
            if (ObjectUtils.isEmpty(subcontractOrderEntity)) {
                throw new ServiceException(ApiError.ERROR_98073);
            }
            if (!SyncKingdeeStatusEnum.SUCCESS_SYNC.getCode().equals(subcontractOrderEntity.getSyncKingdeeStatus())) {
                log.error("委外订单未推送成功，不支持推送采购订单，委外订单号【{}】",subcontractOrderEntity.getCode());
                return;
            }
            //委外订单明细
            subcontractOrderDetailList = subcontractOrderDetailService.listByMainId(subcontractOrderEntity.getId());

            //委外变更单
            List<SubcontractChangeEntity> subcontractChangeList = subcontractChangeService.listBySourceIds(Arrays.asList(subcontractOrderEntity.getId()));
            if (CollectionUtils.isNotEmpty(subcontractChangeList)) {
                String changeCodes = subcontractChangeList.stream().filter(obj -> !SyncKingdeeStatusEnum.SUCCESS_SYNC.getCode().equals(obj.getSyncKingdeeStatus())).map(SubcontractChangeEntity::getCode).collect(Collectors.joining(","));
                if (StringUtils.isNotBlank(changeCodes)) {
                    log.error("委外变更单未推送成功，不支持推送采购订单，委外变更单号【{}】",changeCodes);
                    return;
                }
            }
        }

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //采购日期
        resultMap.put("purchaseDate",entity.getPurchaseDate());

        //委外采购订单
        if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(entity.getSourceType())) {
            resultMap.put("sourceType",SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
        } else {
            resultMap.put("sourceType",SourceTypeEnum.PURCHASE_ORDER.getCode());
        }


        //查询采购供应商
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(entity.getId());
        if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
            return;
        }
        SupplierEntity supplierEntity = supplierService.getById(purchaseOrderSupplierEntity.getSupplierId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
            return;
        }

        //供应商编码
        resultMap.put("supplierCode",supplierEntity.getCode());

        //采购部门
        resultMap.put("purchaseDeptName",entity.getPurchaseDeptName());

        //获取用户部门id
        if (StringUtils.isNotBlank(entity.getPurchaseDeptId())) {
            SysDepartmentDTO departmentDTO = sysUserFeign.getUserDeptById(entity.getPurchaseDeptId());
            //采购部门
            if (ObjectUtil.isNotEmpty(departmentDTO)) {
                resultMap.put("purchaseDeptCode", departmentDTO.getCode());
            }
        }

        //采购员编码
        if (StringUtils.isNotBlank(entity.getPurchaseUserId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getPurchaseUserId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("purchaseUserCode", findUserDTO.getCode());
            }
        }
        //供应商联系人
        resultMap.put("contactName",purchaseOrderSupplierEntity.getContactName());
        //是否是新品首批
        resultMap.put("isFirstMassProduct",entity.getIsFirstMassProduct());

        if (ObjectUtils.isNotEmpty(purchaseOrderSupplierEntity.getPaymentCondition())) {
            //付款条件
            resultMap.put("paymentCondition",purchaseOrderSupplierEntity.getPaymentCondition());
        }

        //采购明细
        List<PurchaseOrderDetailEntity> details = purchaseOrderDetailService.listByPurchaseOrderId(entity.getId());
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getPurchaseOrgId(),entity.getReceiveOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //采购组织编码
            String purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("purchaseOrgCode", purchaseOrgCode);
        }

        //部门信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(entity.getDeliveryWarehouseId()));

        List<JSONObject> list = new ArrayList<>();
        for (PurchaseOrderDetailEntity detailEntity : details) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("detailId",detailEntity.getId());
            jsonObject.set("skuNo",detailEntity.getSkuNo());
            jsonObject.set("purchaseQty",detailEntity.getPurchaseQty());
            jsonObject.set("planDeliveryDate",detailEntity.getPlanDeliveryDate());
            jsonObject.set("price", MathUtil.divide(detailEntity.getTaxPrice(),MathUtil.add(MathUtil.BigDecimal_1,detailEntity.getTaxRate())) );
            jsonObject.set("taxPrice",detailEntity.getTaxPrice());
            //部门编码
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String kingdeeWarehouseCode = warehouseList.get(0).getKingdeeWarehouseCode();
                jsonObject.set("kingdeeWarehouseCode",kingdeeWarehouseCode);
            }
            jsonObject.set("taxRate",MathUtil.multiply(detailEntity.getTaxRate(),MathUtil.BigDecimal_100));
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                //收料组织编码
                String receiveOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
                jsonObject.set("receiveOrgCode", receiveOrgCode);
            }
            jsonObject.set("isGift",detailEntity.getIsGift());
            jsonObject.set("detailRemark",detailEntity.getRemark());
            //来源单据类型类型
            if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(entity.getSourceType())) {
                jsonObject.set("detailSourceType", KingdeePushModuleEnum.SUB_SUBREQORDER.getCode());
            }
            if (ObjectUtils.isNotEmpty(subcontractOrderEntity)) {
                //委外单号
                jsonObject.set("refCode", subcontractOrderEntity.getCode());
            }

            //委外订单关联关系
            List<Map<String,Object>> refList = new ArrayList<>();
            JSONObject refJsonObject = new JSONObject();
            if (ObjectUtils.isNotEmpty(subcontractOrderEntity)) {
                refJsonObject.set("refKingdeeId",subcontractOrderEntity.getSyncKingdeeId());
                if (CollectionUtils.isNotEmpty(subcontractOrderDetailList)) {
                    String subDetailKingdeeId = subcontractOrderDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeDetailId())).orElse("");
                    refJsonObject.set("refDetailKingdeeId",subDetailKingdeeId);
                }
                refList.add(refJsonObject);
                jsonObject.set("refList",refList);
            }
            list.add(jsonObject);
        }
        resultMap.put("list",list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_PURCHASE_ORDER_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return purchaseOrderService.updateSyncKingdeeStatus(Arrays.asList(entity.getId()), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"",operate);
            }
            return Boolean.TRUE;
        });
    }
}
