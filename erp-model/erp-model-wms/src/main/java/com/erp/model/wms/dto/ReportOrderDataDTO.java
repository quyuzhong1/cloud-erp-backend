package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 订单报表信息请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-09-24
*/
@Data
@NoArgsConstructor
public class ReportOrderDataDTO implements Serializable {




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
        * 仓库id 
        */
        private String warehouseId;

        /**
        * 虚拟仓库id
        */
        private String virtualWarehouseId;

        /**
        * sku id
        */
        private String skuId;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 单据状态
        */
        private String status;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 来源单据id
        */
        private String sourceId;

        /**
        * 来源单据明细id
        */
        private String sourceDetailId;

        /**
        * 来源单据编号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 时间
        */
        private LocalDate date;


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
        * 仓库id 
        */
        @NotBlank(message = "仓库id 不能为空")
        @Size(max = 19,message = "仓库id 最大长度不能超过19位")
        private String warehouseId;

        /**
        * 虚拟仓库id
        */
        @NotBlank(message = "虚拟仓库id不能为空")
        @Size(max = 19,message = "虚拟仓库id最大长度不能超过19位")
        private String virtualWarehouseId;

        /**
        * sku id
        */
        @NotBlank(message = "sku id不能为空")
        @Size(max = 19,message = "sku id最大长度不能超过19位")
        private String skuId;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 单据状态
        */
        @NotBlank(message = "单据状态不能为空")
        @Size(max = 32,message = "单据状态最大长度不能超过32位")
        private String status;

        /**
        * 来源单据id
        */
        @NotBlank(message = "来源单据id不能为空")
        @Size(max = 19,message = "来源单据id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源单据明细id
        */
        @NotBlank(message = "来源单据明细id不能为空")
        @Size(max = 19,message = "来源单据明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 来源单据编号
        */
        @NotBlank(message = "来源单据编号不能为空")
        @Size(max = 32,message = "来源单据编号最大长度不能超过32位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 时间
        */
        private LocalDate date;


    }


}