package com.erp.model.scm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 供应商关联仓库表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-06-18
*/
@Data
@NoArgsConstructor
public class SupplierRefWarehouseDTO implements Serializable {

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private Boolean tabFlag;

        /**
         * 类型名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

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
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商编码
         */
        private String supplierCode;

        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 仓位编码
         */
        private String warehouseLocationCode;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 是否禁用
         */
        private Boolean disabled;
        /**
         * true禁用，false启用
         */
        private String disabledName;

        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 备注
         */
        private String remark;
    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 仓位编码
        */
        private String warehouseLocationCode;

        /**
        * 排序字段
        */
        private Integer index;

        /**
        * 备注
        */
        private String remark;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 供应商id
         */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
         * 供应商编码
         */
        private String supplierCode;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
         * 是否禁用
         */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
         * 仓位编码
         */
        private List<String> warehouseLocationCodeList;

    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 供应商id
         */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
         * 供应商编码
         */
        private String supplierCode;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
         * 是否禁用
         */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
         * 仓位编码
         */
        @Size(max = 64,message = "仓位编码最大长度不能超过64位")
        private String warehouseLocationCode;
    }


    /**
     * 禁用DTO
     */
    @Data
    @NoArgsConstructor
    public static class DisabledDTO {
        /**
         * 主键ids
         */
        @NotEmpty(message = "主键id不能为空")
        private List<String> ids;

        /**
         * 禁用状态，true禁用，false启用
         */
        @NotNull(message = "禁用状态不能为空")
        private Boolean disabled;
    }
}