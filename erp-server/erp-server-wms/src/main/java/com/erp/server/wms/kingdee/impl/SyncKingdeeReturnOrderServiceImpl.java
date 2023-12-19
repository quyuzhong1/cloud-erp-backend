package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.PurchaseReturnOrderEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.kingdee.SyncKingdeeReturnOrderService;
import com.erp.server.wms.service.PurchaseReturnOrderDetailService;
import com.erp.server.wms.service.WarehouseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private PurchaseReturnOrderDetailService purchaseReturnOrderDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    /**
     * 发送消息同步金蝶
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     * @param entity
     * @param operate
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void syncDataToKingdee(PurchaseReturnOrderEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        if (SourceTypeEnum.QC_INFO.getCode().equals(entity.getSourceType())) {
            resultMap.put("returnType", ReturnOrderSourceEnum.QC.getKingdeeCode());
            return;
        } else {
            resultMap.put("returnType", ReturnOrderSourceEnum.OTHER.getKingdeeCode());
        }

        PurchaseOrderEntity purchaseOrderEntity = new PurchaseOrderEntity();

        //如果上游单据未发送成功则无需发送
        if (StringUtils.isNotBlank(entity.getPurchaseOrderId())) {
            //采购订单
            purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(entity.getPurchaseOrderId());
            /*DmpPushTaskEntity purchaseOrderTask = dmpMqFeign.getByParam(new DmpSyncTaskDTO.OneDTO(SourceTypeEnum.PURCHASE_ORDER.getCode(), purchaseOrderEntity.getId(), PlatformEnum.KINGDEE.getDesc(), PlatformEnum.ERP.getDesc()));
            if (!SyncStatusEnum.SUCCESS_SYNC.getCode().equals(purchaseOrderTask.getStatus()) && !SyncStatusEnum.NO_NEED_SYNC.getCode().equals(purchaseOrderTask.getStatus())) {
                log.error("采购订单未推送成功，不支持推送采购入库单，采购订单号【{}】",purchaseOrderEntity.getCode());
                return;
            }*/
        }
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id",entity.getId());
        //退货单号
        resultMap.put("code",entity.getCode());

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            sendMqAndSaveTask(entity,operate,resultMap);
            return;
        }

        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getReturnOrgId()));
        String returnOrgCode = accountingCompanyList.stream().filter(req -> req.getId().equals(entity.getReturnOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");
        //退料组织
        resultMap.put("returnOrgName", returnOrgCode);

        //退货日期
        resultMap.put("billDate", LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) );

        //采购员
        String purchaseUserId = entity.getPurchaseUserId();
        if (StringUtils.isNotBlank(entity.getPurchaseUserId())) {
            //获取用户部门id
            SysDepartmentUserNumberDTO departmentDTO = sysUserFeign.getDeptByUserId(entity.getPurchaseUserId());
            //采购部门
            if (ObjectUtil.isEmpty(departmentDTO)) {
                resultMap.put("productDept", departmentDTO.getCode());
            } else {
                resultMap.put("productDept", "");
            }

            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgCode(returnOrgCode);
            findBusinessOperator.setUserId(purchaseUserId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.CGY.getCode());
            //获取员工业务信息
            KingdeeBusinessOperatorEntity kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("purchaseUserCode", kingSellerInfo.getKingdeePostCode());
                resultMap.put("purchaseUserName", kingSellerInfo.getKingdeeUserName());
            }
        }

        //退货来源
        if (SourceTypeEnum.QC_INFO.getCode().equals(entity.getSourceType())) {
            resultMap.put("sourceTypeName", ReturnOrderSourceEnum.QC.getCode());
        } else {
            resultMap.put("sourceTypeName", ReturnOrderSourceEnum.OTHER.getCode());
        }
        //退货原因
        resultMap.put("returnRemark",entity.getReturnRemark());

        //查询供应商信息
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(entity.getSupplierId());
        //供应商编码
        resultMap.put("supplierCode", supplierEntity.getCode());
        //供应商
        resultMap.put("supplierName",entity.getSupplierName());

        //退货方式
        if (entity.getReturnMode().equals(ReturnModeEnum.DEDUCTION.getCode())) {
            resultMap.put("returnMode", ReturnModeEnum.DEDUCTION.getKingdeeCode());
        } else {
            resultMap.put("returnMode", ReturnModeEnum.REPLENISHMENT.getKingdeeCode());
        }

        //供应商联系人
        resultMap.put("supplierContactName", entity.getSupplierContactName());
        //供应商地址
        resultMap.put("address", supplierEntity.getCompanyAddress());

        //退货原因
        resultMap.put("returnRemark", entity.getReturnRemark());

        //退货原因
        resultMap.put("purchaseOrderCode", entity.getPurchaseOrderCode());

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
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(entity.getReturnWarehouseId());
        List<JSONObject> list = new ArrayList<>();
        for (PurchaseReturnOrderDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detail.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            //产品名称
            jsonObject.set("productName", productDetailEntity.getName());
            //实退数量
            jsonObject.set("returnQty", detail.getReturnQty());
            //补货数量
            jsonObject.set("replenishQty", detail.getReplenishQty());
            //扣款数量
            jsonObject.set("deductAmountQty", detail.getDeductAmountQty());
            //退货仓库
            jsonObject.set("returnWarehouseName", entity.getReturnWarehouseName());
            //仓库编码
            jsonObject.set("returnWarehouseCode", warehouseEntity.getKingdeeWarehouseCode());
            //退货备注
            jsonObject.set("remark", detail.getRemark());
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(req -> req.getId().equals(detail.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());
            //采购数量
            jsonObject.set("purchaseQty", purchaseOrderDetailEntity.getPurchaseQty());
            //退款单价
            jsonObject.set("returnPrice", detail.getReturnPrice());
            //仓位
            jsonObject.set("warehouseLocation", detail.getWarehouseLocation());
            //采购单号
            jsonObject.set("purchaseOrderCode", entity.getPurchaseOrderCode());
            if (StringUtils.isNotBlank(entity.getPurchaseOrderCode())) {
                List<Map<String,Object>> mapList = new ArrayList<>();
                Map<String,Object> entityMap = new HashMap<>();
                entityMap.put("poKingdeeDetailId", purchaseOrderDetailEntity.getKingdeeDetailId());
                if (ObjectUtils.isNotEmpty(purchaseOrderEntity)) {
                    entityMap.put("poSyncKingdeeId", purchaseOrderEntity.getSyncKingdeeId());
                }
                mapList.add(entityMap);
                //销售单金蝶明细id
                jsonObject.set("FPURMRBENTRY_Link", mapList);
            }
            list.add(jsonObject);
        }
        resultMap.put("list",list);

        //生成任务
        sendMqAndSaveTask(entity,operate,resultMap);
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private void sendMqAndSaveTask (PurchaseReturnOrderEntity entity, String operate, Map<String, Object> resultMap) {
        //添加推送任务
        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(entity.getId());
        dmpSyncTaskDTO.setSourceCode(entity.getCode());
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.PO_RETURN.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_PURCHASE_RETURN_ORDER_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operate);
        dmpMqFeign.sendMqAndSaveTask(dmpSyncTaskDTO);
    }
}
