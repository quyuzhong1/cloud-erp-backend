package com.erp.model.sys.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname DictCityDTO

 * @Date 2023-05-11 15:19
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictCityDTO implements Serializable {

    /**
     * 添加省
     */
    @Data
    @NoArgsConstructor
    public static class AddProvinceDTO {
        /**
         * 国家id 来源 http://172.16.100.11:3002/project/36/interface/api/13390
         */
        @NotBlank(message = "国家不能为空")
        private String parentId;

        /**
         * 省份名称
         */
        @NotBlank(message = "省份名称不能为空")
        private String name;

        /**
         * 省份code 必填
         */
        @NotBlank(message = "省份code不能为空")
        private String code;
    }


    /**
     * 添加省
     */
    @Data
    @NoArgsConstructor
    public static class AddCityDTO {
        /**
         * 省id 来源 http://172.16.100.11:3002/project/36/interface/api/31307
         */
        @NotBlank(message = "省不能为空")
        private String parentId;

        /**
         * 城市名称
         */
        @NotBlank(message = "城市名称不能为空")
        private String name;

        /**
         * 城市code 必填
         */
        @NotBlank(message = "城市code不能为空")
        private String code;
    }

    /**
     * 修改市
     */
    @Data
    @NoArgsConstructor
    public static class UpdateCityDTO {

        /**
         * id
         */
        @NotBlank(message = "城市不能为空")
        private String id;

        /**
         * 省id 来源 http://172.16.100.11:3002/project/36/interface/api/31307
         */
        @NotBlank(message = "省不能为空")
        private String parentId;

        /**
         * 城市名
         */
        @NotBlank(message = "城市名称不能为空")
        private String name;

    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO{
        /**
         * id
         */
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * code
         */
        private String code;




        /**
         * 上级名称
         */
        private String parentName;

        /**
         * 上级名称
         */
        private String parentId;


    }
    /**
     * 修改省
     */
    @Data
    @NoArgsConstructor
    public static class UpdateProvinceDTO {

        /**
         * id
         */
        @NotBlank(message = "省份不能为空")
        private String id;
        /**
         * 国家id 来源 http://172.16.100.11:3002/project/36/interface/api/13390
         */
        @NotBlank(message = "国家不能为空")
        private String parentId;

        /**
         * 省份名称
         */
        @NotBlank(message = "省份名称不能为空")
        private String name;

    }




    /**
     * 分页响应
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO{

        /**
         * id
         */
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * 金蝶编码
         */
        private String kingdeeCode;


        /**
         * 上级名称
         */
        private String parentName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 更新人
         */
        private String updateUserName;

    }

    /**
     * 省份参数
     */
    @Data
    @NoArgsConstructor
    public static class ProvincePagingParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    /**
     * 省份参数
     */
    @Data
    @NoArgsConstructor
    public static class CityPagingParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

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
        /**
         * 主键id
         */
        private String id;
        /**
         * 名称
         */
        private String name;
        /**
         * 是否禁用
         */
        private Boolean disabled;
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
