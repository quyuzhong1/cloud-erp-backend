package com.erp.server.scm.kingdee.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
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

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    private DictBasicService dictBasicService;


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
    public void syncDataToKingdee(PurchaseOrderEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //采购日期
        resultMap.put("purchaseDate",entity.getPurchaseDate());

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
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(entity.getPurchaseUserId(),entity.getCreateUserId()));

            if (CollectionUtils.isNotEmpty(userList)) {
                //采购员
                String purchaseUserCode = userList.stream().filter(obj -> obj.getUserId().equals(entity.getPurchaseUserId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
                resultMap.put("purchaseUserCode", purchaseUserCode);
                //创建人
                String createUserCode = userList.stream().filter(obj -> obj.getUserId().equals(entity.getCreateUserId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
                resultMap.put("createUserCode", createUserCode);
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
