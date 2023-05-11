package com.erp.model.sys.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Lambda
 * @Classname DictCityDTO
 * @Description TODO
 * @Date 2023-05-11 15:19
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictCityDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO {

        private String id;
        /**
         * 名称
         */
        private String name;

        /**
         * 国家二字码
         */
        private String countryCode;

        /**
         * 时区
         */
        private String timezone;

        /**
         * 邮编
         */

        private String zipCode;

        /**
         * 经度
         */

        private BigDecimal latitude;

        /**
         * 维度
         */

        private BigDecimal longitude;

        /**
         * 上级城市id
         */

        private String parentId;

        /**
         * 等级
         */
        private Integer level;

        /**
         * 城市类型
         */

        private String type;

        /**
         * 排序
         */
        private Integer index;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 名称
         */
        private String name;

        /**
         * 国家二字码
         */
        private String countryCode;

        /**
         * 时区
         */
        private String timezone;

        /**
         * 邮编
         */

        private String zipCode;

        /**
         * 经度
         */

        private BigDecimal latitude;

        /**
         * 维度
         */

        private BigDecimal longitude;

        /**
         * 上级城市id
         */

        private String parentId;

        /**
         * 等级
         */
        private Integer level;

        /**
         * 城市类型
         */

        private String type;

        /**
         * 排序
         */
        private Integer index;


        /**
         * 子
         */
        List<AddDTO> childrenList;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {

        private String id;

        private String name;

        /**
         * 父级id
         */
        private String parentId;

        /**
         * 子集
         */
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        List<ListDTO> childrenList;
    }
}
