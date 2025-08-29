package com.erp.server.scm.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
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
import com.erp.model.scm.entity.*;
import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.scm.kingdee.SyncKingdeePurchaseChangeService;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private PurchaseChangeDetailService purchaseChangeDetailService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

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


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(PurchaseChangeEntity entity, String operate) {
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
    private DmpPushTaskEntity saveTask (PurchaseChangeEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.PURCHASE_CHANGE.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.PURCHASE_CHANGE.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_PURCHASE_CHANGE_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            taskFeignDTO.setParentId(entity.getPurchaseOrderId());
            return dmpMqFeign.saveTask(taskFeignDTO);
        }
        
        ScmPushMsgEntity scmPushMsgEntity = new ScmPushMsgEntity();
        scmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        scmPushMsgEntity.setSourceType(SourceTypeEnum.PURCHASE_CHANGE.getCode());
        scmPushMsgEntity.setSourceId(entity.getId());
        scmPushMsgEntity.setSourceCode(entity.getCode());
        scmPushMsgEntity.setSyncOperate(operate);
        scmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        scmPushMsgEntity.setParentId(entity.getPurchaseOrderId());
        
        scmPushMsgService.save(scmPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(PurchaseChangeEntity entity, String operate) {
		//采购订单未同步成功则无需推送采购变更
        PurchaseOrderEntity purchaseOrderEntity = purchaseOrderService.getById(entity.getPurchaseOrderId());
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
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
        resultMap.put("changeDate", LocalDateTimeUtil.format(entity.getChangeDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        //采购日期
        resultMap.put("purchaseDate",LocalDateTimeUtil.format(purchaseOrderEntity.getPurchaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
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

        //获取用户部门id
        if (StringUtils.isNotBlank(purchaseOrderEntity.getPurchaseDeptId())) {
            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
            dto.setDeptId(purchaseOrderEntity.getPurchaseDeptId());
            dto.setOrgId(entity.getPurchaseOrgId());
            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
            if (ObjectUtils.isNotEmpty(deptKingdee)) {
                resultMap.put("purchaseDeptCode", deptKingdee.getKingdeeDeptCode());
            }
        }
        //采购员编码
        if (StringUtils.isNotBlank(purchaseOrderEntity.getPurchaseUserId())) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgCode(purchaseOrderEntity.getPurchaseOrgId());
            findBusinessOperator.setUserId(purchaseOrderEntity.getPurchaseUserId());
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.CGY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //采购员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("purchaseUserCode", kingSellerInfo.getUserPostCode());
                resultMap.put("purchaseUserName", kingSellerInfo.getUserName());
            }
        }

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

        //交货仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(entity.getDeliveryWarehouseId()));

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
            //新品首批
            jsonObject.set("firstMassProduct", detailEntity.getFirstMassProduct());
            //来源单号
            jsonObject.set("sourceCode",purchaseOrderEntity.getCode());
            //采购明细
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseOrderDetailEntity)) {
                log.error("未找到采购订单明细，purchaseDetailIdList = {}",detailEntity.getPurchaseOrderDetailId());
                throw new ServiceException(ApiError.ERROR_98026);
            }
            //仓库编码
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String kingdeeWarehouseCode = warehouseList.get(0).getKingdeeWarehouseCode();
                jsonObject.set("kingdeeWarehouseCode",kingdeeWarehouseCode);
            }
            //是否赠品
            jsonObject.set("isGift",purchaseOrderDetailEntity.getIsGift());

            //税率
            jsonObject.set("taxRate", MathUtil.multiplyWithTwo(purchaseOrderDetailEntity.getTaxRate(),MathUtil.BigDecimal_100));
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
        return resultMap;
	}
}
