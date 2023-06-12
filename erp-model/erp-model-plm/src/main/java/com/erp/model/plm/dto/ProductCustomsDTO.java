package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProductCustomsDTO {
    /**
     * id
     */
    private String id;

    /**
     * 国家
     */
    private String country;

    /**
     * 海关编码
     */
    private String customsCode;

    /**
     * 税率
     */
    private BigDecimal taxRate;

    /**
     * 海关类型：出关 exitCustoms 清关 clearanceCustoms 默认：清关
     * 获取地址：plm/common/enumDropDown?type=CustomsType
     */
    private String type;
}
