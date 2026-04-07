package com.erp.server.oms.rocketmq.sync.wangdian.impl;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.DictCityConstants;
import com.erp.model.dmp.dto.DmpThirdCityDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.dto.KolSubB2cApplicationDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpThirdCityFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.rocketmq.sync.wangdian.SyncWangDianSoB2cService;
import com.erp.server.oms.service.*;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelf2Request;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SyncWangDianSoB2cServiceImpl implements SyncWangDianSoB2cService {

    @Resource
    private OmsPushMsgService omsPushMsgService;
    @Resource
    private KolB2cApplicationService kolB2cApplicationService;
    @Resource
    private DmpThirdCityFeign DmpThirdCityFeign;
    @Resource
    private KolB2cApplicationAddressService kolB2cApplicationAddressService;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToWangDian(KolSubB2cApplicationDTO.PushDTO pushDTO,Map<String, SkuVO> skuMap ) {
        PushSelf2Request request = newSyncKolB2c(pushDTO, skuMap);
        KolSubB2cApplicationEntity entity = pushDTO.getEntity();
        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.WDT.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.WDT_SO_B2C.getCode());
        omsPushMsgEntity.setSourceId(entity.getId());
        omsPushMsgEntity.setSourceCode(entity.getCode());
        omsPushMsgEntity.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(request));
        omsPushMsgService.save(omsPushMsgEntity);
        return  null ;
    }

    @Override
    public PushSelf2Request newSyncKolB2c(KolSubB2cApplicationDTO.PushDTO pushDTO, Map<String, SkuVO> skuMap) {
        //判断skuMap不能为null 不能为空
        if (skuMap == null || skuMap.isEmpty()) {
            List<String> skuIds = pushDTO.getDetailList().stream().map(KolSubB2cApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
            List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
            skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        }

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
        //省市区的映射
        List<String> sysIds = Arrays.asList(kolB2cApplicationAddressEntity.getProvinceId(), kolB2cApplicationAddressEntity.getCityId(), kolB2cApplicationAddressEntity.getDistrictId())
                .stream()
                .filter(StringUtils::isNotBlank)
                .filter(id -> !DictCityConstants.isNoDistrictId(id))
                .collect(Collectors.toList());
        DmpThirdCityDTO.SysAddressParamsDTO dto = new DmpThirdCityDTO.SysAddressParamsDTO();
        dto.setSourcePlatform(ThirdSysTypeEnum.WDT.getCode());
        dto.setSysIds(sysIds);
        List<DmpThirdCityDTO.ThirdAddressMappingDTO > thirdByAddress = DmpThirdCityFeign.getThirdByAddress(dto);
        Map<String,String> thirdAddressMap = thirdByAddress.stream().collect(Collectors.toMap(DmpThirdCityDTO.ThirdAddressMappingDTO::getSysId, DmpThirdCityDTO.ThirdAddressMappingDTO::getName, (o1, o2) -> o1));

        //wdt物流渠道映射
        String logisticsChannelId = kolB2cApplicationEntity.getLogisticsChannelId();
        String logisticsCode = logisticsChannelId;
        if(StringUtils.isNotBlank(logisticsChannelId)){
            List<ThirdMappingEntity> logisticsChannels = FeignQuery.create(ThirdMappingEntity.class)
                    .eq(ThirdMappingEntity::getType, "logistics")
                    .eq(ThirdMappingEntity::getThirdSysType, "wdt")
                    .eq(ThirdMappingEntity::getDisabled, false)
                    .eq(ThirdMappingEntity::getSysId, logisticsChannelId)
                    .list();
            if (CollUtil.isNotEmpty(logisticsChannels)) {
                logisticsCode = logisticsChannels.get(0).getThirdCode();
            }
        }
        //wdt店铺映射
        String shopId = kolB2cApplicationEntity.getShopId();
        String shopNo =shopId;
        if(StringUtils.isNotBlank(shopId)){
            List<ThirdMappingEntity> shops = FeignQuery.create(ThirdMappingEntity.class)
                    .eq(ThirdMappingEntity::getType, "shop")
                    .eq(ThirdMappingEntity::getThirdSysType, "wdt")
                    .eq(ThirdMappingEntity::getDisabled, false)
                    .eq(ThirdMappingEntity::getSysId, shopId)
                    .list();
            if(CollUtil.isNotEmpty(shops)){
                shopNo = shops.get(0).getThirdCode();
            }
        }
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
        rawTrade.setReceiverArea(Arrays.asList(
                        getAddressName(thirdAddressMap, kolB2cApplicationAddressEntity.getProvinceId(), kolB2cApplicationAddressEntity.getProvince()),
                        getAddressName(thirdAddressMap, kolB2cApplicationAddressEntity.getCityId(), kolB2cApplicationAddressEntity.getCity()),
                        getAddressName(thirdAddressMap, kolB2cApplicationAddressEntity.getDistrictId(), kolB2cApplicationAddressEntity.getDistrict()))
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" ")));
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
        return request;
    }

    private String getAddressName(Map<String, String> thirdAddressMap, String addressId, String fallbackName) {
        if (StringUtils.isNotBlank(addressId)) {
            String thirdName = thirdAddressMap.get(addressId);
            if (StringUtils.isNotBlank(thirdName)) {
                return thirdName;
            }
        }
        return fallbackName;
    }

}
