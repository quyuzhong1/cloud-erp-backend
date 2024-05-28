package com.erp.server.plm.rocketmq.sync.wangdian.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsBatchPushDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.utils.LengthConverterUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.plm.entity.ProductPurchaseEntity;
import com.erp.model.plm.enums.SaleMethodEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.plm.rocketmq.sync.wangdian.SyncWangDianProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProductPackService;
import com.erp.server.plm.service.ProductPurchaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private DmpMqFeign dmpMqFeign;

    @Override
    public void syncDataToWangDian(ProductDetailEntity entity) {
        DmpPushTaskEntity dmpPushTask = getGoodsBatchPushDTO(entity);
        sendMTask(Collections.singletonList(dmpPushTask));
    }

    private DmpPushTaskEntity getGoodsBatchPushDTO(ProductDetailEntity entity) {
        ProductInfoEntity info = productInfoService.getById(entity.getProductId());
        ProductPurchaseEntity productPurchase = productPurchaseService.getBySkuId(entity.getId());
        ProductPackEntity productPack = productPackService.getBySkuId(entity.getId());
        GoodsBatchPushDTO dto = new GoodsBatchPushDTO();
        dto.setGoodsNo(info.getSpuNo());
        dto.setGoodsName(info.getName());
        dto.setGoodsType(getGoodsType(info.getSaleMethod(), info.getProperty()));
        GoodsBatchPushDTO.SpecList specList = new GoodsBatchPushDTO.SpecList();
        specList.setSpecNo(entity.getSkuNo());
        specList.setBarcode(productPurchase.getEan());
        specList.setWeight(LengthConverterUtil.mmToCm(productPack.getProductLength()));
        specList.setLength(LengthConverterUtil.mmToCm(productPack.getProductLength()));
        specList.setWidth(LengthConverterUtil.mmToCm(productPack.getProductLength()));
        specList.setHeight(LengthConverterUtil.mmToCm(productPack.getProductLength()));
        specList.setImgUrl(entity.getImagesUrl());
//        specList.setUnitName(entity.getUnitName());
        dto.setSpecList(Collections.singletonList(specList));
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

    @Override
    public void syncDataToWangDian(List<ProductDetailEntity> entityList) {
        List<DmpPushTaskEntity> taskEntities = entityList.stream().map(this::getGoodsBatchPushDTO).collect(Collectors.toList());
        sendMTask(taskEntities);
    }


    private int getGoodsType(String saleMethod, String property) {
        SaleMethodEnum saleMethodEnum = SaleMethodEnum.getEnumByType(saleMethod);
        if (ObjectUtils.isEmpty(saleMethodEnum)) {
            return 0;
        }
        switch (saleMethodEnum) {
            case GOODS:
                return (PRODUCT_PROPERTY_COST.equals(property) || PRODUCT_PROPERTY_SERVICE.equals(property)) ? 5 : 1;
            case PACKAGING_MATERIALS:
                return 3;
            case SEMI_FINISHED:
                return 2;
            default:
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
}
