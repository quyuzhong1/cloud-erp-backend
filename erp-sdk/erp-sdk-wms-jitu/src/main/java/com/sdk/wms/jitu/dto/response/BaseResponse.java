package com.sdk.wms.jitu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName BaseResponse
 * @description: TODO
 * @date 2026年04月20日
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse implements Serializable {
    private String logisticproviderid;
    private String traceId;
}
