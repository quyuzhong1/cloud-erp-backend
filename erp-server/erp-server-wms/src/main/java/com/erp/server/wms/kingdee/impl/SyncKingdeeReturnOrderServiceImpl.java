package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.*;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.PurchaseReturnOrderEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.kingdee.SyncKingdeeReturnOrderService;
import com.erp.server.wms.service.PurchaseReturnOrderDetailService;
import com.erp.server.wms.service.PurchaseReturnOrderService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class SyncKingdeeReturnOrderServiceImpl implements SyncKingdeeReturnOrderService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PurchaseReturnOrderService purchaseReturnOrderService;

    @Resource
    private PurchaseReturnOrderDetailService purchaseReturnOrderDetailService;

    @Resource
    private MQProducerService mQProducerService;

    /**
     * 发送消息同步金蝶
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     * @param entity
     * @param operate
     * @return void
     **/
    @Override
    public void syncDataToKingdee(PurchaseReturnOrderEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id",entity.getId());
        //退货单号
        resultMap.put("code",entity.getCode());
        //退料组织
        resultMap.put("returnOrgName",entity.getReturnOrgName());
        //获取用户部门id
        SysDepartmentUserNumberDTO departmentDTO = sysUserFeign.getDeptByUserId(entity.getPurchaseUserId());
        //获取用户部门信息
        SysDepartmentDTO department = sysUserFeign.getUserDeptById(departmentDTO.getDepartmentId());
        //采购部门
        resultMap.put("returnOrgName",department.getName());
        //退货日期
        resultMap.put("billDate",entity.getBillDate());
        // TODO 单据状态
        //采购员
        resultMap.put("purchaseUserName",entity.getPurchaseUserName());

        //退货来源
        if (SourceTypeEnum.QC_BILL.getType().equals(entity.getSourceType())) {
            resultMap.put("sourceTypeName", ReturnOrderSourceEnum.QC.getCode());
        } else {
            resultMap.put("sourceTypeName", ReturnOrderSourceEnum.OTHER.getCode());
        }
        //退货原因
        resultMap.put("returnRemark",entity.getReturnRemark());
        //供应商
        resultMap.put("supplierName",entity.getSupplierName());
        //退货方式
        resultMap.put("returnMode", ReturnModeEnum.getName(entity.getReturnMode()));
        //供应商联系人
        resultMap.put("supplierContactName", entity.getSupplierContactName());
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(entity.getSupplierId());
        //供应商地址
        resultMap.put("address", supplierEntity.getCompanyAddress());

        //退货单明细
        List<PurchaseReturnOrderDetailEntity> detailList = purchaseReturnOrderDetailService.getDetailByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }

        //获取sku的id集合
        List<String> skuIdList = detailList.stream().map(PurchaseReturnOrderDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);

        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = detailList.stream().map(PurchaseReturnOrderDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);

        List<JSONObject> list = new ArrayList<>();
        for (PurchaseReturnOrderDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("skuNo", detail.getSkuNo());
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detail.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            jsonObject.set("productName", productDetailEntity.getName());
            jsonObject.set("returnQty", detail.getReturnQty());
            jsonObject.set("replenishQty", detail.getReplenishQty());
            jsonObject.set("deductAmountQty", detail.getDeductAmountQty());
            jsonObject.set("returnWarehouseName", entity.getReturnWarehouseName());
            jsonObject.set("remark", detail.getRemark());
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(req -> req.getId().equals(detail.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());
            jsonObject.set("PurchaseQty", purchaseOrderDetailEntity.getPurchaseQty());
            jsonObject.set("ReturnPrice", detail.getReturnPrice());

            list.add(jsonObject);
        }
        resultMap.put("list",list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

       /* //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_PURCHASE_APPLICATION_ORDER_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return purchaseReturnOrderService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"");
            }
            return Boolean.TRUE;
        });*/

    }
}
