package com.erp.server.oms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.entity.*;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.oms.kingdee.SyncAmazonSoMultiChannelService;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Lambda
 * @Classname SyncKingdeeSoServiceImpl
 * @Date 2023-05-30 11:46
 * @Created by yl
 */
@Slf4j
@Service
public class SyncAmazonSoMultiChannelServiceImpl implements SyncAmazonSoMultiChannelService {
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cReceiverService soB2cReceiverService;
    @Resource
    private OmsPushMsgService omsPushMsgService;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private SoMultiChannelDetailService soMultiChannelDetailService;
    @Resource
    private SoB2cDetailService soB2cDetailService;

    /**
     * 销售变更单同步金碟
     *
     * @param entity
     * @param operate
     * @return void
     * @author yl
     * @date 2023-05-30 11:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(SoMultiChannelEntity entity, String operate) {
        //生成任务
        if (SyncOperateEnum.OPERATE_ADD.getCode().equals(operate)) {
            return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
        } else {
            throw new ServiceException("操作类型【{}】不支持同步亚马逊", operate);
        }
    }


    /**
     * @param entity
     * @param operate
     * @param resultMap
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     */
    private DmpPushTaskEntity saveTask(SoMultiChannelEntity entity, String operate, Map<String, Object> resultMap) {
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, SourceTypeEnum.SO_MULTI_CHANNEL.getCode())
                .eq(CfgSettingEntity::getType, settingEnum.getType())
                .eq(CfgSettingEntity::getValue, "1")
                .list();
        if (CollUtil.isEmpty(list)) {
            //添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.SO_MULTI_CHANNEL.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_AMAZON_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_SO_MULTI_CHANNEL_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.AMAZON.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }

        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(entity.getId());
        omsPushMsgEntity.setSourceCode(entity.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.SO_MULTI_CHANNEL.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        omsPushMsgEntity.setTargetPlatform(PlatformEnum.AMAZON.getName());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);

        return null;
    }


    @Override
    public Map<String, Object> newSyncDataToKingdee(SoMultiChannelEntity entity, String operate) {
        String soId = entity.getSoId();
        SoB2cEntity soB2cEntity = CharSequenceUtil.isNotBlank(soId) ? soB2cService.getById(soId) : null;
        if (Objects.isNull(soB2cEntity)) {
            throw new ServiceException("未找到销售订单【{}】信息", entity.getSoCode());
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(soId);
        SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverService.getByMainId(soId);
        if (Objects.isNull(soB2cReceiverEntity)) {
            throw new ServiceException("未找到销售订单【{}】收货人信息", entity.getSoCode());
        }
        List<SoMultiChannelDetailEntity> detailList = soMultiChannelDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException("未找到多渠道订单【{}】明细信息", entity.getCode());
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", entity.getId());
        resultMap.put("shopId", entity.getDeliveryShopId());
//        resultMap.put("marketplaceId",entity.getDeliveryShopId());
        if (CharSequenceUtil.isBlank(entity.getDeliveryCode())) {
            throw new ServiceException("未找到多渠道订单【{}】发货单号", entity.getDeliveryCode());
        }
        resultMap.put("sellerFulfillmentOrderId", entity.getDeliveryCode());
        String displayableOrderId = CharSequenceUtil.isNotBlank(entity.getPlatformCode()) ? entity.getPlatformCode() : entity.getSoCode();
        if (CharSequenceUtil.isBlank(displayableOrderId)) {
            throw new ServiceException("亚马逊订单编号不能为空");
        }
        resultMap.put("displayableOrderId", displayableOrderId);
        LocalDateTime createTime = entity.getCreateTime();
        //默认订单的创建时间，统一需要转换成0时区
        resultMap.put("displayableOrderDate", DateUtil.plus8SameUtcOffset(createTime).toString());
        resultMap.put("displayableOrderComment", CharSequenceUtil.isNotBlank(entity.getRemark()) ? entity.getRemark() : entity.getDeliveryCode());
        String logisticsChannelId = entity.getLogisticsChannelId();
        if (CharSequenceUtil.isBlank(logisticsChannelId)) {
            throw new ServiceException("未找到配送服务");
        }
        LogisticsChannelEntity channel = logisticsFeign.getChannelById(logisticsChannelId);
        if (Objects.isNull(channel)) {
            throw new ServiceException("未找到物流渠道【{}】信息", entity.getLogisticsChannelName());
        }
        //配送服务
        resultMap.put("shippingSpeedCategory", channel.getCode());
        //配送方式
        resultMap.put("fulfillmentPolicy", entity.getShippingMethod());
        //配送地址
        HashMap<String, Object> addressMap = new HashMap<>();
        if (CharSequenceUtil.isBlank(soB2cReceiverEntity.getReceiverName())) {
            throw new ServiceException("收货人姓名不能为空");
        }
        addressMap.put("name", soB2cReceiverEntity.getReceiverName());
        if (CharSequenceUtil.isNotBlank(soB2cReceiverEntity.getFirstAddress())) {
            addressMap.put("addressLine1", soB2cReceiverEntity.getFirstAddress());
        } else if (CharSequenceUtil.isNotBlank(soB2cReceiverEntity.getSecondAddress())) {
            addressMap.put("addressLine1", soB2cReceiverEntity.getSecondAddress());
        } else if (CharSequenceUtil.isNotBlank(soB2cReceiverEntity.getFullAddress())) {
            addressMap.put("addressLine1", soB2cReceiverEntity.getFullAddress());
        }
        addressMap.put("addressLine2", soB2cReceiverEntity.getSecondAddress());
        addressMap.put("addressLine3", soB2cReceiverEntity.getFullAddress());
        addressMap.put("city", soB2cReceiverEntity.getCityName());
        addressMap.put("districtOrCounty", soB2cReceiverEntity.getDistrictName());
        if (CharSequenceUtil.isBlank(soB2cReceiverEntity.getProvinceName())) {
            throw new ServiceException("收货人州省不能为空");
        }
        addressMap.put("stateOrRegion", soB2cReceiverEntity.getProvinceName());
        if (CharSequenceUtil.isBlank(soB2cReceiverEntity.getPostCode())) {
            throw new ServiceException("收货人邮编不能为空");
        }
        addressMap.put("postalCode", soB2cReceiverEntity.getPostCode());
        if (CharSequenceUtil.isBlank(soB2cReceiverEntity.getCountry())) {
            throw new ServiceException("收货人国家不能为空");
        }
        addressMap.put("countryCode", soB2cReceiverEntity.getCountry());
        addressMap.put("phone", soB2cReceiverEntity.getTelNumber());
        resultMap.put("destinationAddress", addressMap);

        List<HashMap<String, Object>> itemList = new ArrayList<>();
        for (int i = 0; i < detailList.size(); i++) {
            SoMultiChannelDetailEntity detail = detailList.get(i);
            HashMap<String, Object> itemMap = new HashMap<>();
            itemMap.put("sellerSku", detail.getPlatformSkuNo());
//            itemMap.put("sellerFulfillmentOrderItemId", entity.getDeliveryCode() + "_" + i);
            itemMap.put("sellerFulfillmentOrderItemId", detail.getId());
            itemMap.put("quantity", detail.getQty());
            itemMap.put("fulfillmentNetworkSku", detail.getFnSku());
            soB2cDetailEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(detail.getSoDetailId()) && detail.getSoDetailId().equals(e.getId())).findFirst().ifPresent(e -> {
                HashMap<String, Object> perUnitDeclaredValue = new HashMap<>();
                perUnitDeclaredValue.put("currencyCode", e.getCurrency());
                perUnitDeclaredValue.put("value", e.getPrice().toString());
                itemMap.put("perUnitDeclaredValue", perUnitDeclaredValue);
            });
            itemList.add(itemMap);
        }
        resultMap.put("items", itemList);
//        HashMap<String, String> featureConstraintsMap = new HashMap<>();
//        featureConstraintsMap.put("featureName", "BLANK_BOX");
//        featureConstraintsMap.put("featureFulfillmentPolicy", "NotRequired");
//        resultMap.put("featureConstraints", Collections.singletonList(featureConstraintsMap));
        return resultMap;
    }

}
