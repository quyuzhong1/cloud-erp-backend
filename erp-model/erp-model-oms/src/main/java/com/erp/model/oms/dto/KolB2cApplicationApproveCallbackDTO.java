package com.erp.model.oms.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * B2C KOL approve push callback payload.
 */
@Data
public class KolB2cApplicationApproveCallbackDTO implements Serializable {

    private String subOrderId;

    private String subOrderCode;

    private String syncTaskId;

    private String responseMsg;
}
