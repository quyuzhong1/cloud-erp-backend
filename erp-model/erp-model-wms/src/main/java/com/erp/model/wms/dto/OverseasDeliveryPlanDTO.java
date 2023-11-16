package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * 发货计划请求响应实体
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class OverseasDeliveryPlanDTO implements Serializable {


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
         private String tabFlag;

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
         * 搜索类型
         */
         private String tabFlag;

         /**
         * 单号
         */
         private String code;

         /**
         * 产品编号
         */
         private String skuNoList;

         /**
         * 审核状态
         */
         private List<String> approveStatusList;

         /**
         * 发货状态
         */
         private List<String> deliveryStatusList;

         /**
         * 发货单号
         */
         private String deliveryCode;

         /**
         * 目的仓库
         */
         private List<String> toWarehouseIdList;

         /**
         * 国家
         */
         private List<String> countryList;

         /**
         * 是否组合品
         */
         private Boolean isCombination;

         /**
         * 操作人
         */
         private List<String> updateUserIdList;

         /**
         * 创建时间
         */
         private List<LocalDate> createTimeList;

         /**
         * 审核时间
         */
         private List<LocalDate> approveTimeList;
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
         * 编号
         */
        private String code;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态中文名
         */
        private String approveStatusName;

        /**
         * 发货状态
         */
        private String deliveryStatus;

        /**
         * 发货状态中文名
         */
        private String deliveryStatusName;

        /**
         * 发货单号
         */
        private String deliveryCode;

        /**
         * 目的仓库
         */
        private String toWarehouseId;

        /**
         * 目的仓库中文名
         */
        private String toWarehouseName;

        /**
         * 国家
         */
        private String countryId;

        /**
         * 国家中文名
         */
        private String countryName;

        /**
         * 产品id
         */
        private String skuId;

        /**
         * 产品编号
         */
        private String skuNo;

        /**
         * 是否组合品
         */
        private String isCombination;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 计划数量
         */
        private Integer planQty;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人中文名
         */
        private String createUserName;

        /**
         * 待审核人名称
         */
        private String waitApproveUserName;

        /**
         * 审核人id
         */
        private String approveUserId;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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
        * code
        */
        private String code;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 审核人
        */
        private String approveUserId;

        /**
        * 审核人中文名
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 发货状态
        */
        private String deliveryStatus;

        /**
        * 目的仓id
        */
        private String toWarehouseId;

        /**
        * 目的仓中文名
        */
        private String toWarehouseName;

        /**
        * 国家二字码
        */
        private String country;

        /**
        * 国家中文名
        */
        private String countryName;

        /**
        * 计划发货时间
        */
        private LocalDate planDeliveryDate;

        /**
        * 描述
        */
        private String remark;


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
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 发货状态
        */
        @NotBlank(message = "发货状态不能为空")
        @Size(max = 50,message = "发货状态最大长度不能超过50位")
        private String deliveryStatus;

        /**
        * 目的仓id
        */
        @NotBlank(message = "目的仓id不能为空")
        @Size(max = 19,message = "目的仓id最大长度不能超过19位")
        private String toWarehouseId;

        /**
        * 目的仓中文名
        */
        @NotBlank(message = "目的仓中文名不能为空")
        @Size(max = 255,message = "目的仓中文名最大长度不能超过255位")
        private String toWarehouseName;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 19,message = "国家二字码最大长度不能超过19位")
        private String country;

        /**
        * 国家中文名
        */
        @NotBlank(message = "国家中文名不能为空")
        @Size(max = 255,message = "国家中文名最大长度不能超过255位")
        private String countryName;

        /**
        * 计划发货时间
        */
        private LocalDate planDeliveryDate;

        /**
        * 描述
        */
        @NotBlank(message = "描述不能为空")
        private String remark;


    }


}