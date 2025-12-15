package com.erp.server.oms.rocketmq.sync.wangdian.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.dto.KolSubB2cApplicationDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.oms.rocketmq.sync.wangdian.SyncWangDianSoB2cService;
import com.erp.server.oms.service.*;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelf2Request;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SyncWangDianSoB2cServiceImpl implements SyncWangDianSoB2cService {

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private OmsPushMsgService omsPushMsgService;
    @Resource
    private KolB2cApplicationService kolB2cApplicationService;
    @Resource
    private KolB2cApplicationDetailService kolB2cApplicationDetailService;
    @Resource
    private KolB2cApplicationAddressService kolB2cApplicationAddressService;

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDataToWangDian(KolSubB2cApplicationDTO.PushDTO pushDTO,Map<String, SkuVO> skuMap ) {
        DmpPushTaskEntity dmpPushTask = buildDmpPushTask(pushDTO,skuMap);
        if (ObjectUtil.isNotEmpty(dmpPushTask)) {
            sendMTask(Collections.singletonList(dmpPushTask));
        }
    }

    private DmpPushTaskEntity buildDmpPushTask(KolSubB2cApplicationDTO.PushDTO pushDTO,Map<String, SkuVO> skuMap) {
        PushSelf2Request request = new PushSelf2Request();
        //原始单信息
        List<PushSelf2Request.RawTrade> rawTradeList = new ArrayList<>();
        //原始子单信息
        List<PushSelf2Request.RawTradeOrder> rawTradeOrderList = new ArrayList<>();

        KolSubB2cApplicationEntity entity = pushDTO.getEntity();
        List<KolSubB2cApplicationDetailEntity> detailList = pushDTO.getDetailList();
        //拆分前订单
        KolB2cApplicationEntity kolB2cApplicationEntity = kolB2cApplicationService.getById(entity.getSourceId());
        //达人收货地址
        KolB2cApplicationAddressEntity kolB2cApplicationAddressEntity = kolB2cApplicationAddressService.lambdaQuery()
                .eq(KolB2cApplicationAddressEntity::getMainId, entity.getSourceId())
                .eq(KolB2cApplicationAddressEntity::getPartnerId, entity.getPartnerId())
                .one();

//        //wdt仓库映射
//        String warehouseCode = "";
//        String warehouseId = kolB2cApplicationEntity.getWarehouseId();
//        if(StringUtils.isNotBlank(warehouseId)){
//            List<ThirdMappingEntity> warehouses = FeignQuery.create(ThirdMappingEntity.class)
//                    .eq(ThirdMappingEntity::getType, "warehouse")
//                    .eq(ThirdMappingEntity::getThirdSysType, "wdt")
//                    .eq(ThirdMappingEntity::getDisabled, false)
//                    .eq(ThirdMappingEntity::getSysId, warehouseId)
//                    .list();
//            if(CollUtil.isEmpty(warehouses)){
//                throw new ServiceException(ApiError.ERROR_WDT_NOT_FOUND_WAREHOUSE_MAPPING,kolB2cApplicationEntity.getWarehouseName());
//            }
//            warehouseCode = warehouses.get(0).getThirdCode();
//        }


        //wdt物流渠道映射
        String logisticsCode = "";
                String logisticsChannelId = kolB2cApplicationEntity.getLogisticsChannelId();
        if(StringUtils.isNotBlank(logisticsChannelId)){
            List<ThirdMappingEntity> logisticsChannels = FeignQuery.create(ThirdMappingEntity.class)
                    .eq(ThirdMappingEntity::getType, "logistics")
                    .eq(ThirdMappingEntity::getThirdSysType, "wdt")
                    .eq(ThirdMappingEntity::getDisabled, false)
                    .eq(ThirdMappingEntity::getSysId, logisticsChannelId)
                    .list();
            if(CollUtil.isEmpty(logisticsChannels)){
                throw new ServiceException(ApiError.ERROR_WDT_NOT_FOUND_LOGISTICSCHANNEL_MAPPING,kolB2cApplicationEntity.getLogisticsChannelName());
            }
            logisticsCode = logisticsChannels.get(0).getThirdCode();
        }

        //wdt店铺映射
        String shopId = kolB2cApplicationEntity.getShopId();
        List<ThirdMappingEntity> shops = FeignQuery.create(ThirdMappingEntity.class)
                .eq(ThirdMappingEntity::getType, "shop")
                .eq(ThirdMappingEntity::getThirdSysType, "wdt")
                .eq(ThirdMappingEntity::getDisabled, false)
                .eq(ThirdMappingEntity::getSysId, shopId)
                .list();
        if(CollUtil.isEmpty(shops)){
            throw new ServiceException(ApiError.ERROR_WDT_NOT_FOUND_SHOP_MAPPING,kolB2cApplicationEntity.getShopName());
        }
        String shopNo = shops.get(0).getThirdCode();
        request.setShopNo(shopNo);

        PushSelf2Request.RawTrade rawTrade = new PushSelf2Request.RawTrade();
        rawTrade.setTid(entity.getCode());
        rawTrade.setProcessStatus(PushSelf2Request.RawTrade.PROCESS_STATUS_DELIVERED);
        rawTrade.setTradeStatus(PushSelf2Request.RawTrade.TRADE_STATUS_DELIVERY);
        rawTrade.setRefundStatus(PushSelf2Request.RawTrade.REFUND_STATUS_NO);
        rawTrade.setPayStatus(PushSelf2Request.RawTrade.PAY_STATUS_PAID);
        rawTrade.setOrderCount(detailList.size());

        int totalApplyQty = detailList.stream()
                .mapToInt(KolSubB2cApplicationDetailEntity::getApplyQty)
                .sum();
        rawTrade.setGoodsCount(new BigDecimal(totalApplyQty));

        rawTrade.setPayMethod(PushSelf2Request.RawTrade.PAY_METHOD_CASH);

        String approveTimeString = kolB2cApplicationEntity.getApproveTime().format(formatter);
        rawTrade.setTradeTime(approveTimeString);
        rawTrade.setPayTime(approveTimeString);
        rawTrade.setEndTime(null);
        rawTrade.setBuyerNick(entity.getNickname());
        rawTrade.setReceiverName(kolB2cApplicationAddressEntity.getReceiverName());
        //省市区空格分隔，示例【北京 北京市 朝阳区】
        rawTrade.setReceiverArea(StrUtil.format("{} {} {}",kolB2cApplicationAddressEntity.getProvince(), kolB2cApplicationAddressEntity.getCity(), kolB2cApplicationAddressEntity.getDistrict()));
        rawTrade.setReceiverAddress(kolB2cApplicationAddressEntity.getDetailAddress());
        rawTrade.setReceiverZip(kolB2cApplicationAddressEntity.getZipCode());
        rawTrade.setReceiverMobile(kolB2cApplicationAddressEntity.getReceiverPhone());
        rawTrade.setPostAmount(BigDecimal.ZERO);
        rawTrade.setOtherAmount(BigDecimal.ZERO);
        rawTrade.setDiscount(BigDecimal.ZERO);
        rawTrade.setReceivable(BigDecimal.ZERO);
        rawTrade.setPlatformCost(BigDecimal.ZERO);
        rawTrade.setInvoiceType(PushSelf2Request.RawTrade.INVOICE_TYPE_NO);
        rawTrade.setDeliveryTerm(PushSelf2Request.RawTrade.DELIVERY_TERM_PAY_FIRST);
        if(StringUtils.isNotBlank(logisticsCode)){
            rawTrade.setCustData(logisticsCode);
        }
        rawTrade.setIsAutoWms(Boolean.FALSE);
        rawTrade.setWarehouseNo("");
        rawTrade.setIsSealed(PushSelf2Request.RawTrade.IS_SEALED_NOT_ALLOW);
        rawTrade.setIdCardType(PushSelf2Request.RawTrade.ID_CARD_TYPE_NONE);
        rawTrade.setIdCard("");
        rawTradeList.add(rawTrade);

        for (KolSubB2cApplicationDetailEntity detailEntity : detailList) {
            SkuVO skuVO = skuMap.get(detailEntity.getSkuId());

            PushSelf2Request.RawTradeOrder rawTradeOrder = new PushSelf2Request.RawTradeOrder();
            rawTradeOrder.setTid(entity.getCode());
            rawTradeOrder.setOid(detailEntity.getId());
            rawTradeOrder.setStatus(PushSelf2Request.RawTradeOrder.STATUS_WAIT_DELIVERY);
            rawTradeOrder.setRefundStatus(PushSelf2Request.RawTradeOrder.REFUND_STATUS_NO);
            rawTradeOrder.setGoodsId(skuVO.getSpuNo());
            rawTradeOrder.setGoodsNo(skuVO.getSpuNo());
            rawTradeOrder.setGoodsName(skuVO.getSpuName());
            rawTradeOrder.setSpecId(detailEntity.getSkuNo());
            rawTradeOrder.setSpecNo(detailEntity.getSkuNo());
            rawTradeOrder.setSpecName(skuVO.getSkuName());
            rawTradeOrder.setOrderType(PushSelf2Request.RawTradeOrder.ORDER_TYPE_NORMAL);
            rawTradeOrder.setNum(new BigDecimal(detailEntity.getApplyQty()));
            rawTradeOrder.setPrice(BigDecimal.ZERO);
            rawTradeOrder.setDiscount(BigDecimal.ZERO);
            rawTradeOrder.setShareDiscount(BigDecimal.ZERO);
            rawTradeOrder.setTotalAmount(BigDecimal.ZERO);
            rawTradeOrder.setAdjustAmount(BigDecimal.ZERO);
            rawTradeOrder.setRefundAmount(BigDecimal.ZERO);
            rawTradeOrder.setRemark(detailEntity.getRemark());
            rawTradeOrder.setJson("");
            rawTradeOrderList.add(rawTradeOrder);
        }
        request.setRawTradeList(rawTradeList);
        request.setRawTradeOrderList(rawTradeOrderList);

//        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
//        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
//                .eq(CfgSettingEntity::getKey, SourceTypeEnum.WDT_SO_B2C.getCode())
//                .eq(CfgSettingEntity::getType, settingEnum.getType())
//                .eq(CfgSettingEntity::getValue, "1")
//                .list();
//        if(CollUtil.isEmpty(list)) {
//            //添加推送任务
//            DmpPushTaskFeignDTO taskEntity = new DmpPushTaskFeignDTO();
//            taskEntity.setSourceId(entity.getId());
//            taskEntity.setSourceCode(entity.getCode());
//            taskEntity.setSourceType(SourceTypeEnum.KOL_SUB_B2C_APPLICATION.getCode());
//            taskEntity.setMqTopic(RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC);
//            taskEntity.setMqTag(RocketMqTagEnum.WDT_SO_B2C_TAG.getName());
//            taskEntity.setMqData(JSONUtil.toJsonStr(request));
//            taskEntity.setSourcePlatformName(PlatformEnum.ERP.getDesc());
//            taskEntity.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
//            taskEntity.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
//            return dmpMqFeign.saveTask(taskEntity);
//        }

        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.WDT.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.WDT_SO_B2C.getCode());
        omsPushMsgEntity.setSourceId(entity.getId());
        omsPushMsgEntity.setSourceCode(entity.getCode());
        omsPushMsgEntity.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(request));
        omsPushMsgService.save(omsPushMsgEntity);
        return null;
    }

    private void sendMTask(List<DmpPushTaskEntity> dmpPushTask) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(dmpPushTask);
            }
        });
    }

}
