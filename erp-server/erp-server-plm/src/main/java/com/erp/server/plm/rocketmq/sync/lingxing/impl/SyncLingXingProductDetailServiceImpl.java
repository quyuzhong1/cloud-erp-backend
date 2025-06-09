package com.erp.server.plm.rocketmq.sync.lingxing.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.plm.entity.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.plm.rocketmq.sync.lingxing.SyncLingXingProductDetailService;
import com.erp.server.plm.service.*;
import com.sdk.third.lingxing.dto.ProductInfo;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SyncLingXingProductDetailServiceImpl implements SyncLingXingProductDetailService {

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
    public void syncDataToLingxing(ProductDetailEntity entity) {
        PlmPushMsgEntity plmPushMsgEntity = createPlmPushMsgEntity(entity);
        boolean save = plmPushMsgService.save(plmPushMsgEntity);
        if (!save){
            ServiceException.runError("保存本地消息失败:{}", JSONUtil.toJsonStr(plmPushMsgEntity));
        }
    }

    @Override
    public PlmPushMsgEntity createPlmPushMsgEntity(ProductDetailEntity entity) {
//        ProductInfo productInfo = convertProductInfo(entity);
        PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
        plmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.LING_XING.getCode());
        plmPushMsgEntity.setSourceType(SourceTypeEnum.LX_PRODUCT_DETAIL.getCode());
        plmPushMsgEntity.setSourceId(entity.getId());
        plmPushMsgEntity.setSourceCode(entity.getSkuNo());
        plmPushMsgEntity.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
        plmPushMsgEntity.setPushData(JSON.toJSONString(DmpOutputConstant.getQuerySyncMap()));
        return plmPushMsgEntity;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDataToLingxing(List<ProductDetailEntity> entityList) {
        List<PlmPushMsgEntity> msgList = entityList.stream()
                .map(this::createPlmPushMsgEntity)
                .collect(Collectors.toList());
        boolean save = plmPushMsgService.saveBatch(msgList);
        if (!save){
            ServiceException.runError("保存本地消息失败:{}", JSONUtil.toJsonStr(msgList));
        }
    }




    @Override
    public void addPlmPushMsg(ProductDetailEntity entity) {
        Map<String, Object> pushData = new HashMap<>();
        pushData.put("remark",String.format("【%s】删除，同步领星失败", entity.getSkuNo()));
        PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
        plmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.LING_XING.getCode());
        plmPushMsgEntity.setSourceType(SourceTypeEnum.LX_PRODUCT_DETAIL.getCode());
        plmPushMsgEntity.setSourceId(entity.getId());
        plmPushMsgEntity.setSourceCode(entity.getSkuNo());
        plmPushMsgEntity.setSyncOperate(SyncOperateEnum.OPERATE_SYNC_ERROR.getCode());
        plmPushMsgEntity.setPushData(JSON.toJSONString(pushData));
        plmPushMsgService.save(plmPushMsgEntity);
    }

    @Override
    public ProductInfo convertProductInfo(ProductDetailEntity entity) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setSku(LingxingApiUtils.convertLxSku(entity.getSkuNo()));
//        productInfo.setSkuIdentifier(entity.getId());
        productInfo.setProductName(LingxingApiUtils.convertLxProductName(entity.getName()));
        return productInfo;
    }
}
