package com.erp.server.plm.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.entity.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.plm.rocketmq.sync.kingdee.*;
import com.erp.server.plm.rocketmq.sync.lingxing.SyncLingXingProductDetailService;
import com.erp.server.plm.service.*;
import com.sdk.third.lingxing.dto.ProductInfo;
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
 * @description: 同步任务实现
 * @date 2023/10/30 10:43
 */
@Service
@Slf4j
public class SyncTaskServiceImpl implements SyncTaskService {

    @Resource
    private BasicCategoryService basicCategoryService;

    @Resource
    private SyncKingdeeCategoryService syncKingdeeCategoryService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private SyncKingdeeProductDetailService syncKingdeeProductDetailService;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private ProductBomHistoryService productBomHistoryService;
    
    @Resource
    private ProductBomSkuHistoryService productBomSkuHistoryService;

    @Resource
    private SyncKingdeeBomInfoService syncKingdeeBomInfoService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private ApplicationCategoryService applicationCategoryService;

    @Resource
    private SyncKingdeeApplicationCategoryService syncKingdeeApplicationCategoryService;
    @Resource
    private SyncLingXingProductDetailService syncLingXingProductDetailService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private AssetPurchaseOrderService assetPurchaseOrderService;

    @Resource
    private SyncKingdeeAssetPurchaseService syncKingdeeAssetPurchaseService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        switch (sourceType) {
            case BASIC_CATEGORY:
                resultList = syncCategory(sourceDetailList);
                break;
            case PRODUCT_DETAIL:
                resultList = syncProductDetail(sourceDetailList);
                break;
            case PRODUCT_BOM_INFO:
                resultList = syncBomInfo(sourceDetailList);
                break;
            case APPLICATION_CATEGORY:
                resultList = syncApplicationCategory(sourceDetailList);
                break;
            case ASSET_PURCHASE_ORDER:
                resultList =  syncAssetPurchaseOrder(sourceDetailList);
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

    private List<DmpPushTaskEntity> syncApplicationCategory(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<ApplicationCategoryEntity> list = applicationCategoryService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCategory >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            ApplicationCategoryEntity basicCategoryEntity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(basicCategoryEntity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeApplicationCategoryService.syncDataToKingdee(basicCategoryEntity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * @description: 同步产品分类
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncCategory (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<BasicCategoryEntity> list = basicCategoryService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCategory >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            BasicCategoryEntity basicCategoryEntity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(basicCategoryEntity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCategoryService.syncDataToKingdee(basicCategoryEntity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * @description: 同步产品
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncProductDetail (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<ProductDetailEntity> list = productDetailService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncProductDetail >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            ProductDetailEntity poroductDetailEntity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(poroductDetailEntity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeProductDetailService.syncDataToKingdee(poroductDetailEntity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * @description: 同步BOM
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncBomInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<ProductBomHistoryEntity> list = productBomHistoryService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncBomInfo >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<String> bomIdList = list.stream().map(ProductBomHistoryEntity::getBomId).collect(Collectors.toList());
        List<BomInfoEntity> bomList = bomInfoService.listByIds(bomIdList);

        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            ProductBomHistoryEntity productBomHistoryEntity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(productBomHistoryEntity)) {
                continue;
            }
            BomInfoEntity bomInfoEntity = bomList.stream().filter(obj -> obj.getId().equals(productBomHistoryEntity.getBomId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(bomInfoEntity)) {
                continue;
            }
            List<DmpPushTaskEntity> dmpPushTaskList = syncKingdeeBomInfoService.syncDataToKingdee(bomInfoEntity, syncParamDetailDTO.getSyncOperate());
            resultList.addAll(dmpPushTaskList);
        }
        return resultList;
    }

	@Override
	public Map<String, Map<String, Object>> newFindDataSendSyncTask(SyncParamDTO syncParamDTO) {
		List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        switch (sourceType) {
            case BASIC_CATEGORY:
                resultList = newSyncCategory(sourceDetailList);
                break;
            case PRODUCT_DETAIL:
                resultList = newSyncProductDetail(sourceDetailList);
                break;
            case SDY_PRODUCT_DETAIL:
            	resultList = newSyncSdyProductDetail(sourceDetailList);
                break;
            case LX_PRODUCT_DETAIL:
                resultList = newSyncLxProductDetail(sourceDetailList);
                break;
            case PRODUCT_BOM_INFO:
//                resultList = newSyncBomInfo(sourceDetailList);
                break;
            case SDY_PRODUCT_BOM_INFO:
                resultList = newSyncSdyBomInfo(sourceDetailList);
            	break;
            case APPLICATION_CATEGORY:
                resultList = newSyncApplicationCategory(sourceDetailList);
                break;
            default:
                break;
        }
		return resultList;
	}

    private Map<String, Map<String, Object>> newSyncApplicationCategory(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<ApplicationCategoryEntity> list = applicationCategoryService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCategory >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            ApplicationCategoryEntity categoryEntity = list.stream().filter(obj -> obj.getId().equals(sourceId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(categoryEntity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeApplicationCategoryService.newSyncDataToKingdee(categoryEntity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @description: 同步产品分类
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncCategory (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<BasicCategoryEntity> list = basicCategoryService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncCategory >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            BasicCategoryEntity basicCategoryEntity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(basicCategoryEntity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeCategoryService.newSyncDataToKingdee(basicCategoryEntity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * @description: 同步产品
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncProductDetail (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<ProductDetailEntity> list = productDetailService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncProductDetail >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            ProductDetailEntity poroductDetailEntity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(poroductDetailEntity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeProductDetailService.newSyncDataToKingdee(poroductDetailEntity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }
    
    private Map<String , Map<String, Object>> newSyncSdyProductDetail (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
    	List<ProductDetailEntity> list = productDetailService.listByIds(sourceIdList);
    	if (CollectionUtils.isEmpty(list)) {
    		log.error("syncProductDetail >>>> 未找到数据！");
    		return resultList;
    	}
        List<String> productIds = list.stream().map(ProductDetailEntity::getProductId).distinct().collect(Collectors.toList());
        Map<String, ProductInfoEntity> productMap = productInfoService.listByIds(productIds)
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, e -> e));

        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
    		String sourceId = syncParamDetailDTO.getSourceId();
    		ProductDetailEntity poroductDetailEntity = list.stream().filter(obj -> {
    			return obj.getId().equals(sourceId);
    		}).findFirst().orElse(null);
    		if (ObjectUtils.isEmpty(poroductDetailEntity)) {
    			continue;
    		}
    		resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeProductDetailService.newSyncDataToSdy(poroductDetailEntity, productMap.get(poroductDetailEntity.getProductId()), syncParamDetailDTO.getSyncOperate()));
    	}
    	return resultList;
    }
    
    private Map<String , Map<String, Object>> newSyncSdyBomInfo (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<ProductBomSkuHistoryEntity> list = productBomSkuHistoryService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncBomInfo >>>> 未找到数据！");
            return resultList;
        }
    	for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
    		String sourceId = syncParamDetailDTO.getSourceId();
    		ProductBomSkuHistoryEntity productBomSkuHistoryEntity = list.stream().filter(obj -> {
    			return obj.getId().equals(sourceId);
    		}).findFirst().orElse(null);
    		if (ObjectUtils.isEmpty(productBomSkuHistoryEntity)) {
    			continue;
    		}
    		resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeBomInfoService.newSyncDataToSdy(productBomSkuHistoryEntity, syncParamDetailDTO.getSyncOperate()));
    	}
    	return resultList;
    }

    /**
     * 查询同步SKU到领星
     */
    private Map<String , Map<String, Object>> newSyncLxProductDetail (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<ProductDetailEntity> list = productDetailService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("newSyncLxProductDetail >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            ProductDetailEntity productDetailEntity = list.stream()
                    .filter(obj -> obj.getId().equals(sourceId))
                    .findFirst()
                    .orElse(null);
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                continue;
            }
            ProductInfo productInfo = syncLingXingProductDetailService.convertProductInfo(productDetailEntity);
            Map<String, Object> dataMap = JSONUtil.parseObj(productInfo);
            resultList.put(syncParamDetailDTO.getDataId(), dataMap);
        }
        return resultList;
    }

    private List<DmpPushTaskEntity> syncAssetPurchaseOrder (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<AssetPurchaseOrderEntity> list = assetPurchaseOrderService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncAssetPurchaseOrder >>>> 未找到数据！");
            return Collections.EMPTY_LIST;
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            AssetPurchaseOrderEntity assetPurchaseOrderEntity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(assetPurchaseOrderEntity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeAssetPurchaseService.syncDataToKingdee(assetPurchaseOrderEntity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }
}
