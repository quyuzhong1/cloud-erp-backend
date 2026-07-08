package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.sdk.oms.magalu.dto.MagaluShopInfoDTO;
import com.sdk.oms.magalu.service.MagaluService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.MAGALU)
public class MagaluShipOrder extends AbstractShipOrder {

    private static final int DEFAULT_ESTIMATED_DELIVERY_HOURS = 72;
    private static final String MAGALU_DELIVERY_SEPARATOR = "_";

    @Resource
    private MagaluService magaluService;
    @Resource
    private LogisticsFeign logisticsFeign;

    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        Tuple tuple = super.allSourceOrderInfo(dto);
        List<SoB2cEntity> sourceOrderList = tuple.get(0);
        Map<String, List<SoB2cDetailEntity>> detailMap = tuple.get(1);
        SoB2cLogisticsEntity logisticsEntity = tuple.get(2);

        List<String> shippedDetailIds = new ArrayList<>();
        for (SoB2cEntity entity : sourceOrderList) {
            List<SoB2cDetailEntity> detailList = detailMap.get(entity.getId());
            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException("Magalu标发失败，销售订单明细不存在");
            }
            detailList = detailList.stream()
                    .filter(detail -> CharSequenceUtil.isNotBlank(detail.getPlatformPackageId())
                            || CharSequenceUtil.isNotBlank(detail.getSourceDetailId()))
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(detailList)) {
                log.warn("Magalu标发跳过，订单{}没有可标发的平台明细", entity.getCode());
                continue;
            }

            String deliveryId = getDeliveryId(entity, detailList, logisticsEntity);
            String trackingNo = firstNotBlank(logisticsEntity.getTrackNo(), logisticsEntity.getCode());
            if (CharSequenceUtil.isBlank(trackingNo)) {
                throw new ServiceException("Magalu标发失败，物流跟踪号不能为空");
            }

            LocalDateTime shippedTime = dto.isFalseDeliveryFlag()
                    ? LocalDateTime.now()
                    : (logisticsEntity.getDeliveryTime() == null ? LocalDateTime.now() : logisticsEntity.getDeliveryTime());
            String shippedAt = formatUtc(shippedTime);
            String estimatedDeliveryAt = formatUtc(calculateEstimatedDeliveryTime(shippedTime, logisticsEntity.getLogisticsChannelId()));

            MagaluShopInfoDTO shopInfoDTO = magaluService.getShopInfoByShopId(entity.getShopId());
            if (shopInfoDTO == null || CharSequenceUtil.isBlank(shopInfoDTO.getAccessToken())) {
                throw new ServiceException("Magalu店铺授权信息不存在");
            }
            if (CharSequenceUtil.isBlank(shopInfoDTO.getChannelId())) {
                throw new ServiceException("Magalu渠道ID未配置");
            }

            magaluService.markDeliveryShipped(shopInfoDTO, deliveryId, shippedAt, estimatedDeliveryAt, trackingNo);
            shippedDetailIds.addAll(detailList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
        }
        return shippedDetailIds;
    }

    private String getDeliveryId(SoB2cEntity entity, List<SoB2cDetailEntity> detailList, SoB2cLogisticsEntity logisticsEntity) {
        if (logisticsEntity != null && CharSequenceUtil.isNotBlank(logisticsEntity.getPlanPackageNo())) {
            return logisticsEntity.getPlanPackageNo();
        }
        String platformPackageId = detailList.stream()
                .map(SoB2cDetailEntity::getPlatformPackageId)
                .filter(CharSequenceUtil::isNotBlank)
                .findFirst()
                .orElse("");
        if (CharSequenceUtil.isNotBlank(platformPackageId)) {
            return platformPackageId;
        }
        String thirdCode = entity.getThirdCode();
        String platformCode = entity.getPlatformCode();
        if (CharSequenceUtil.isNotBlank(thirdCode) && CharSequenceUtil.isNotBlank(platformCode)) {
            String prefix = platformCode + MAGALU_DELIVERY_SEPARATOR;
            if (thirdCode.startsWith(prefix) && thirdCode.length() > prefix.length()) {
                return thirdCode.substring(prefix.length());
            }
        }
        throw new ServiceException("Magalu标发失败，delivery.id不能为空");
    }

    private LocalDateTime calculateEstimatedDeliveryTime(LocalDateTime shippedTime, String logisticsChannelId) {
        int defaultHours = DEFAULT_ESTIMATED_DELIVERY_HOURS;
        if (CharSequenceUtil.isBlank(logisticsChannelId)) {
            return shippedTime.plusHours(defaultHours);
        }
        LogisticsChannelEntity channel = logisticsFeign.getChannelById(logisticsChannelId);
        if (channel == null || CharSequenceUtil.isBlank(channel.getEffectiveTime())) {
            return shippedTime.plusHours(defaultHours);
        }
        Integer duration = parseEffectiveTime(channel.getEffectiveTime());
        if (duration == null) {
            return shippedTime.plusHours(defaultHours);
        }
        String unit = CharSequenceUtil.blankToDefault(channel.getEffectiveTimeUnit(), "day");
        if ("hour".equalsIgnoreCase(unit) || "hours".equalsIgnoreCase(unit)) {
            return shippedTime.plusHours(duration);
        }
        return shippedTime.plusDays(duration);
    }

    private Integer parseEffectiveTime(String effectiveTime) {
        Matcher matcher = Pattern.compile("(\\d+)").matcher(effectiveTime);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }

    private String formatUtc(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (CharSequenceUtil.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        return null;
    }

    @Override
    public Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto) {
        return null;
    }

    @Override
    public Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList) {
        return null;
    }
}
