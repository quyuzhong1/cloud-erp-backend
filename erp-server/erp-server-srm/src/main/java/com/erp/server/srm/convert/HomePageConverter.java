package com.erp.server.srm.convert;

import com.common.business.mapper.BooleanMapperWork;
import com.common.business.utils.StringUtil;
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

    @Mapping(target = "month", expression = "java(HomePageConverter.changeMonthToStr(purchase.getMonth()))")
    @Mapping(target = "orderAmount", source = "orderMoney")
    @Mapping(target = "orderSkuCount", source = "orderSkuCount")
    HomePageDTO.OrderTrend orderTrendConvert(PurchaseStatisticsDTO.StatisticsMonthDTO purchase);
    List<HomePageDTO.OrderTrend> orderTrendConvert(List<PurchaseStatisticsDTO.StatisticsMonthDTO> purchaseList);

    @Mapping(target = "month", expression = "java(HomePageConverter.changeMonthToStr(returnList.getMonth()))")
    @Mapping(target = "refundSkuCount", source = "returnSkuCount")
    @Mapping(target = "qcRefundSkuCount", source = "qcReturnSkuCount")
    HomePageDTO.RefundTrend refundTrendConvert(PurchaseReturnStatisticsDTO.StatisticsMonthDTO returnList);
    List<HomePageDTO.RefundTrend> refundTrendConvert(List<PurchaseReturnStatisticsDTO.StatisticsMonthDTO> returnList);

    static String changeMonthToStr(Integer month){
        if(month == null){
            return "";
        }
        if (month == 1) {
            return "一月";
        }
        if (month == 2) {
            return "二月";
        }
        if (month == 3) {
            return "三月";
        }
        if (month == 4) {
            return "四月";
        }
        if (month == 5) {
            return "五月";
        }
        if (month == 6) {
            return "六月";
        }
        if (month == 7) {
            return "七月";
        }
        if (month == 8) {
            return "八月";
        }
        if (month == 9) {
            return "九月";
        }
        if (month == 10) {
            return "十月";
        }
        if (month == 11) {
            return "十一月";
        }
        if (month == 12) {
            return "十二月";
        }

        return String.valueOf(month);
    }
}
