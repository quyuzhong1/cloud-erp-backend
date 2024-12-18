package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.*;
import com.erp.server.oms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/10/30 11:51
 */
@Service
@Slf4j
public class SyncTaskServiceImpl implements SyncTaskService {

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private SyncKingdeeCustomerService syncKingdeeCustomerInfoService;

    @Resource
    private CustomerContactService customerContactService;

    @Resource
    private SyncKingdeeCustomerContactService syncKingdeeCustomerContactService;

    @Resource
    private CustomerGroupService customerGroupService;

    @Resource
    private SyncKingdeeCustomerGroupService syncKingdeeCustomerGroupService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SyncKingdeeSoService syncKingdeeSoService;

    @Resource
    private SoChangeService soChangeService;

    @Resource
    private SyncKingdeeSoChangeService syncKingdeeSoChangeService;

    @Resource
    private DmpMqFeign dmpMqFeign;
    
    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Resource
    private SyncSoB2cService syncSoB2cService;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ListingInfoService listingInfoService;

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SoChangeDetailService soChangeDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        switch (sourceType) {
            case CUSTOMER_INFO:
                resultList = syncCustomerInfo(sourceDetailList);
                break;
            case CUSTOMER_CONTACT:
                resultList = syncCustomerContract(sourceDetailList);
                break;
            case CUSTOMER_GROUP:
                resultList = syncCustomerGroup(sourceDetailList);
                break;
            case SO_INFO:
                resultList = syncSoInfo(sourceDetailList);
                break;
            case SO_CHANGE:
                resultList = syncSoChange(sourceDetailList);
                break;
            default:
                break;
        }
        //推送金蝶
        List<DmpPushTaskEntity> finalResultList = resultList;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(finalResultList);
            }
        });
    }

    /**
     * @description: 同步客户
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncCustomerInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerInfoEntity> list = customerInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerInfo >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            CustomerInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            List<DmpPushTaskEntity> dmpPushTaskList = syncKingdeeCustomerInfoService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.addAll(dmpPushTaskList);
        }
        return resultList;
    }

    /**
     * @description: 同步客户联系人
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncCustomerContract (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerContactEntity> list = customerContactService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerContract >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            CustomerContactEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCustomerContactService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * @description: 同步客户分组
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncCustomerGroup (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerGroupEntity> list = customerGroupService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerGroup >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            CustomerGroupEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCustomerGroupService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * @description: 同步销售订单
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncSoInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoInfoEntity> list = soInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoInfo >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SoInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSoService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
            //同步订单数据到dmp
            syncKingdeeSoService.syncOrderToDmp(entity,syncParamDetailDTO.getSyncOperate());
        }
        return resultList;
    }

    /**
     * @description: 同步销售变更
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncSoChange (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoChangeEntity> list = soChangeService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoChange >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SoChangeEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSoChangeService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

	@Override
	public Map<String , Map<String, Object>> newFindDataSendSyncTask(SyncParamDTO syncParamDTO) {
		List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        switch (sourceType) {
            case CUSTOMER_INFO:
                resultList = newSyncCustomerInfo(sourceDetailList);
                break;
            case CUSTOMER_CONTACT:
                resultList = newSyncCustomerContract(sourceDetailList);
                break;
            case CUSTOMER_GROUP:
                resultList = newSyncCustomerGroup(sourceDetailList);
                break;
            case SO_INFO:
                resultList = newSyncSoInfo(sourceDetailList);
                break;
            case SO_CHANGE:
                resultList = newSyncSoChange(sourceDetailList);
                break;
            case SDY_SKU_MAPPING:
            	resultList = newSyncSdySkuMapping(sourceDetailList);
            	break;
            case SDY_CUSTOMER_INFO:
            	resultList = newSyncSdyCustomerInfo(sourceDetailList);
            	break;
            case SDY_DELIVERY_ORDER:
            	resultList = newSyncSdyDeliveryOrder(sourceDetailList);
            	break;
            case SDY_OFFLINE_ORDER:
            	resultList = newSyncSdyOfflineOrder(sourceDetailList);
            	break;
            default:
                break;
        }
		return resultList;
	}
	
	/**
     * @description: 同步客户
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncCustomerInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerInfoEntity> list = customerInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerInfo >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            CustomerInfoEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCustomerInfoService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @description: 同步客户联系人
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncCustomerContract (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerContactEntity> list = customerContactService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerContract >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            CustomerContactEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCustomerContactService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @description: 同步客户分组
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncCustomerGroup (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerGroupEntity> list = customerGroupService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCustomerGroup >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            CustomerGroupEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCustomerGroupService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @description: 同步销售订单
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncSoInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoInfoEntity> list = soInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoInfo >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            SoInfoEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @description: 同步销售变更
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncSoChange (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoChangeEntity> list = soChangeService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoChange >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            SoChangeEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoChangeService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }
    
    private Map<String , Map<String, Object>> newSyncSdySkuMapping (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
    	List<SkuMappingEntity> list = skuMappingService.listByIds(sourceIdList);
    	if (CollectionUtils.isEmpty(list)) {
    		log.error("newSyncSdySkuMapping >>>> 未找到数据！");
    		return resultList;
    	}
    	for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
    		String sourceId = syncParamDetailDTO.getSourceId();
    		SkuMappingEntity entity = list.stream().filter(obj -> {
    			return obj.getId().equals(sourceId);
    		}).findFirst().orElse(null);
    		if (ObjectUtils.isEmpty(entity)) {
    			continue;
    		}
    		resultList.put(syncParamDetailDTO.getDataId(), skuMappingService.newSyncDataToSdy(entity, syncParamDetailDTO.getSyncOperate()));
    	}
    	return resultList;
    }
    
    private Map<String , Map<String, Object>> newSyncSdyCustomerInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<CustomerInfoEntity> list = customerInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("newSyncSdyCustomerInfo >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            CustomerInfoEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCustomerInfoService.newSyncDataToSdy(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    private Map<String , Map<String, Object>> newSyncSdyDeliveryOrder(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        //订单同步数帝云是详情级别同步，所以查询详情
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByIds(sourceIdList);

        //根据详情获取订单主表
        List<String> soIdList = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cService.listByIds(soIdList);
        if (CollectionUtils.isEmpty(soB2cEntities)) {
            log.error("newSyncSdyDeliveryOrder >>>> 未找到数据！");
            return resultList;
        }

        List<String> ids = soB2cEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoB2cDetailEntity> detailEntityList = soB2cDetailService.listByMainIds(ids);

        //产品信息
        List<String> skuNos = soB2cDetailEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        List<String> skuIds = soB2cDetailEntityList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomBySkuIds(skuIds);
        //父类产品
        List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> parentSkuList = new ArrayList<>();
        if (CollUtil.isNotEmpty(parentSkuId)) {
            parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                    .in(ProductDetailEntity::getId, parentSkuId)
                    .list();
        }
        //平台sku映射信息
        List<String> platformSkuNoList = soB2cDetailEntityList.stream().map(req -> req.getPlatformSkuNo()).distinct().collect(Collectors.toList());
        List<ListingInfoEntity> listingInfoEntities = listingInfoService.lambdaQuery().in(ListingInfoEntity::getPlatformSkuNo, platformSkuNoList).list();

        //币别
        List<String> currency = soB2cEntities.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currency);

        //店铺
        List<String> shopIds = soB2cEntities.stream().map(req -> req.getShopId()).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(shopIds)) {
            shopInfoList = shopInfoService.lambdaQuery().in(ShopInfoEntity::getId, shopIds).list();
        }

        List<String> tradeCurrency = shopInfoList.stream().map(req -> req.getTradeCurrency()).distinct().collect(Collectors.toList());
        List<DictCurrencyEntity> dictCurrencyEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(tradeCurrency)) {
            dictCurrencyEntities = FeignQuery.create(DictCurrencyEntity.class).in(DictCurrencyEntity::getId, tradeCurrency).list();
        }

        //客户
        List<String> customerIdList = shopInfoList.stream().map(req -> req.getCustomerId()).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(customerIdList)) {
            customerInfoList = customerInfoService.lambdaQuery().in(CustomerInfoEntity::getId, customerIdList).list();
        }

        List<String> orgList = new ArrayList<>();
        List<String> orgIds = customerInfoList.stream().map(req -> req.getFinancialOrganization()).distinct().collect(Collectors.toList());
        orgList.addAll(orgIds);
        List<String> salseOrgIds = shopInfoList.stream().map(req -> req.getSalesOrgId()).distinct().collect(Collectors.toList());
        orgList.addAll(salseOrgIds);

        List<BaseIdDTO.CodeDTO> companyEntities = new ArrayList<>();

        if (CollUtil.isNotEmpty(orgList)) {
            companyEntities = sysUserFeign.getAccountingCompanyList(orgList);
        }
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.SALES_PLATFORM.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);

        List<String> platformTypeList = customerInfoList.stream().map(req -> req.getPlatformType()).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictList = dictBasicService.lambdaQuery().eq(DictBasicEntity::getType, "sdySubPlatform").in(DictBasicEntity::getName, platformTypeList).list();

        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntityList.stream().filter(req -> req.getId().equalsIgnoreCase(sourceId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cDetailEntity)) {
                continue;
            }
            SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(soB2cDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cEntity)) {
                continue;
            }
            List<SoB2cDetailEntity> detailEntities = detailEntityList.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(detailEntities)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncSoB2cService.syncDataToSdyFieldHandler(soB2cEntity,
                    detailEntities,
                    soB2cDetailEntity,
                    syncParamDetailDTO.getSyncOperate(),
                    skuVOList,
                    bomChildrenSkuDTOS,
                    parentSkuList,
                    listingInfoEntities,
                    currencyList,
                    dictCurrencyEntities,
                    shopInfoList,
                    customerInfoList,
                    companyEntities,
                    dictBasicEntityList,
                    dictList));
        }
        return resultList;
    }

    private Map<String , Map<String, Object>> newSyncSdyOfflineOrder(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        //订单同步数帝云是详情级别同步，所以查询详情
        List<SoDetailEntity> soDetailEntityList = soDetailService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(soDetailEntityList)) {
            log.error("newSyncSdyOfflineOrder >>>> 未找到数据！");
            return resultList;
        }

        //产品信息
        List<String> skuNos = soDetailEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        List<String> skuIds = soDetailEntityList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //父类产品
        List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> parentSkuList = new ArrayList<>();
        if (CollUtil.isNotEmpty(parentSkuId)) {
            parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                    .in(ProductDetailEntity::getId, parentSkuId)
                    .list();
        }
        List<String> ids = soDetailEntityList.stream().map(req -> req.getMainId()).collect(Collectors.toList());
        List<SoInfoEntity> list = soInfoService.listByIds(ids);

        List<String> customerIds = list.stream().map(req -> req.getCustomerId()).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(customerIds)) {
            customerInfoEntities = customerInfoService.listByIds(customerIds);
        }

        //组织信息
        List<String> orgIdList = new ArrayList<>();
        List<String> orgIds = customerInfoEntities.stream().map(req -> req.getFinancialOrganization()).distinct().collect(Collectors.toList());
        orgIdList.addAll(orgIds);
        List<String> salesOrgIds = list.stream().map(req -> req.getSalesOrgId()).collect(Collectors.toList());
        orgIdList.addAll(salesOrgIds);
        List<BaseIdDTO.CodeDTO> companyEntities = sysUserFeign.getAccountingCompanyList(orgIdList);

        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.SALES_PLATFORM.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);

        List<String> currencyIds = list.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIds);

        List<String> soDetailIds = soDetailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoChangeDetailEntity> soChangeDetailEntities = soChangeDetailService.listBySoDetailIdList(soDetailIds);

        List<String> subPlatformType = customerInfoEntities.stream().map(req -> req.getPlatformType()).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictList = dictBasicService.lambdaQuery().eq(DictBasicEntity::getType, "sdySubPlatform").in(DictBasicEntity::getName, subPlatformType).list();

        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            SoDetailEntity soDetailEntity = soDetailEntityList.stream().filter(req -> req.getId().equalsIgnoreCase(sourceId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soDetailEntity)) {
                continue;
            }
            SoInfoEntity soInfoEntity = list.stream().filter(req -> req.getId().equals(soDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soInfoEntity)) {
                continue;
            }
            List<SoDetailEntity> detailEntityList = soDetailEntityList.stream().filter(req -> req.getMainId().equals(soInfoEntity.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(detailEntityList)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoService.syncDataToSdyFieldHandler(soInfoEntity, soDetailEntity, detailEntityList, syncParamDetailDTO.getSyncOperate(), skuVOList, bomChildrenSkuDTOS, parentSkuList, customerInfoEntities, companyEntities, dictBasicEntityList, currencyList, soChangeDetailEntities, "", dictList));
        }
        return resultList;
    }
}