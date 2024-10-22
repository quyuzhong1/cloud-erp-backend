package com.erp.model.plm.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.util.List;

/**
 *
 */
@Getter
@Setter
public class PrintEanDTO {

    /**
     * 打印类型
     */
    private String printType;

    /**
     * 打印内容
     */
    @Size(min = 1, message = "至少选一个打印")
    private List<String> typeList;
    /**
     * sku数据
     */
    private List<PrintSkuEanDTO> printSkuEanList;


    /**
     * 打印sku参数
     */
    @Getter
    @Setter
    public static class PrintSkuEanDTO {

        /**
         * skuNo
         */
        private String skuNo;
        /**
         * ean
         */
        private String ean;
        /**
         * 数量
         */
        private Integer qty;
    }

}
