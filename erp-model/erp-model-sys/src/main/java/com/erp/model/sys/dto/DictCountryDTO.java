package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname DictCountryDTO

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

        /**
         * 子区域code
         */
        private String subregionCode;

        /**
         * 子区域名称
         */
        private String subregionName;

    }

    @Data
    @NoArgsConstructor
    public static class CascadeDTO{

        private String dictAreaCode;

        private List<ChildrenDTO> children;

    }

    @Data
    @NoArgsConstructor
    public static class ChildrenDTO{

        private String dictCountryCode;

        private String dictCountryName;

    }

    @Data
    @NoArgsConstructor
    public static class ListParamDTO{

        /**
         * 搜索关键字
         */
        private String searchKeyword;

        /**
         * 区域编码
         */
        private String regionCode;

        /**
         * 国家中文名称
         */
        private List<String> nameCnList;
    }

    @Data
    @NoArgsConstructor
    public static class ListRegionDTO{

        /**
         * 区域编码
         */
        private String regionCode;

        /**
         * 区域名称
         */
        private String regionName;

        /**
         * 国家
         */
        private List<ListDTO> list;


        public ListRegionDTO (String regionCode,String regionName) {
            this.regionCode = regionCode;
            this.regionName = regionName;
        }
    }
}
