package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname CurrencyDTO
 * @Description TODO
 * @Date 2023-03-21 17:13
 * @Created by yl
 */
public class CurrencyDTO implements Serializable {


    /**
     * 货币
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class ViewDTO{

        private String id;


        /**
         * 名称
         */
        private String name;

        /**
         * 货币符号
         */
        private String symbol;


        /**
         * 是否禁用
         * true 禁用
         */
        private Boolean disabled;

    }
}
