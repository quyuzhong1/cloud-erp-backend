package com.erp.server.scm.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SubcontractTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.scm.entity.AssetPurchaseOrderEntity;
import com.erp.model.scm.entity.*;
import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.scm.kingdee.SyncKingdeePurchaseOrderService;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeePurchaseOrderServiceImpl implements SyncKingdeePurchaseOrderService {

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private AssetPurchaseOrderSupplierService assetPurchaseOrderSupplierService;

    @Resource
    private AssetPurchaseOrderDetailService assetPurchaseOrderDetailService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;
    
    @Resource
    private ScmPushMsgService scmPushMsgService;

    @Resource
    private KingdeeFeign kingdeeFeign;


    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(PurchaseOrderEntity entity, String operate) {
        //生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
    	}
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (PurchaseOrderEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.PURCHASE_ORDER.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.PURCHASE_ORDER.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_PURCHASE_ORDER_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(entity.getSourceType()) && SubcontractTypeEnum.ENUM_PARENT.getCode().equals(entity.getSubcontractType())) {
                taskFeignDTO.setParentId(entity.getSourceId());
            }
            if (SourceTypeEnum.PO_RETURN.getCode().equals(entity.getSourceType())) {
                taskFeignDTO.setParentId(entity.getSourceId());
            }
            return dmpMqFeign.saveTask(taskFeignDTO);
        }
        
        ScmPushMsgEntity scmPushMsgEntity = new ScmPushMsgEntity();
        scmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        scmPushMsgEntity.setSourceType(SourceTypeEnum.PURCHASE_ORDER.getCode());
        scmPushMsgEntity.setSourceId(entity.getId());
        scmPushMsgEntity.setSourceCode(entity.getCode());
        scmPushMsgEntity.setSyncOperate(operate);
        scmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(entity.getSourceType()) && SubcontractTypeEnum.ENUM_PARENT.getCode().equals(entity.getSubcontractType())) {
        	scmPushMsgEntity.setParentId(entity.getSourceId());
        }
        if (SourceTypeEnum.PO_RETURN.getCode().equals(entity.getSourceType())) {
        	scmPushMsgEntity.setParentId(entity.getSourceId());
        }
        
        scmPushMsgService.save(scmPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(PurchaseOrderEntity entity, String operate) {
		Map<String, Object> resultMap = new HashMap<>();

        //如果上游单据未发送成功则无需发送
        SubcontractOrderEntity subcontractOrderEntity = null;
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = null;
        if (StringUtils.isNotBlank(entity.getSubcontractType())) {
            //委外订单
            subcontractOrderEntity = subcontractOrderService.getById(entity.getSourceId());
            if (ObjectUtils.isEmpty(subcontractOrderEntity)) {
                throw new ServiceException(ApiError.ERROR_98073);
            }
            //委外订单明细
            subcontractOrderDetailList = subcontractOrderDetailService.listByMainId(subcontractOrderEntity.getId());
        }

        PoReturnEntity poReturnEntity = null;
        List<PoReturnDetailEntity> poReturnDetailEntityList = null;
        if (SourceTypeEnum.PO_RETURN.getCode().equals(entity.getSourceType())) {
            List<PoReturnEntity> poReturnEntityList = wmsTaskFeign.listPoReturnByIdList(Arrays.asList(entity.getSourceId()));
            poReturnEntity = poReturnEntityList.stream().findFirst().orElse(null);
            if (ObjectUtils.isEmpty(poReturnEntity)) {
                throw new ServiceException(ApiError.PO_RETURN_NOT_EXISTS);
            }
            poReturnDetailEntityList = wmsTaskFeign.listPurchaseReturnOrderDetailByMainIds(Arrays.asList(poReturnEntity.getId()));
        }

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        //采购日期
        resultMap.put("purchaseDate", LocalDateTimeUtil.format(entity.getPurchaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //单据类型
        resultMap.put("type",entity.getType());

        //查询采购供应商
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(entity.getId());
        if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
           throw new ServiceException("未发现采购供应商信息");
        }
        SupplierEntity supplierEntity = supplierService.getById(purchaseOrderSupplierEntity.getSupplierId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
            throw new ServiceException("未发现供应商信息");
        }

        //供应商编码
        resultMap.put("supplierCode",supplierEntity.getCode());

        //采购部门
        resultMap.put("purchaseDeptName",entity.getPurchaseDeptName());

        //获取用户部门id
        if (StringUtils.isNotBlank(entity.getPurchaseDeptId())) {
            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
            dto.setDeptId(entity.getPurchaseDeptId());
            dto.setOrgId(entity.getPurchaseOrgId());
            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
            if (ObjectUtils.isNotEmpty(deptKingdee)) {
                resultMap.put("purchaseDeptCode", deptKingdee.getKingdeeDeptCode());
            }
        }

        //采购员编码
        if (StringUtils.isNotBlank(entity.getPurchaseUserId())) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgCode(entity.getPurchaseOrgId());
            findBusinessOperator.setUserId(entity.getPurchaseUserId());
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.CGY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //采购员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("purchaseUserCode", kingSellerInfo.getUserPostCode());
                resultMap.put("purchaseUserName", kingSellerInfo.getUserName());
            }
        }
        //供应商联系人
        resultMap.put("contactName",purchaseOrderSupplierEntity.getContactName());

        if (ObjectUtils.isNotEmpty(purchaseOrderSupplierEntity.getPaymentCondition())) {
            //付款条件
            resultMap.put("paymentCondition",purchaseOrderSupplierEntity.getPaymentCondition());
        }

        //采购明细
        List<PurchaseOrderDetailEntity> details = purchaseOrderDetailService.listByPurchaseOrderId(entity.getId());
        if (CollectionUtils.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_98026);
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
            jsonObject.set("planDeliveryDate",LocalDateTimeUtil.format(detailEntity.getPlanDeliveryDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
            jsonObject.set("price", MathUtil.divide(detailEntity.getTaxPrice(),MathUtil.add(MathUtil.BigDecimal_1,detailEntity.getTaxRate())) );
            jsonObject.set("taxPrice",detailEntity.getTaxPrice());
            //新品首批
            jsonObject.set("firstMassProduct", detailEntity.getFirstMassProduct());
            //部门编码
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String kingdeeWarehouseCode = warehouseList.get(0).getKingdeeWarehouseCode();
                jsonObject.set("kingdeeWarehouseCode",kingdeeWarehouseCode);
            }
            jsonObject.set("taxRate",MathUtil.multiplyWithTwo(detailEntity.getTaxRate(),MathUtil.BigDecimal_100));
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                //收料组织编码
                String receiveOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
                jsonObject.set("receiveOrgCode", receiveOrgCode);

                //采购组织编码
                String purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
                jsonObject.set("purchaseOrgCode", purchaseOrgCode);
            }
            jsonObject.set("isGift",detailEntity.getIsGift());
            jsonObject.set("detailRemark",detailEntity.getRemark());
            //来源单据类型类型(只有委外父级SKU生成的采购订单需要设置委外采购)
            if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(entity.getSourceType()) && SubcontractTypeEnum.ENUM_PARENT.getCode().equals(entity.getSubcontractType())) {
                jsonObject.set("detailSourceType", KingdeePushModuleEnum.SUB_SUBREQORDER.getCode());
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
                        String subDetailKingdeeId = subcontractOrderDetailList.stream()
                                .filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeDetailId()))
                                .orElse("");
                        refJsonObject.set("refDetailKingdeeId",subDetailKingdeeId);
                    }
                    refList.add(refJsonObject);
                    jsonObject.set("refList",refList);
                }
            }

            if (SourceTypeEnum.PO_RETURN.getCode().equals(entity.getSourceType())) {
                jsonObject.set("detailSourceType", KingdeePushModuleEnum.PUR_MRB.getCode());
                List<Map<String,Object>> refList = new ArrayList<>();
                JSONObject refJsonObject = new JSONObject();
                if (ObjectUtils.isNotEmpty(poReturnEntity)) {
                    jsonObject.set("refCode", poReturnEntity.getCode());
                    refJsonObject.set("refKingdeeId",poReturnEntity.getSyncKingdeeId());
                    String detailKingdeeId = poReturnDetailEntityList.stream()
                            .filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeDetailId()))
                            .orElse("");
                    if(StringUtils.isNotBlank(detailKingdeeId)){
                        refJsonObject.set("refDetailKingdeeId",detailKingdeeId);
                        refJsonObject.set("linkRule","PUR_MRB-PUR_PurchaseOrder");
                        refJsonObject.set("linkTable","T_PUR_MRBENTRY");
                        refList.add(refJsonObject);
                        jsonObject.set("refList",refList);
                    }
                }
            }
            list.add(jsonObject);
        }
        resultMap.put("list",list);
        return resultMap;
	}

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(AssetPurchaseOrderEntity entity, String operate) {
        //生成任务
        if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
        }else {
            return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
        }
    }

    /**
     * @description: 生成任务
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (AssetPurchaseOrderEntity entity, String operate, Map<String, Object> resultMap) {
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode())
                .eq(CfgSettingEntity::getType, settingEnum.getType())
                .eq(CfgSettingEntity::getValue, "1")
                .list();
        if(CollUtil.isEmpty(list)) {
            //添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_PURCHASE_ORDER_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }

        ScmPushMsgEntity scmPushMsgEntity = new ScmPushMsgEntity();
        scmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        scmPushMsgEntity.setSourceType(SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode());
        scmPushMsgEntity.setSourceId(entity.getId());
        scmPushMsgEntity.setSourceCode(entity.getCode());
        scmPushMsgEntity.setSyncOperate(operate);
        scmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        scmPushMsgService.save(scmPushMsgEntity);

        return null;
    }

    @Override
    public Map<String, Object> newSyncDataToKingdee(AssetPurchaseOrderEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        //采购日期
        resultMap.put("purchaseDate", LocalDateTimeUtil.format(entity.getPurchaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //单据类型
        //resultMap.put("type",entity.getOrderType());

        //查询采购供应商
        AssetPurchaseOrderSupplierEntity assetPurchaseOrderSupplierEntity = assetPurchaseOrderSupplierService.lambdaQuery()
                .eq(AssetPurchaseOrderSupplierEntity::getAssetPurchaseOrderId, entity.getId())
                .one();
        if (ObjectUtils.isEmpty(assetPurchaseOrderSupplierEntity)) {
            throw new ServiceException("未发现采购供应商信息");
        }
        SupplierEntity supplierEntity = supplierService.getById(assetPurchaseOrderSupplierEntity.getSupplierId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
            throw new ServiceException("未发现供应商信息");
        }

        //供应商编码
        resultMap.put("supplierCode",supplierEntity.getCode());

        //采购部门
        resultMap.put("purchaseDeptName",entity.getPurchaseDeptName());

        //获取用户部门id
        if (StringUtils.isNotBlank(entity.getPurchaseDeptId())) {
            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
            dto.setDeptId(entity.getPurchaseDeptId());
            dto.setOrgId(entity.getPurchaseOrgId());
            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
            if (ObjectUtils.isNotEmpty(deptKingdee)) {
                resultMap.put("purchaseDeptCode", deptKingdee.getKingdeeDeptCode());
            }
        }

        //采购员编码
        if (StringUtils.isNotBlank(entity.getPurchaseUserId())) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgCode(entity.getPurchaseOrgId());
            findBusinessOperator.setUserId(entity.getPurchaseUserId());
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.CGY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //采购员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("purchaseUserCode", kingSellerInfo.getUserPostCode());
                resultMap.put("purchaseUserName", kingSellerInfo.getUserName());
            }
        }
        //供应商联系人
        resultMap.put("contactName",assetPurchaseOrderSupplierEntity.getContactName());

        if (ObjectUtils.isNotEmpty(assetPurchaseOrderSupplierEntity.getPaymentCondition())) {
            //付款条件
            resultMap.put("paymentCondition",assetPurchaseOrderSupplierEntity.getPaymentCondition());
        }

        //采购明细
        List<AssetPurchaseOrderDetailEntity> details = assetPurchaseOrderDetailService.lambdaQuery().eq(AssetPurchaseOrderDetailEntity::getMainId, entity.getId()).list();
        if (CollectionUtils.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getPurchaseOrgId(),entity.getPurchaseOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //采购组织编码
            String purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("receiveOrgCode", purchaseOrgCode);
            resultMap.put("purchaseOrgCode", purchaseOrgCode);
        }


        List<JSONObject> list = new ArrayList<>();
        for (AssetPurchaseOrderDetailEntity detailEntity : details) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("detailId",detailEntity.getId());
            jsonObject.set("skuNo",detailEntity.getAssetCode());
            jsonObject.set("purchaseQty",detailEntity.getPurchaseQty());
            jsonObject.set("planDeliveryDate",LocalDateTimeUtil.format(detailEntity.getPlanDeliveryDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
            jsonObject.set("price", MathUtil.divide(detailEntity.getTaxPrice(),MathUtil.add(MathUtil.BigDecimal_1,detailEntity.getTaxRate())) );
            jsonObject.set("taxPrice",detailEntity.getTaxPrice());
            //新品首批
            //jsonObject.set("firstMassProduct", detailEntity.getFirstMassProduct());

            jsonObject.set("taxRate",MathUtil.multiplyWithTwo(detailEntity.getTaxRate(),MathUtil.BigDecimal_100));
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                //采购组织编码
                String purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
                jsonObject.set("receiveOrgCode", purchaseOrgCode);
                jsonObject.set("purchaseOrgCode", purchaseOrgCode);
            }
            jsonObject.set("isGift",Boolean.FALSE);
            jsonObject.set("tag",StringUtils.isNotBlank(detailEntity.getTag()) ? detailEntity.getTag() : null);
            jsonObject.set("detailRemark",detailEntity.getRemark());

            list.add(jsonObject);
        }
        resultMap.put("list",list);
        return resultMap;
    }
}
