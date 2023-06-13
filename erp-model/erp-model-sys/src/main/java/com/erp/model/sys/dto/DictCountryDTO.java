package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname DictCountryDTO
 * @Description TODO
 * @Date 2023-05-11 16:11
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictCountryDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * id
         */
        private String id;

        /**
         * 中文名
         */
        private String nameCn;

        /**
         * 英文名
         */
        private String nameEn;

        /**
         * 中文简称
         */
        private String shortNameCn;

        /**
         * 英文简称
         */
        private String shortNameEn;

        /**
         * 大区code
         */
        private String regionCode;

        /**
         * 大区名称
         */
        private String areaName;

        /**
         * 是否禁用
         * false 没有
         */
        private Boolean disabled;

    }
}
