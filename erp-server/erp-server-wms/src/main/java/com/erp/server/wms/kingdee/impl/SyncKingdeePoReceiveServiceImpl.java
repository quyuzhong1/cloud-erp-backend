package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.*;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.kingdee.SyncKingdeePoReceiveService;
import com.erp.server.wms.service.WarehouseReceiveDetailService;
import com.erp.server.wms.service.WarehouseReceiveService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.WmsPushMsgService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采购收货单
 *
 * @Author Luo_WG
 * @Date 2023/10/10 14:26
 **/
@Slf4j
@Service
public class SyncKingdeePoReceiveServiceImpl implements SyncKingdeePoReceiveService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;
    
    @Resource
    private WmsPushMsgService wmsPushMsgService;

    /**
     * 发送消息同步金蝶
     *
     * @param entity
     * @param operate
     * @return void
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     **/
    @Override
    public DmpPushTaskEntity syncDataToKingdee(WarehouseReceiveEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        PurchaseOrderEntity purchaseOrderEntity = new PurchaseOrderEntity();


        //更新同步状态为待同步
//        warehouseReceiveService.updateSyncKingdeeStatus(entity.getId(), SyncStatusEnum.TO_BE_SYNC.getCode(), "", operate);
        //如果上游单据未发送成功则无需发送
        if (StringUtils.isNotBlank(entity.getPurchaseOrderId())) {
            //采购订单
            purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(entity.getPurchaseOrderId());
           /* DmpPushTaskEntity purchaseOrderTask = dmpMqFeign.getByParam(new DmpSyncTaskDTO.OneDTO(SourceTypeEnum.PURCHASE_ORDER.getCode(), purchaseOrderEntity.getId(), PlatformEnum.KINGDEE.getDesc(), PlatformEnum.ERP.getDesc()));

            if (!SyncStatusEnum.SUCCESS_SYNC.getCode().equals(purchaseOrderTask.getStatus()) && !SyncStatusEnum.NO_NEED_SYNC.getCode().equals(purchaseOrderTask.getStatus())) {
                log.error("采购订单未推送成功，不支持推送采购入库单，采购订单号【{}】", purchaseOrderEntity.getCode());
                return;
            }*/
        }
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //收货单号
        resultMap.put("code", entity.getCode());

        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return saveTask(entity,operate,resultMap);
        }

        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getReceiveOrgId(), purchaseOrderEntity.getPurchaseOrgId()));
        String receiveOrgCode = accountingCompanyList.stream().filter(req -> req.getId().equals(entity.getReceiveOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");
        //收货组织
        resultMap.put("receiveOrgCode", receiveOrgCode);
        PurchaseOrderEntity finalPurchaseOrderEntity = purchaseOrderEntity;
        String purchaseOrgCode = accountingCompanyList.stream().filter(req -> req.getId().equals(finalPurchaseOrderEntity.getPurchaseOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");
        //采购组织
        resultMap.put("purchaseOrgCode", purchaseOrgCode);
        //收货日期
        resultMap.put("billDate", LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        //查询供应商信息
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(entity.getSupplierId());
        //供应商编码
        resultMap.put("supplierCode", supplierEntity.getCode());
        //收料员
        String receiveUserId = entity.getReceiveUserId();
        if (StringUtils.isNotBlank(receiveUserId)) {
            //获取用户部门id
            SysDepartmentUserNumberDTO departmentDTO = sysUserFeign.getDeptByUserId(receiveUserId);
            //收料部门
            if (ObjectUtil.isNotEmpty(departmentDTO)) {
                resultMap.put("receiveDept", departmentDTO.getCode());
            } else {
                resultMap.put("receiveDept", "");
            }
            //收料员
            FindUserDTO userByUserId = sysUserFeign.getUserByUserId(receiveUserId);
            if (ObjectUtil.isNotEmpty(userByUserId)) {
                resultMap.put("receiveUser", userByUserId.getCode());
            } else {
                resultMap.put("receiveUser", "");
            }

        }


        //采购员
        String purchaseUserId = entity.getPurchaseUserId();
        if (StringUtils.isNotBlank(purchaseUserId)) {
            //获取用户部门id
            SysDepartmentUserNumberDTO departmentDTO = sysUserFeign.getDeptByUserId(purchaseUserId);
            //采购部门
            if (ObjectUtil.isEmpty(departmentDTO)) {
                resultMap.put("purchaseDept", departmentDTO.getCode());
            } else {
                resultMap.put("purchaseDept", "");
            }
        }

/*        //收料员
        String receiveUserId = entity.getReceiveUserId();
        if (StringUtils.isNotBlank(receiveUserId)) {
            //获取用户部门id
            SysDepartmentUserNumberDTO departmentDTO = sysUserFeign.getDeptByUserId(receiveUserId);
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
                resultMap.put("receiveUserId", kingSellerInfo.getKingdeePostCode());
                resultMap.put("receiveUserId", kingSellerInfo.getKingdeeUserName());
            }
        }*/

        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = scmTaskFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());

        //供应商联系人
        resultMap.put("supplierContactName", purchaseOrderSupplierEntity.getContactName());
        //供应商地址
        resultMap.put("address", supplierEntity.getCompanyAddress());


        //退货原因
        resultMap.put("purchaseOrderCode", entity.getPurchaseOrderCode());

        String billDate = purchaseOrderEntity.getPurchaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String currency = StringUtils.isNotBlank(purchaseOrderSupplierEntity.getPayCurrency()) ? purchaseOrderSupplierEntity.getPayCurrency() : "CNY";
        //汇率
        BigDecimal exchangeRate = dmpTaskFeign.getRate(billDate, currency);
        if (Objects.isNull(exchangeRate)) {
            exchangeRate = MathUtil.BigDecimal_1;
        }
        //汇率
        resultMap.put("exchangeRate", exchangeRate);
        //获取币别信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(purchaseOrderSupplierEntity.getPayCurrency()));

        //结算币别
        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(purchaseOrderSupplierEntity.getPayCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
        resultMap.put("currencyCode", viewDTO.getKingdeeCode());

        //收货单明细
        List<WarehouseReceiveDetailEntity> detailList = warehouseReceiveDetailService.getDetailByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException("未找到收货单明细");
        }

        //获取sku的id集合
        List<String> skuIdList = detailList.stream().map(WarehouseReceiveDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);


        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = detailList.stream().map(WarehouseReceiveDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);

        List<String> subcontractDetailIds = purchaseOrderDetailEntities.stream().map(req -> req.getSourceDetailId()).distinct().collect(Collectors.toList());
        List<SubcontractOrderDetailEntity> subcontractOrderDetailEntities = scmTaskFeign.listSubcontractDetailByIds(subcontractDetailIds);

        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(entity.getDeliveryWarehouseId());

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(Arrays.asList(entity.getDeliveryWarehouseId()));

        List<JSONObject> list = new ArrayList<>();
        for (WarehouseReceiveDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("detailId",detail.getId());
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detail.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            //产品名称
            jsonObject.set("productName", productDetailEntity.getName());
            //交货数量
            jsonObject.set("receiveQty", detail.getReceiveQty());
            //计划交货时间
            jsonObject.set("planDeliveryDate", LocalDateTimeUtil.format(detail.getPlanDeliveryDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            //仓库编码
            jsonObject.set("warehouseCode", warehouseEntity.getKingdeeWarehouseCode());

            //退货备注
            jsonObject.set("remark", detail.getRemark());
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(req -> req.getId().equals(detail.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());

            SubcontractOrderDetailEntity subcontractOrderDetailEntity = subcontractOrderDetailEntities.stream().filter(req -> req.getId().equals(purchaseOrderDetailEntity.getSourceDetailId())).findFirst().orElse(new SubcontractOrderDetailEntity());

            //是否下推仓位
            Boolean isPush = pushKingdeeList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), entity.getDeliveryWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPush) {
                //仓位
                jsonObject.set("warehouseLocation", subcontractOrderDetailEntity.getWarehouseLocation());
            }
            //采购单号
            jsonObject.set("purchaseOrderCode", entity.getPurchaseOrderCode());
            if (StringUtils.isNotBlank(entity.getPurchaseOrderCode())) {
                List<Map<String, Object>> mapList = new ArrayList<>();
                Map<String, Object> entityMap = new HashMap<>();
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
        resultMap.put("list", list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);


        //生成任务
        return saveTask(entity,operate,resultMap);
    }


    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (WarehouseReceiveEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.PO_RECEIVE.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.PO_RECEIVE.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_PO_RECEIVE_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            dmpSyncTaskDTO.setParentId(entity.getPurchaseOrderId());
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.PO_RECEIVE.getCode());
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        wmsPushMsgEntity.setParentId(entity.getPurchaseOrderId());
        
        wmsPushMsgService.save(wmsPushMsgEntity);
        
        return null;
    }
}
