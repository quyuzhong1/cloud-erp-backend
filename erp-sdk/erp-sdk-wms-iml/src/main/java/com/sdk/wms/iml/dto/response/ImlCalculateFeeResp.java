package com.sdk.wms.iml.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class ImlCalculateFeeResp {


    @JSONField(name = "currencyCode")
    private String currencyCode;
    @JSONField(name = "currencyDesc")
    private String currencyDesc;
    @JSONField(name = "amount")
    private Integer amount;
    @JSONField(name = "feeDetails")
    private List<FeeDetailsDTO> feeDetails;
    @JSONField(name = "isCheckSuccess")
    private Boolean isCheckSuccess;
    @JSONField(name = "errorMsg")
    private String errorMsg;

    @NoArgsConstructor
    @Data
    public static class FeeDetailsDTO {
        @JSONField(name = "feeCode")
        private String feeCode;
        @JSONField(name = "feeName")
        private String feeName;
        @JSONField(name = "feeAmount")
        private Integer feeAmount;
        @JSONField(name = "remark")
        private String remark;
    }
}
