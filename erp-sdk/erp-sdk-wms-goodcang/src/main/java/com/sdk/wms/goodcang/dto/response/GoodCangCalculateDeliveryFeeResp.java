package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class GoodCangCalculateDeliveryFeeResp extends CleanBaseDTO implements Serializable {

    //物流产品代码
    @JSONField(name = "sm_code")
    private String smCode;

    //物流产品的英文名称
    @JSONField(name = "sm_name")
    private String smName;

    //物流产品的中文名称
    @JSONField(name = "sm_name_cn")
    private String smNameCn;

    //最慢时效
    @JSONField(name = "sm_delivery_time_min")
    private Integer smDeliveryTimeMin;

    //最快时效
    @JSONField(name = "sm_delivery_time_max")
    private Integer smDeliveryTimeMax;

    //总费用
    @JSONField(name = "total")
    private String total;

    //明细
    @JSONField(name = "income")
    private List<GoodCangCalculateDeliveryFeeResp.Income> income;

    @EqualsAndHashCode(callSuper = true)
    @Data
    @ToString
    public static class Income extends CleanBaseDTO {

        //名称
        @JSONField(name = "name")
        private String name;

        //费用
        @JSONField(name = "amount")
        private String amount;
    }
}
