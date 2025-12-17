package com.erp.server.scm.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
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
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.ScmPushMsgEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchasePriceService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceService;
import com.erp.server.scm.service.ScmPushMsgService;
import com.erp.server.scm.service.SupplierService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
public class SyncKingdeePurchasePriceServiceImpl implements SyncKingdeePurchasePriceService {

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

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
    public DmpPushTaskEntity syncDataToKingdee(PurchasePriceEntity entity, String operate) {
        //生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
    	}
    }

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public DmpPushTaskEntity syncDataDetailToKingdee(List<PurchasePriceDetailEntity> details, Boolean disabled) {
        if (CollectionUtils.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_98049);
        }
        List<String> purchasePriceIds = details.stream().map(PurchasePriceDetailEntity::getPurchasePriceId).distinct().collect(Collectors.toList());
        List<PurchasePriceEntity> list = purchasePriceService.listByIds(purchasePriceIds);
        if (CollectionUtils.isEmpty(list)) {
           throw new ServiceException(ApiError.ERROR_98024);
        }
        Map<String, Object> resultMap = new HashMap<>();

        JSONArray jsonArray = new JSONArray();

        for (PurchasePriceDetailEntity entity : details) {
            JSONObject jsonObject = new JSONObject();
            String syncKingdeeId = list.stream().filter(obj -> obj.getId().equals(entity.getPurchasePriceId())).map(PurchasePriceEntity::getSyncKingdeeId).findFirst().orElse(null);
            if (StringUtils.isBlank(syncKingdeeId)) {
                continue;
            }
            jsonObject.set("syncKingdeeId", syncKingdeeId);
            jsonObject.set("kingdeeDetailId", entity.getKingdeeDetailId());
            jsonObject.set("skuNo", entity.getSkuNo());
            jsonObject.set("minQty", entity.getMinQty());
            jsonObject.set("maxQty", entity.getMaxQty());
            jsonArray.put(jsonObject);
        }
        String operate;
        if (disabled) {
            operate = SyncOperateEnum.OPERATE_SUB_UN_EFFECTIVE.getCode();
        } else {
            operate = SyncOperateEnum.OPERATE_SUB_EFFECTIVE.getCode();
        }

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        resultMap.put("list", jsonArray);

        //主表id
        String priceIds = list.stream().map(PurchasePriceEntity::getId).collect(Collectors.joining(","));
        //主表code
        String priceCodes = list.stream().map(PurchasePriceEntity::getCode).collect(Collectors.joining(","));
        
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> cfgSettingEntityList = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.PURCHASE_PRICE.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(cfgSettingEntityList)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(priceIds);
            dmpSyncTaskDTO.setSourceCode(priceCodes);
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.PURCHASE_PRICE.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_PURCHASE_PRICE_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        for(PurchasePriceEntity entity : list) {
        	ScmPushMsgEntity scmPushMsgEntity = new ScmPushMsgEntity();
            scmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
            scmPushMsgEntity.setSourceType(SourceTypeEnum.PURCHASE_PRICE.getCode());
            scmPushMsgEntity.setSourceId(entity.getId());
            scmPushMsgEntity.setSourceCode(entity.getCode());
            scmPushMsgEntity.setSyncOperate(operate);
            scmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
            
            scmPushMsgService.save(scmPushMsgEntity);
        }
        
        return null;
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (PurchasePriceEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.PURCHASE_PRICE.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.PURCHASE_PRICE.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_PURCHASE_PRICE_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
           return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
       
       ScmPushMsgEntity scmPushMsgEntity = new ScmPushMsgEntity();
       scmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
       scmPushMsgEntity.setSourceType(SourceTypeEnum.PURCHASE_PRICE.getCode());
       scmPushMsgEntity.setSourceId(entity.getId());
       scmPushMsgEntity.setSourceCode(entity.getCode());
       scmPushMsgEntity.setSyncOperate(operate);
       scmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
       
       scmPushMsgService.save(scmPushMsgEntity);
       
       return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(PurchasePriceEntity entity, String operate) {
		Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id", entity.getId());
        //编码
        resultMap.put("code", entity.getCode());
        //名称
        resultMap.put("name", entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        //查询供应商
        SupplierEntity supplierEntity = supplierService.getById(entity.getSupplierId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
           throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //供应商编码
        resultMap.put("supplierCode", supplierEntity.getCode());
        //采购组织id
        String purchaseOrgId = entity.getPurchaseOrgId();
        //组织
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(purchaseOrgId));

        String purchaseOrgCode = "";
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            purchaseOrgCode = accountingCompanyList.get(0).getCode();
        }
        //采购组织
        resultMap.put("purchaseOrgCode", purchaseOrgCode);

        if (StringUtils.isNotBlank(entity.getPricingUserId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getPricingUserId());

            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                //定价员
                resultMap.put("pricingUserCode", findUserDTO.getCode());
            }
        }

        //获取币别信息
        List<CurrencyDTO.ViewDTO> currencyListt = sysUserFeign.listByCurrency(Arrays.asList(entity.getCurrency()));
        CurrencyDTO.ViewDTO currencyDTO = currencyListt.stream().filter(req -> req.getId().equals(entity.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
        resultMap.put("currencyCode", currencyDTO.getKingdeeCode());

        //价目明细
        List<PurchasePriceDetailDTO.ViewDTO> details = purchasePriceDetailService.getByPurchasePriceId(entity.getId());
        if (CollectionUtils.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_98049);
        }
        List<JSONObject> list = new ArrayList<>();
        for (PurchasePriceDetailDTO.ViewDTO detailEntity : details) {
            BigDecimal rate = MathUtil.divide(detailEntity.getTaxRate(), MathUtil.BigDecimal_100);
            JSONObject jsonObject = new JSONObject();
            //金蝶id
            resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
            resultMap.put("kingdeeDetailId", detailEntity.getKingdeeDetailId());
            jsonObject.set("detailId", detailEntity.getId());
            jsonObject.set("skuNo", detailEntity.getSkuNo());
            jsonObject.set("taxRate", detailEntity.getTaxRate());
            jsonObject.set("price", MathUtil.divide(detailEntity.getTaxPrice(), MathUtil.add(MathUtil.BigDecimal_1, rate)));
            jsonObject.set("taxPrice", detailEntity.getTaxPrice());
            jsonObject.set("minQty", detailEntity.getMinQty());
            jsonObject.set("maxQty", detailEntity.getMaxQty());
            jsonObject.set("effectiveDate", LocalDateTimeUtil.format(detailEntity.getEffectiveDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            jsonObject.set("expireDate",LocalDateTimeUtil.format(detailEntity.getExpireDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            jsonObject.set("disabled", detailEntity.getDisabled());
            list.add(jsonObject);
        }
        resultMap.put("list", list);
        return resultMap;
	}
}
