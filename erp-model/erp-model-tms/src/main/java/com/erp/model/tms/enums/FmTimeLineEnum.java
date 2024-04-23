package com.erp.model.tms.enums;

import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 头程物流时间线
 */

@Getter
public enum FmTimeLineEnum  {
    APPROVE("发货审核时间", TmsFirstMileLogisticDTO.TimeInfoDTO::getApproveTime),
    LOGISTIC("物流下单时间",TmsFirstMileLogisticDTO.TimeInfoDTO::getLogisticOrderTime),
    SHIP("开船时间",TmsFirstMileLogisticDTO.TimeInfoDTO::getShipTime),
    TRACKING("运输时间",TmsFirstMileLogisticDTO.TimeInfoDTO::getTrackingTime),
    ARRIVED("到达时间",TmsFirstMileLogisticDTO.TimeInfoDTO::getArrivedTime),
    SIGN("签收时间",TmsFirstMileLogisticDTO.TimeInfoDTO::getSignTime),
    ;


    /**
     * 类型
     */
    private String code;

    private final Function<TmsFirstMileLogisticDTO.TimeInfoDTO, LocalDateTime> timeMethod;

    FmTimeLineEnum(String code, Function<TmsFirstMileLogisticDTO.TimeInfoDTO, LocalDateTime> timeMethod){
        this.code = code;
        this.timeMethod = timeMethod;
    }

    public static List<TmsFirstMileLogisticDTO.TimeLine> convertToViewList(TmsFirstMileLogisticDTO.TimeInfoDTO timeInfoDTO){
        List<TmsFirstMileLogisticDTO.TimeLine> viewDetailVOList = new ArrayList<>();
        for(FmTimeLineEnum timeLineEnum : FmTimeLineEnum.values()){
            TmsFirstMileLogisticDTO.TimeLine viewDetailVO = new TmsFirstMileLogisticDTO.TimeLine();
            viewDetailVO.setTimeName(timeLineEnum.getCode());
            viewDetailVO.setTime(timeLineEnum.getTimeMethod().apply(timeInfoDTO));
            viewDetailVOList.add(viewDetailVO);
        }
        return viewDetailVOList;
    }
}
