package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 虚拟仓分货单请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
@Data
@NoArgsConstructor
public class VirtualWarehouseAllocationDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 是否失效 true 失效 false 未失效
         */
        private Boolean disabled;

        /**
         * code
         */
        private String code;

        /**
         * 类型：0新增分货，1虚拟仓调拨，2取消分货
         */
        private String type;

        /**
         * 状态 ：0待提交 1已处理 2已作废
         */
        private String status;

        /**
         * 备注
         */
        private String remark;

        /**
         * 方向
         */
        private Integer direction;

        /**
         * 作废说明
         */
        private String invalidDescription;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 是否失效 true 失效 false 未失效
         */
        @NotNull(message = "是否失效 true 失效 false 未失效不能为空")
        private Boolean disabled;

        /**
         * 类型：0新增分货，1虚拟仓调拨，2取消分货
         */
        @NotBlank(message = "类型：0新增分货，1虚拟仓调拨，2取消分货不能为空")
        @Size(max = 10, message = "类型：0新增分货，1虚拟仓调拨，2取消分货最大长度不能超过10位")
        private String type;

        /**
         * 状态 ：0待提交 1已处理 2已作废
         */
        @NotBlank(message = "状态 ：0待提交 1已处理 2已作废不能为空")
        @Size(max = 20, message = "状态 ：0待提交 1已处理 2已作废最大长度不能超过20位")
        private String status;

        /**
         * 备注
         */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200, message = "备注最大长度不能超过200位")
        private String remark;

        /**
         * 方向
         */
        @NotNull(message = "方向不能为空")
        private Integer direction;

        /**
         * 作废说明
         */
        @NotBlank(message = "作废说明不能为空")
        @Size(max = 255, message = "作废说明最大长度不能超过255位")
        private String invalidDescription;


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
        private Map<String, String> sqlMap;

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键
         */
        private String id;
        /**
         * 明细主键
         */
        private String detailId;
        /**
         * code
         */
        private String code;
        /**
         * 状态 ：0待提交 1已处理 2已作废
         */
        private String status;
        /**
         * 状态 ：0待提交 1已处理 2已作废
         */
        private String statusName;
        /**
         * 类型：0新增分货，1虚拟仓调拨，2取消分货
         */
        private String type;
        /**
         * 类型：0新增分货，1虚拟仓调拨，2取消分货
         */
        private String typeName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;
        /**
         * sku名称
         */
        private String skuName;
        /**
         * sku编号
         */
        private String imageUrl;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 调出虚拟仓id
         */
        private String fromVirtualWarehouseId;

        /**
         * 调出虚拟仓名称
         */
        private String fromVirtualWarehouseName;

        /**
         * 调出数量/调拨数量
         */
        private Integer qty;

        /**
         * 调入虚拟仓id
         */
        private String toVirtualWarehouseId;

        /**
         * 调出入虚拟仓名称
         */
        private String toVirtualWarehouseName;

        /**
         * 完结说明
         */
        private String finishDescription;
        /**
         * 作废说明
         */
        private String invalidDescription;
        /**
         * 第三方单据单号
         */
        private String thirdCode;

    }

}