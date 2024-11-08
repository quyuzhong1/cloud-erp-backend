package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 分货单拆单主表请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
*/
@Data
@NoArgsConstructor
public class VirtualWarehousePushHandleDTO implements Serializable {




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
        * 分货单id
        */
        private String allocationId;

        /**
        * 编号
        */
        private String allocationCode;

        /**
        * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
         * CostAllocationEnum
        */
        private String allocationType;

        /**
        * 状态 ：waitSubmit待提交 handle已处理 invalid已作废
        */
        private String allocationStatus;

        /**
        * 方向
        */
        private Integer allocationDirection;


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
        * 分货单id
        */
        @NotBlank(message = "分货单id不能为空")
        @Size(max = 19,message = "分货单id最大长度不能超过19位")
        private String allocationId;

        /**
        * 编号
        */
        @NotBlank(message = "编号不能为空")
        @Size(max = 30,message = "编号最大长度不能超过30位")
        private String allocationCode;

        /**
        * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
         * CostAllocationEnum
        */
        @NotBlank(message = "类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货不能为空")
        @Size(max = 10,message = "类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货最大长度不能超过10位")
        private String allocationType;

        /**
        * 状态 ：waitSubmit待提交 handle已处理 invalid已作废
        */
        @NotBlank(message = "状态 ：waitSubmit待提交 handle已处理 invalid已作废不能为空")
        @Size(max = 20,message = "状态 ：waitSubmit待提交 handle已处理 invalid已作废最大长度不能超过20位")
        private String allocationStatus;

        /**
        * 方向
        */
        @NotNull(message = "方向不能为空")
        private Integer allocationDirection;


    }


}