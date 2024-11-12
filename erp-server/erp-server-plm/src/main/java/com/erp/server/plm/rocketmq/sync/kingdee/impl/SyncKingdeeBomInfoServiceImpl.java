package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.PlmPushMsgEntity;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.model.plm.entity.ProductBomSkuHistoryEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomStateEnum;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeBomInfoService;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.PlmPushMsgService;
import com.erp.server.plm.service.ProductBomHistoryService;
import com.erp.server.plm.service.ProductBomSkuHistoryService;
import com.erp.server.plm.service.ProductDetailService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import io.seata.spring.annotation.GlobalTransactional;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/8 18:13
 */
@Service
public class SyncKingdeeBomInfoServiceImpl implements SyncKingdeeBomInfoService {
    @Resource
    private ProductBomHistoryService productBomHistoryService;

    @Resource
    private ProductBomSkuHistoryService productBomSkuHistoryService;

    @Resource
    private DmpMqFeign dmpMqFeign;
    
    @Resource
    private PlmPushMsgService plmPushMsgService;
    
    @Resource
    private BomInfoService bomInfoService;
    
    @Resource
    private ProductDetailService productDetailService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<DmpPushTaskEntity> syncDataToKingdee(BomInfoEntity entity,String operate) {

        //bom历史数据
        List<ProductBomHistoryEntity> bomHistoryList = productBomHistoryService.listByBomId(entity.getId());
        if (CollectionUtils.isEmpty(bomHistoryList)) {
            return null;
        }
        //bom历史明细数据
        List<String> bomHistoryIdList = bomHistoryList.stream().map(ProductBomHistoryEntity::getId).collect(Collectors.toList());
        List<ProductBomSkuHistoryEntity> skuHistoryList = productBomSkuHistoryService.getSkuByHistoryIds(bomHistoryIdList);
        if (CollectionUtils.isEmpty(skuHistoryList)) {
            return null;
        }

        List<Map<String, Object>> listMap = new ArrayList<>();

        List<DmpPushTaskEntity> pushTaskList = new ArrayList<>();

        for (ProductBomHistoryEntity productBomHistoryEntity : bomHistoryList) {
            Map<String, Object> resultMap = new HashMap<>();


            //金蝶id
            resultMap.put("syncKingdeeId",productBomHistoryEntity.getSyncKingdeeId());
            //操作（枚举SyncKingdeeOperateEnum）
            resultMap.put("operate", operate);

            List<ProductBomSkuHistoryEntity> childrenList = skuHistoryList.stream().filter(obj -> obj.getBomHistoryId().equals(productBomHistoryEntity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(childrenList)) {
               throw new ServiceException("bom历史子级明细数据不能为空");
            }
            //父级物料
            ProductBomSkuHistoryEntity parent = childrenList.get(0);
            //父级sku编码
            resultMap.put("id",productBomHistoryEntity.getId());
            //父级sku编码
            resultMap.put("parentSkuNo",parent.getParentSkuNo());
            //版本
            resultMap.put("version",parent.getParentSkuNo().concat("_").concat(productBomHistoryEntity.getBomVersion().toString()));

            //删除操作
            if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
                DmpPushTaskEntity pushTaskEntity = saveTask(operate, resultMap);
                pushTaskList.add(pushTaskEntity);
                continue;
            }
            if (!entity.getBomVersion().equals(productBomHistoryEntity.getBomVersion())) {
                DmpPushTaskEntity productBomHistoryTask = dmpMqFeign.getByParam(new DmpSyncTaskDTO.OneDTO(SourceTypeEnum.PRODUCT_BOM_INFO.getCode(), productBomHistoryEntity.getId(), PlatformEnum.KINGDEE.getDesc(), PlatformEnum.ERP.getDesc()));
                if (SyncStatusEnum.SUCCESS_SYNC.getCode().equals(productBomHistoryTask.getStatus())) {
                    continue;
                }
            }

            List<Map<String, Object>> mapList = new ArrayList<>();
            for (ProductBomSkuHistoryEntity child: childrenList) {
                Map<String, Object> detailMap = new HashMap<>(MathUtil.THREE);
                detailMap.put("skuNo",child.getSkuNo());
                detailMap.put("date", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                detailMap.put("quantity",child.getQuantity().toString());
                mapList.add(detailMap);
            }
            resultMap.put("list",mapList);
            listMap.add(resultMap);
        }
        if (CollectionUtils.isEmpty(listMap)) {
            return null;
        }
        listMap.forEach(obj -> {
            //生成任务
            DmpPushTaskEntity pushTaskEntity = saveTask(operate, obj);
            pushTaskList.add(pushTaskEntity);
        });
        return pushTaskList;
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity  saveTask (String operate,Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.PRODUCT_BOM_INFO.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId((String)resultMap.get("id"));
            taskFeignDTO.setSourceCode((String)resultMap.get("version"));
            taskFeignDTO.setSourceType(SourceTypeEnum.PRODUCT_BOM_INFO.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_BOM_INFO_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }
    	
    	PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
        plmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        plmPushMsgEntity.setSourceType(SourceTypeEnum.PRODUCT_BOM_INFO.getCode());
        plmPushMsgEntity.setSourceId((String)resultMap.get("id"));
        plmPushMsgEntity.setSourceCode((String)resultMap.get("version"));
        plmPushMsgEntity.setSyncOperate(operate);
        plmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
        plmPushMsgService.save(plmPushMsgEntity);
        
        return null;
    }

	@Override
	public void syncDataToSdy(BomInfoEntity entity, String operate) {
		if(!BomTypeEnum.COMBINATION.getType().equals(entity.getType())) {
			return;
		}
		List<ProductBomHistoryEntity> bomHistoryList = productBomHistoryService.lambdaQuery()
			.eq(ProductBomHistoryEntity::getBomId, entity.getId())
			.eq(ProductBomHistoryEntity::getBomVersion, entity.getBomVersion())
			.list();
		List<String> bomHistoryIdList = bomHistoryList.stream().map(ProductBomHistoryEntity::getId).collect(Collectors.toList());
        List<ProductBomSkuHistoryEntity> skuHistoryList = productBomSkuHistoryService.getSkuByHistoryIds(bomHistoryIdList);
        
        if(CollUtil.isNotEmpty(skuHistoryList)) {
        	List<PlmPushMsgEntity> plmPushMsgEntityList = new ArrayList<>();
        	for(ProductBomSkuHistoryEntity skuHistory : skuHistoryList) {
        		PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
                plmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
                plmPushMsgEntity.setSourceType(SourceTypeEnum.SDY_PRODUCT_BOM_INFO.getCode());
                plmPushMsgEntity.setSourceId(skuHistory.getId());
                plmPushMsgEntity.setSourceCode(skuHistory.getSkuNo());
                plmPushMsgEntity.setSyncOperate(operate);
                plmPushMsgEntity.setPushData(JSON.toJSONString(this.newSyncDataToSdy(skuHistory, operate)));
            }
        	plmPushMsgService.saveBatch(plmPushMsgEntityList);
        }
	}

	@Override
	public Map<String, Object> newSyncDataToSdy(ProductBomSkuHistoryEntity entity, String operate) {
		ProductBomHistoryEntity productBomHistoryEntity = productBomHistoryService.getById(entity.getBomHistoryId());
		BomInfoEntity bomInfoEntity = bomInfoService.getById(productBomHistoryEntity.getBomId());
		
		Map<String, String> productIdNameMap = productDetailService.listByIds(Arrays.asList(entity.getSkuId() , entity.getParentSkuId()))
				.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("biz_uni_key", entity.getId());
		resultMap.put("combination_type", BomTypeEnum.getName(bomInfoEntity.getType()));
		resultMap.put("msku", entity.getParentSkuNo());
		resultMap.put("msku_description", productIdNameMap.get(entity.getParentSkuId()));
		resultMap.put("sku", entity.getSkuNo());
		resultMap.put("sku_description", productIdNameMap.get(entity.getSkuId()));
		resultMap.put("product_code", entity.getSkuNo());
		resultMap.put("product_name", productIdNameMap.get(entity.getSkuId()));
		resultMap.put("product_quota", entity.getQuantity());
		resultMap.put("create_time", bomInfoEntity.getCreateTime());
		resultMap.put("owner", bomInfoEntity.getCreateUserName());
		resultMap.put("version", bomInfoEntity.getBomVersion());
		
		if(SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
			resultMap.put("status", "已删除");
		}else {
			resultMap.put("status", BomStateEnum.getName(bomInfoEntity.getState()));
		}
		return resultMap;
	}
}
