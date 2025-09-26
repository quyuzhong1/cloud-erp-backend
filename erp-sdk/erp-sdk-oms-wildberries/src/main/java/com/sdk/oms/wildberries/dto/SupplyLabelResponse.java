package com.sdk.oms.wildberries.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author zdy
 * @ClassName OrderRequest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SupplyLabelResponse extends BaseResponse{

    /**
     * {
     *   "barcode": "WB-GI-12345678",
     *   "file": "U3dhZ2dlciByb2Nrcw=="
     * }
     */

    private String barcode;
    private String file;
}
