package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PoInstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeStockInService;
import com.erp.server.wms.service.PoInstockDetailService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.WmsPushMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 同步金蝶采购入库单单
 *
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
    private PoInstockDetailService poInstockDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;
    
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(PoInstockEntity entity, String operate) {
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
    private DmpPushTaskEntity saveTask (PoInstockEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.PO_INSTOCK.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.PO_INSTOCK.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_PURCHASE_STOCK_IN_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            dmpSyncTaskDTO.setParentId(entity.getPurchaseOrderId());
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.PO_INSTOCK.getCode());
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        wmsPushMsgEntity.setParentId(entity.getPurchaseOrderId());
        
        wmsPushMsgService.save(wmsPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(PoInstockEntity entity, String operate) {
		//如果上游单据未发送成功则无需发送
        if (CharSequenceUtil.isNotBlank(entity.getPurchaseOrderId())) {
            //采购订单
           /* PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(entity.getPurchaseOrderId());
            DmpPushTaskEntity purchaseOrderTask = dmpMqFeign.getByParam(new DmpSyncTaskDTO.OneDTO(SourceTypeEnum.PURCHASE_ORDER.getCode(), purchaseOrderEntity.getId(), PlatformEnum.KINGDEE.getDesc(), PlatformEnum.ERP.getDesc()));
            if (!SyncStatusEnum.SUCCESS_SYNC.getCode().equals(purchaseOrderTask.getStatus()) && !SyncStatusEnum.NO_NEED_SYNC.getCode().equals(purchaseOrderTask.getStatus())) {
                log.error("采购订单未推送成功，不支持推送采购入库单，采购订单号【{}】", purchaseOrderEntity.getCode());
                return;
            }*/
        }
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //入库单号
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }

        PurchaseOrderEntity purchaseOrderEntity = scmTaskFeign.getPurchaseOrderById(entity.getPurchaseOrderId());

        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getReceiveOrgId(), purchaseOrderEntity.getPurchaseOrgId()));
        //收货组织
        String receiveOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId())).distinct()
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");

        //采购组织
        String purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(purchaseOrderEntity.getPurchaseOrgId())).distinct()
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");


        //入库组织
        resultMap.put("receiveOrgName", entity.getReceiveOrgName());
        //单据类型
        resultMap.put("billType",purchaseOrderEntity.getType());


        //获取用户部门id
        if (CharSequenceUtil.isNotBlank(entity.getPurchaseDeptId())) {
            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
            dto.setDeptId(entity.getPurchaseDeptId());
            dto.setOrgId(purchaseOrderEntity.getPurchaseOrgId());
            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
            if (ObjectUtils.isNotEmpty(deptKingdee)) {
                resultMap.put("purchaseDeptCode", deptKingdee.getKingdeeDeptCode());
            }
        }
        //入库日期
        resultMap.put("billDate", LocalDateTimeUtil.format(entity.getStockInDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //采购员
        String purchaseUserId = entity.getPurchaseUserId();
        if (CharSequenceUtil.isNotBlank(entity.getPurchaseUserId())) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgCode(purchaseOrgCode);
            findBusinessOperator.setUserId(purchaseUserId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.CGY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("purchaseUserCode", kingSellerInfo.getUserPostCode());
                resultMap.put("purchaseUserName", kingSellerInfo.getUserName());
            }
        }

        resultMap.put("receiveOrgCode", receiveOrgCode);

        resultMap.put("purchaseOrgCode", purchaseOrgCode);

        //退货组织
        resultMap.put("supplierCode", entity.getReceiveOrgId());
        //查询供应商信息
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(entity.getSupplierId());
        //供应商编码
        resultMap.put("supplierCode", supplierEntity.getCode());
        //供应商
        resultMap.put("supplierName", entity.getSupplierName());
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = scmTaskFeign.getOrderSupplierByOrderId(entity.getPurchaseOrderId());

        //供应商联系人
        resultMap.put("supplierContactName", purchaseOrderSupplierEntity.getContactName());

        //供应商地址
        resultMap.put("address", supplierEntity.getCompanyAddress());

        //入库单明细
        List<PoInstockDetailEntity> detailList = poInstockDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98051);
        }

        //获取sku的id集合
        List<String> skuIdList = detailList.stream().map(PoInstockDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);

        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = detailList.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(entity.getDeliveryWarehouseId());

        List<String> soKingdeeDetailIdList = purchaseOrderDetailEntities.stream().map(req -> req.getKingdeeDetailId()).collect(Collectors.toList());
        resultMap.put("poKingdeeDetailIds", String.join(",", soKingdeeDetailIdList));
        resultMap.put("poSyncKingdeeId", purchaseOrderEntity.getSyncKingdeeId());
        List<JSONObject> list = new ArrayList<>();

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(Collections.singletonList(entity.getDeliveryWarehouseId()));

        for (PoInstockDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detail.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            //产品名称
            jsonObject.set("productName", productDetailEntity.getName());
            //实收数量
            jsonObject.set("stockInQty", detail.getStockInQty());
            //供应商编码
            jsonObject.set("supplierCode", supplierEntity.getCode());
            //供应商编码
            jsonObject.set("supplierName", supplierEntity.getName());
            jsonObject.set("firstMassProduct", detail.getFirstMassProduct());
            //交货仓库
            jsonObject.set("deliveryWarehouseName", entity.getDeliveryWarehouseName());
            if (ObjectUtil.isNotEmpty(warehouseEntity)) {
                //交货仓库
                jsonObject.set("deliveryWarehouseCode", warehouseEntity.getKingdeeWarehouseCode());
            }
            //是否下推仓位
            Boolean isPush = pushKingdeeList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), entity.getDeliveryWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPush) {
                //库位
                jsonObject.set("warehouseLocation", detail.getWarehouseLocation());
            }

            //入库备注
            jsonObject.set("remark", detail.getRemark());
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(req -> req.getId().equals(detail.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());
            //采购数量
            jsonObject.set("purchaseQty", purchaseOrderDetailEntity.getPurchaseQty());

            //赠品
            jsonObject.set("isGift", purchaseOrderDetailEntity.getIsGift());
            //计价数量
            jsonObject.set("priceBaseQty", detail.getStockInQty());
            //含税单价
            jsonObject.set("taxPrice", purchaseOrderDetailEntity.getTaxPrice());
            //税率
            jsonObject.set("taxRate", MathUtil.multiplyWithTwo(purchaseOrderDetailEntity.getTaxRate(),MathUtil.BigDecimal_100));
            //采购编号
            jsonObject.set("purchaseOrderCode", entity.getPurchaseOrderCode());
            //明细id
            jsonObject.set("detailId", detail.getId());
            //销售订单金蝶id
            jsonObject.set("poSyncKingdeeId", purchaseOrderEntity.getSyncKingdeeId());

            //销售单金蝶明细id
            jsonObject.set("poKingdeeDetailId", purchaseOrderDetailEntity.getKingdeeDetailId());

            List<Map<String, Object>> mapList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("poKingdeeDetailId", purchaseOrderDetailEntity.getKingdeeDetailId());
            map.put("poSyncKingdeeId", purchaseOrderEntity.getSyncKingdeeId());
            map.put("FInStockEntry_Link_FSTableName", "t_PUR_POOrderEntry");
            map.put("FInStockEntry_Link_FRuleId", "PUR_PurchaseOrder-STK_InStock");
            mapList.add(map);
            //销售单金蝶明细id
            jsonObject.set("FInStockEntry_Link", mapList);

            list.add(jsonObject);
        }
        resultMap.put("list", list);
        return resultMap;
	}
}
