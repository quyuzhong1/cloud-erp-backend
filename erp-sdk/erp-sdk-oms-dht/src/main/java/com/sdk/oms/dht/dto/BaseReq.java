package com.sdk.oms.dht.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseReq {

    protected String corpAccessToken;

    private String corpId;

    private String currentOpenUserId;

    /**
     * 是否触发审批流,默认false
     */
    private Boolean triggerApprovalFlow = false;

    /**
     * 是否触发工作流,默认false
     */
    private Boolean triggerWorkFlow = false;

}
