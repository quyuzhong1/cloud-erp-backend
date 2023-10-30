package com.erp.model.wms.dto;

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
 * FBI发货单请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@NoArgsConstructor
public class FbaDeliveryDTO implements Serializable {


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
         private String  tabFlag;

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
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人
        */
        private String approveUserName;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 备货类型
        */
        private String demandType;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 国家二字码
        */
        private String countryId;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 发货仓id
        */
        private String deliveryWarehouseId;

        /**
        * 发货仓名称
        */
        private String deliveryWarehouseName;

        /**
        * 目的仓id
        */
        private String destWarehouseId;

        /**
        * 目的仓名称
        */
        private String destWarehouseName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 平台物流中心
        */
        private String fulfillmentCenter;

        /**
        * 库存组织id
        */
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        private String inventoryOrgName;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;
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
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人
        */
        private String approveUserName;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 备货类型
        */
        private String demandType;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 国家二字码
        */
        private String countryId;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 发货仓id
        */
        private String deliveryWarehouseId;

        /**
        * 发货仓名称
        */
        private String deliveryWarehouseName;

        /**
        * 目的仓id
        */
        private String destWarehouseId;

        /**
        * 目的仓名称
        */
        private String destWarehouseName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 平台物流中心
        */
        private String fulfillmentCenter;

        /**
        * 库存组织id
        */
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        private String inventoryOrgName;


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
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 来源编码
        */
        @NotBlank(message = "来源编码不能为空")
        @Size(max = 50,message = "来源编码最大长度不能超过50位")
        private String sourceCode;

        /**
        * 备货类型
        */
        @NotBlank(message = "备货类型不能为空")
        @Size(max = 64,message = "备货类型最大长度不能超过64位")
        private String demandType;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 64,message = "店铺id最大长度不能超过64位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255,message = "店铺名称最大长度不能超过255位")
        private String shopName;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 10,message = "国家二字码最大长度不能超过10位")
        private String countryId;

        /**
        * 国家名称
        */
        @NotBlank(message = "国家名称不能为空")
        @Size(max = 64,message = "国家名称最大长度不能超过64位")
        private String countryName;

        /**
        * 发货仓id
        */
        @NotBlank(message = "发货仓id不能为空")
        @Size(max = 19,message = "发货仓id最大长度不能超过19位")
        private String deliveryWarehouseId;

        /**
        * 发货仓名称
        */
        @NotBlank(message = "发货仓名称不能为空")
        @Size(max = 255,message = "发货仓名称最大长度不能超过255位")
        private String deliveryWarehouseName;

        /**
        * 目的仓id
        */
        @NotBlank(message = "目的仓id不能为空")
        @Size(max = 19,message = "目的仓id最大长度不能超过19位")
        private String destWarehouseId;

        /**
        * 目的仓名称
        */
        @NotBlank(message = "目的仓名称不能为空")
        @Size(max = 255,message = "目的仓名称最大长度不能超过255位")
        private String destWarehouseName;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 500,message = "备注最大长度不能超过500位")
        private String remark;

        /**
        * 平台物流中心
        */
        @NotBlank(message = "平台物流中心不能为空")
        @Size(max = 64,message = "平台物流中心最大长度不能超过64位")
        private String fulfillmentCenter;

        /**
        * 库存组织id
        */
        @NotBlank(message = "库存组织id不能为空")
        @Size(max = 19,message = "库存组织id最大长度不能超过19位")
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        @NotBlank(message = "库存组织名称不能为空")
        @Size(max = 255,message = "库存组织名称最大长度不能超过255位")
        private String inventoryOrgName;


    }


}