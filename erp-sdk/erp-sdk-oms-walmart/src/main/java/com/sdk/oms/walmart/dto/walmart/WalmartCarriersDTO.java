package com.sdk.oms.walmart.dto.walmart;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 沃尔玛物流渠道
 * @Author Jim
 * @Date 2023/12/25
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class WalmartCarriersDTO {

    private List<Carrier> carriers;



    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Carrier {

        private String carrierId;

        private String shortName;

        private String carrierName;
    }
}
