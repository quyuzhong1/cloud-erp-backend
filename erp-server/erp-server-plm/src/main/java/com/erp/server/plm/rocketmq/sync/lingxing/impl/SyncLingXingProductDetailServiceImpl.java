package com.erp.server.plm.rocketmq.sync.lingxing.impl;

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
import com.erp.server.plm.rocketmq.sync.lingxing.SyncLingXingProductDetailService;
import com.erp.server.plm.service.*;
import com.sdk.third.lingxing.dto.ProductInfo;
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
        DmpPushTaskEntity dmpPushTask = getGoodsBatchPushDTO(entity);
        if (ObjectUtil.isNotEmpty(dmpPushTask)) {
            sendMTask(Collections.singletonList(dmpPushTask));
        }
    }

    private DmpPushTaskEntity getGoodsBatchPushDTO(ProductDetailEntity entity) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setSku(entity.getSkuNo());
        productInfo.setSkuIdentifier(entity.getId());
        productInfo.setProductName(entity.getName());
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.LX_PRODUCT_DETAIL.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO taskEntity = new DmpPushTaskFeignDTO();
            taskEntity.setSourceId(entity.getId());
            taskEntity.setSourceCode(entity.getSkuNo());
            taskEntity.setSourceType(SourceTypeEnum.PRODUCT_DETAIL.getCode());
            taskEntity.setMqTopic(RocketMqTopic.SYNC_LINGXING_ERP_TOPIC);
            taskEntity.setMqTag(RocketMqTagEnum.LINGXING_PRODUCT_DETAIL_TAG.getName());
            taskEntity.setMqData(JSONUtil.toJsonStr(productInfo));
            taskEntity.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskEntity.setTargetPlatformName(PlatformEnum.LINGXING.getDesc());
            taskEntity.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
            return dmpMqFeign.saveTask(taskEntity);
        }
        
        PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
        plmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.LING_XING.getCode());
        plmPushMsgEntity.setSourceType(SourceTypeEnum.LX_PRODUCT_DETAIL.getCode());
        plmPushMsgEntity.setSourceId(entity.getId());
        plmPushMsgEntity.setSourceCode(entity.getSkuNo());
        plmPushMsgEntity.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
        plmPushMsgEntity.setPushData(JSON.toJSONString(productInfo));
        
        plmPushMsgService.save(plmPushMsgEntity);
        
        return null;
    }

    @Override
    public void syncDataToLingxing(List<ProductDetailEntity> entityList) {
        List<DmpPushTaskEntity> taskEntities = entityList.stream().map(this::getGoodsBatchPushDTO).collect(Collectors.toList());
        sendMTask(taskEntities);
    }


    private void sendMTask(List<DmpPushTaskEntity> dmpPushTask) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(dmpPushTask);
            }
        });
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
}
