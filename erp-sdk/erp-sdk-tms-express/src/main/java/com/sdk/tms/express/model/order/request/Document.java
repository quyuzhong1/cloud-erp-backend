package com.sdk.tms.express.model.order.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Document
 * @description: TODO
 * @date 2023年11月13日
 * @version: 1.0
 */
@Data
@Builder
public class Document implements Serializable {
    private String masterWaybillNo;
    private String backWaybillNo;
    private String seq;
    private String sum;
}
