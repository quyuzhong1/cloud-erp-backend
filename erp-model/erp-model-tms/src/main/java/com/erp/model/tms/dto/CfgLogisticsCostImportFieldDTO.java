package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.erp.model.sys.dto.CfgQueryConditionDTO;
import com.erp.model.sys.dto.CfgQueryOptionDTO;
import com.erp.model.sys.dto.DepartmentDTO;
import com.erp.model.sys.entity.CfgQueryOptionEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 费用项配置字段基础表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-01-20
*/
@Data
@NoArgsConstructor
public class CfgLogisticsCostImportFieldDTO implements Serializable {


    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;
        /**
        * 父id
        */
        private String parentId;

        /**
        * ERP字段
        */
        private String field;

        /**
        * ERP字段名称
        */
        private String fieldName;

        /**
        * ERP字段类型
        */
        private String fieldType;
        /**
        * ERP字段类型名称
        */
        private String fieldTypeName;
        /**
         * 是否唯一字段
         */
        private Boolean isUniqueField;
        /**
         * 默认值下拉配置ID
         */
        private String queryOptionId;

        private CfgQueryOptionDTO.ViewDTO queryOptionItem;
        /**
         * 默认值静态选项
         */
        private String optionList;
        /**
         * 字段单位属性
         */
        private String unitType;
        /**
         * 标准单位
         */
        private String standardUnit;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class TreeDTO {

        /**
         * 主键id
         */
        private String  id;
        /**
         * 父id
         */
        private String parentId;

        /**
         * ERP字段
         */
        private String field;

        /**
         * ERP字段名称
         */
        private String fieldName;

        /**
         * ERP字段类型
         */
        private String fieldType;
        /**
         * ERP字段类型名称
         */
        private String fieldTypeName;
        /**
         * 路径
         */
        private String path;
        /**
         * 默认值下拉配置ID
         */
        private String queryOptionId;
        /**
         * 默认值静态选项
         */
        private String optionList;
        /**
         * 字段单位属性
         */
        private String unitType;
        /**
         * 标准单位
         */
        private String standardUnit;

        @JsonInclude(value= JsonInclude.Include.NON_NULL)
        private List<TreeDTO> childrenList;
    }


}
