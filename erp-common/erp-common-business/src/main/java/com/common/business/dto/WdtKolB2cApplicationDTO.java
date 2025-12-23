package com.common.business.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@ToString
public class WdtKolB2cApplicationDTO {

    private String code;

    private String platformOrderId;

    private String platformOrderCode;

    private String platformSoCode;

    private String trackNo;

    private String deliveryStatus;

    private String orderStatus;

    private List<WdtKolB2cApplicationDetailDTO> details;


    @Data
    public static class WdtKolB2cApplicationDetailDTO {

        private String skuNo;

        private String platformDetailId;

        private String thirdDetailId;

        private Integer applyQty;
    }


}
