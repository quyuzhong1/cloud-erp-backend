package com.erp.server.plm.rocketmq.sync.wangdian.impl;

import cn.hutool.json.JSONUtil;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsBatchPushDTO;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.utils.LengthConverterUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
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
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Collections;

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
        ProductInfoEntity info = productInfoService.getById(entity.getProductId());
        ProductPurchaseEntity productPurchase = productPurchaseService.getBySkuId(entity.getId());
        ProductPackEntity productPack = productPackService.getBySkuId(entity.getId());
        GoodsBatchPushDTO dto = new GoodsBatchPushDTO();
        dto.setGoodsNo(info.getSpuNo());
        dto.setGoodsName(info.getName());
        dto.setGoodsType(getGoodsType(info.getSaleMethod(),info.getProperty()));
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
        sendMqAndSaveTask(entity, dto);
    }


    private int getGoodsType(String saleMethod, String property){
        SaleMethodEnum saleMethodEnum = SaleMethodEnum.getEnumByType(saleMethod);
        if (ObjectUtils.isEmpty(saleMethodEnum)){
            return 0;
        }
        switch (saleMethodEnum){
            case GOODS:return (PRODUCT_PROPERTY_COST.equals(property) || PRODUCT_PROPERTY_SERVICE.equals(property)) ? 5 : 1;
            case PACKAGING_MATERIALS: return 3;
            case SEMI_FINISHED:return 2;
            default: return 0;
        }
    }

    private void sendMqAndSaveTask (ProductDetailEntity entity, GoodsBatchPushDTO dto) {
        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(entity.getId());
        taskFeignDTO.setSourceCode(entity.getSkuNo());
        taskFeignDTO.setSourceType(SourceTypeEnum.PRODUCT_DETAIL.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.WANGDIAN_PRODUCT_DETAIL_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(dto));
        taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        taskFeignDTO.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
        dmpMqFeign.sendMqAndSaveTask(taskFeignDTO);
    }
}
