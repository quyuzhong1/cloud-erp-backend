package com.erp.server.wms.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.PurchaseReturnOrderEntity;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.model.wms.entity.PurchaseStockInEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.kingdee.SyncKingdeeReturnOrderService;
import com.erp.server.wms.kingdee.SyncKingdeeStockInService;
import com.erp.server.wms.service.PurchaseReturnOrderDetailService;
import com.erp.server.wms.service.PurchaseReturnOrderService;
import com.erp.server.wms.service.PurchaseStockInDetailService;
import com.erp.server.wms.service.PurchaseStockInService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 同步金蝶采购入库单单
 * @Author Luo_WG
 * @Date 2023/4/24 11:22
 **/
@Slf4j
@Service
public class SyncKingdeeStockInServiceImpl implements SyncKingdeeStockInService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PurchaseStockInService purchaseStockInService;

    @Resource
    private PurchaseStockInDetailService purchaseStockInDetailService;

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
    public void syncDataToKingdee(PurchaseStockInEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //入库单号
        resultMap.put("code", entity.getCode());
        //入库组织
        resultMap.put("receiveOrgName", entity.getReceiveOrgName());
        //获取用户部门id
        SysDepartmentUserNumberDTO departmentDTO = sysUserFeign.getDeptByUserId(entity.getPurchaseUserId());
        //获取用户部门信息
        SysDepartmentDTO department = sysUserFeign.getUserDeptById(departmentDTO.getDepartmentId());
        //采购部门
        resultMap.put("productDept", department.getName());

        //入库日期
        resultMap.put("billDate", entity.getStockInDate());
        // TODO 单据状态
        //采购员
        resultMap.put("purchaseUserName", entity.getPurchaseUserName());

        //新品首批
        resultMap.put("isFirstMassProduct", entity.getIsFirstMassProduct());

        //供应商
        resultMap.put("supplierName", entity.getSupplierName());
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = scmTaskFeign.getOrderSupplierByOrderId(entity.getPurchaseOrderId());
        //供应商联系人
        resultMap.put("supplierContactName", purchaseOrderSupplierEntity.getContactName());

        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(entity.getSupplierId());
        //供应商地址
        resultMap.put("address", supplierEntity.getCompanyAddress());

        //退货单明细
        List<PurchaseStockInDetailEntity> detailList = purchaseStockInDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }

        //获取sku的id集合
        List<String> skuIdList = detailList.stream().map(PurchaseStockInDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);

        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = detailList.stream().map(PurchaseStockInDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);

        List<JSONObject> list = new ArrayList<>();
        for (PurchaseStockInDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detail.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            //产品名称
            jsonObject.set("productName", productDetailEntity.getName());
            //实收数量
            jsonObject.set("returnQty", detail.getReceiveQty());

            //交货仓库
            jsonObject.set("deliveryWarehouseName", entity.getDeliveryWarehouseName());
            //库位
            jsonObject.set("deliveryWarehouseName", detail.getWarehouseLocationName());
            //入库备注
            jsonObject.set("remark", detail.getRemark());
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(req -> req.getId().equals(detail.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());
            //采购数量
            jsonObject.set("purchaseQty", purchaseOrderDetailEntity.getPurchaseQty());

            list.add(jsonObject);
        }
        resultMap.put("list", list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

/*        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_PURCHASE_STOCK_IN_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return purchaseStockInService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(), "");
            }
            return Boolean.TRUE;
        });*/
    }
}
