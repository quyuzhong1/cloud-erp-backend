package com.erp.server.srm.convert;

import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.scm.dto.PurchaseStatisticsDTO;
import com.erp.model.srm.dto.HomePageDTO;
import com.erp.model.srm.dto.OrderAcceptDTO;
import com.erp.model.srm.dto.ReturnConfirmDTO;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.erp.model.wms.dto.PurchaseReturnStatisticsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper()
public interface HomePageConverter {
    HomePageConverter INSTANCE = Mappers.getMapper(HomePageConverter.class);

    @Mapping(target = "month", source = "month")
    @Mapping(target = "orderAmount", source = "orderMoney")
    @Mapping(target = "orderSkuCount", source = "orderSkuCount")
    HomePageDTO.OrderTrend orderTrendConvert(PurchaseStatisticsDTO.StatisticsMonthDTO purchase);
    List<HomePageDTO.OrderTrend> orderTrendConvert(List<PurchaseStatisticsDTO.StatisticsMonthDTO> purchaseList);

    @Mapping(target = "month", source = "month")
    @Mapping(target = "refundSkuCount", source = "returnSkuCount")
    @Mapping(target = "qcRefundSkuCount", source = "qcReturnSkuCount")
    HomePageDTO.RefundTrend refundTrendConvert(PurchaseReturnStatisticsDTO.StatisticsMonthDTO returnList);
    List<HomePageDTO.RefundTrend> refundTrendConvert(List<PurchaseReturnStatisticsDTO.StatisticsMonthDTO> returnList);
}
