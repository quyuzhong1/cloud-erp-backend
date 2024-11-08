package com.erp.server.plm.rocketmq.sync.wangdian.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.LengthConverterUtil;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.SaleMethodEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.plm.rocketmq.sync.wangdian.SyncWangDianProductDetailService;
import com.erp.server.plm.service.*;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsBatchPushDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.erp.model.plm.enums.SaleMethodEnum.*;
import static com.erp.server.plm.constant.ProductConstant.PRODUCT_PROPERTY_COST;
import static com.erp.server.plm.constant.ProductConstant.PRODUCT_PROPERTY_SERVICE;

@Service
public class SyncWangDianProductDetailServiceImpl implements SyncWangDianProductDetailService {

    @Resource
    private ProductInfoService productInfoService;
    @Resource
    private ProductPurchaseService productPurchaseService;
    @Resource
    private ProductPackService productPackService;
    @Resource
    private BomSkuService bomSkuService;
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private PlmPushMsgService plmPushMsgService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDataToWangDian(ProductDetailEntity entity) {
        DmpPushTaskEntity dmpPushTask = getGoodsBatchPushDTO(entity);
        if (ObjectUtil.isNotEmpty(dmpPushTask)) {
            sendMTask(Collections.singletonList(dmpPushTask));
        }
    }

    private DmpPushTaskEntity getGoodsBatchPushDTO(ProductDetailEntity entity) {
        List<BomInfoEntity> infoEntities = bomSkuService.listBomByParentSkuIds(Collections.singletonList(entity.getId()));
        BomInfoEntity bomInfoEntity = infoEntities.stream()
                .findFirst().orElse(null);
        if (!ObjectUtils.isEmpty(bomInfoEntity) && BomTypeEnum.COMBINATION.getType().equals(bomInfoEntity.getType())){
            return null;
        }
        ProductInfoEntity info = productInfoService.getById(entity.getProductId());
        ProductPurchaseEntity productPurchase = Optional.ofNullable(productPurchaseService.getBySkuId(entity.getId())).orElse(new ProductPurchaseEntity());
        ProductPackEntity productPack = productPackService.getBySkuId(entity.getId());
        GoodsBatchPushDTO dto = new GoodsBatchPushDTO();
        dto.setGoodsNo(entity.getSkuNo());
        dto.setGoodsName(entity.getName());
        dto.setGoodsType(getGoodsType(info.getSaleMethod(), info.getProperty()));
        GoodsBatchPushDTO.SpecList specList = new GoodsBatchPushDTO.SpecList();
        specList.setSpecNo(entity.getSkuNo());
        specList.setSpecName(entity.getName());
        specList.setBarcode(productPurchase.getEan());
        specList.setWeight(MathUtil.divide(productPack.getNetWeight(), new BigDecimal(1000), 4, BigDecimal.ROUND_HALF_UP));
        specList.setLength(LengthConverterUtil.mmToCm(productPack.getProductLength()));
        specList.setWidth(LengthConverterUtil.mmToCm(productPack.getProductWidth()));
        specList.setHeight(LengthConverterUtil.mmToCm(productPack.getProductHeight()));
        specList.setImgUrl(entity.getImagesUrl());
//        specList.setUnitName(entity.getUnitName());
        dto.setSpecList(Collections.singletonList(specList));
        
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.WDT_PRODUCT_DETAIL.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO taskEntity = new DmpPushTaskFeignDTO();
            taskEntity.setSourceId(entity.getId());
            taskEntity.setSourceCode(entity.getSkuNo());
            taskEntity.setSourceType(SourceTypeEnum.PRODUCT_DETAIL.getCode());
            taskEntity.setMqTopic(RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC);
            taskEntity.setMqTag(RocketMqTagEnum.WDT_PRODUCT_DETAIL_TAG.getName());
            taskEntity.setMqData(JSONUtil.toJsonStr(dto));
            taskEntity.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskEntity.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
            taskEntity.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
            return dmpMqFeign.saveTask(taskEntity);
        }
        
        PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
        plmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.WDT.getCode());
        plmPushMsgEntity.setSourceType(SourceTypeEnum.WDT_PRODUCT_DETAIL.getCode());
        plmPushMsgEntity.setSourceId(entity.getId());
        plmPushMsgEntity.setSourceCode(entity.getSkuNo());
        plmPushMsgEntity.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
        plmPushMsgEntity.setPushData(JSON.toJSONString(dto));
        
        plmPushMsgService.save(plmPushMsgEntity);
        
        return null;
    }

    @Override
    public void syncDataToWangDian(List<ProductDetailEntity> entityList) {
        List<DmpPushTaskEntity> taskEntities = entityList.stream().map(this::getGoodsBatchPushDTO).collect(Collectors.toList());
        sendMTask(taskEntities);
    }


    private int getGoodsType(String saleMethod, String property) {
        if (StringUtils.isEmpty(saleMethod)) {
            return 0;
        }
        String[] sales = saleMethod.split(",");
        List<SaleMethodEnum> saleMethods = Stream.of(sales).map(SaleMethodEnum::getEnumByName).collect(Collectors.toList());
        if (saleMethods.contains(GOODS)) {
            return (PRODUCT_PROPERTY_COST.equals(property) || PRODUCT_PROPERTY_SERVICE.equals(property)) ? 5 : 1;
        } else if (saleMethods.contains(GIFT)) {
            return 0;
        } else if (saleMethods.contains(PACKAGING_MATERIALS)) {
            return 3;
        } else if (saleMethods.contains(SEMI_FINISHED)) {
            return 2;
        } else {
            return 0;
        }
    }

    private void sendMTask(List<DmpPushTaskEntity> dmpPushTask) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(dmpPushTask);
            }
        });
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask(ProductDetailEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.PRODUCT_DETAIL.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getSkuNo());
            taskFeignDTO.setSourceType(SourceTypeEnum.PRODUCT_DETAIL.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_PRODUCT_DETAIL_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }
        
        PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
        plmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        plmPushMsgEntity.setSourceType(SourceTypeEnum.PRODUCT_DETAIL.getCode());
        plmPushMsgEntity.setSourceId(entity.getId());
        plmPushMsgEntity.setSourceCode(entity.getSkuNo());
        plmPushMsgEntity.setSyncOperate(operate);
        plmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
        plmPushMsgService.save(plmPushMsgEntity);
        
        return null;
    }


    @Override
    public void addPlmPushMsg(ProductDetailEntity entity) {
        Map<String, Object> pushData = new HashMap<>();
        pushData.put("remark",String.format("【%s】删除，同步旺店通失败", entity.getSkuNo()));
        PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
        plmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.WDT.getCode());
        plmPushMsgEntity.setSourceType(SourceTypeEnum.WDT_PRODUCT_DETAIL.getCode());
        plmPushMsgEntity.setSourceId(entity.getId());
        plmPushMsgEntity.setSourceCode(entity.getSkuNo());
        plmPushMsgEntity.setSyncOperate(SyncOperateEnum.OPERATE_SYNC_ERROR.getCode());
        plmPushMsgEntity.setPushData(JSON.toJSONString(pushData));
        plmPushMsgService.save(plmPushMsgEntity);
    }
}
