package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Brand
 * @description: TODO
 * @date 2023年10月19日
 * @version: 1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class TaxInfo implements Serializable {
    @Alias( "ncm")
    private String ncm;
    @Alias( "diff_state_cfop")
    private String diffStateCfop;

    @Alias( "csosn")
    private String csosn;

    @Alias( "origin")
    private String origin;

    @Alias( "cest")
    private String cest;

    @Alias( "measure_unit")
    private String measureUnit;

    @Alias( "invoice_option")
    private String invoiceOption;

    @Alias( "vat_rate")
    private String vatRate;

    @Alias( "hs_code")
    private String hsCode;

    @Alias( "tax_code")
    private String taxCode;
    @Alias( "tax_type")
    private int taxType;

}
