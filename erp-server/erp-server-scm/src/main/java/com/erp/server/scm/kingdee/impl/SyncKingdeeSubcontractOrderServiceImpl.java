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
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.scm.entity.ScmPushMsgEntity;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSubcontractOrderService;
import com.erp.server.scm.service.ScmPushMsgService;
import com.erp.server.scm.service.SubcontractOrderDetailService;
import com.erp.server.scm.service.SupplierService;
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

 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeeSubcontractOrderServiceImpl implements SyncKingdeeSubcontractOrderService {

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

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


    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(SubcontractOrderEntity entity, String operate) {
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
    private DmpPushTaskEntity saveTask (SubcontractOrderEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.SUBCONTRACT_ORDER.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_SUBCONTRACT_ORDER_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        ScmPushMsgEntity scmPushMsgEntity = new ScmPushMsgEntity();
        scmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        scmPushMsgEntity.setSourceType(SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
        scmPushMsgEntity.setSourceId(entity.getId());
        scmPushMsgEntity.setSourceCode(entity.getCode());
        scmPushMsgEntity.setSyncOperate(operate);
        scmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
        scmPushMsgService.save(scmPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(SubcontractOrderEntity entity, String operate) {
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
        resultMap.put("billDate", LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //采购员编码
        if (StringUtils.isNotBlank(entity.getPurchaserId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getPurchaserId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("purchaseUserCode", findUserDTO.getCode());
            }
        }


        //采购明细
        List<SubcontractOrderDetailEntity> details = subcontractOrderDetailService.listByMainId(entity.getId());
        //父级数据
        List<SubcontractOrderDetailEntity> parentList = details.stream().filter(obj -> StringUtils.isBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }

        //仓库信息
        List<String> warehouseIds = details.stream().map(SubcontractOrderDetailEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIds);

        //供应商信息
        List<String> supplierIds = details.stream().map(SubcontractOrderDetailEntity::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = supplierService.listByIds(supplierIds);

        //组织机构
        List<String> orgIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            List<String> orgIds = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
            orgIdList.addAll(orgIds);
        }
        orgIdList.add(entity.getSubcontractOrgId());
        orgIdList.add(entity.getPurchaseOrgId());
        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //委外组织编码
            String subcontractOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSubcontractOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("subcontractOrgCode", subcontractOrgCode);
        }

        List<JSONObject> list = new ArrayList<>();
        for (SubcontractOrderDetailEntity detailEntity : parentList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("detailId",detailEntity.getId());
            jsonObject.set("skuNo",detailEntity.getSkuNo());
            jsonObject.set("qty",detailEntity.getQty());
            jsonObject.set("planDeliveryDate",LocalDateTimeUtil.format(detailEntity.getPlanDeliveryDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            jsonObject.set("price",detailEntity.getPrice());
            //新品首批
            jsonObject.set("firstMassProduct", detailEntity.getFirstMassProduct());
            //单据日期
            jsonObject.set("billDate",LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            //仓库编码
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).findFirst().orElse(null);
                jsonObject.set("kingdeeWarehouseCode",updateDTO.getKingdeeWarehouseCode());
                //库存组织
                if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                    String inStockOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(updateDTO.getOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
                    jsonObject.set("inStockOrgCode",inStockOrgCode);
                }
            }
            //采购组织编码
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                String purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
                jsonObject.set("purchaseOrgCode", purchaseOrgCode);
            }
            //供应商编码
            if (CollectionUtils.isNotEmpty(supplierList)) {
                String supplierCode = supplierList.stream().filter(obj -> obj.getId().equals(detailEntity.getSupplierId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
                jsonObject.set("supplierCode",supplierCode);
            }
            String referenceVersion = detailEntity.getSkuNo() + "_" + detailEntity.getBomVersion();
            //参照版本
            jsonObject.set("referenceVersion", referenceVersion);
            jsonObject.set("detailRemark",detailEntity.getRemark());
            list.add(jsonObject);
        }
        resultMap.put("list",list);
        return resultMap;
	}
}
