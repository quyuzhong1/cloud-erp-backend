package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.anno.StateEnumValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 委外订单请求响应实体
 *
 * @author will
 * @since 2023-06-08
*/
@Data
@NoArgsConstructor
public class SubcontractOrderDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型 （toBeApprove 待审批 toBeCreate 待到货 created 已到货 reject 不通过）
         */
         private String searchType;

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
         private String  searchType;

         /**
          * 委外订单编号
          */
         private String code;

         /**
          * sku编码
          */
         private List<String> skuNoList;

         /**
          * 供应商id
          */
         private List<String> supplierIdList;

         /**
          * 审核状态（waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核）
          */
         private List<String> approveStatusList;

         /**
          * 作废状态（false未作废，true已作废）
          */
         private Boolean invalidStatus;

         /**
          * 到货状态（0未到货，1部分到货，2已到货）
          */
         private List<String> arrivalStatusList;

         /**
          * 是否加急（false否，true是）
          */
         private Boolean isUrgent;

         /**
          * 仓库id
          */
         private List<String> warehouseIdList;

         /**
          * 创建时间
          */
         private List<LocalDate> createTimeList;

         /**
          * 审核时间
          */
         private List<LocalDate> approveTimeList;

         /**
          * 申请人id
          */
         private List<String> purchaseUserIdList;

         /**
          * 创建人id
          */
         private List<String> createUserIdList;
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
         * 明细id
         */
        private String detailId;
        /**
         * 单据编号
         */
        private String code;
        /**
         * 来源编码
         */
        private String sourceCode;
        /**
         * 单据日期
         */
        private LocalDate billDate;
        /**
        * 审核状态
        */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * 到货状态
         */
        private String arrivalStatus;

        /**
         * 到货状态名称
         */
        private String arrivalStatusName;

        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
        * 产品名称
        */
        private String productName;

        /**
         * bom版本
         */
        private Integer bomVersion;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 领料数量
         */
        private Integer deliveryQty;

        /**
         * 已收货数量
         */
        private Integer receiveQty;

        /**
         * 审核名称
         */
        private String approveUserName;

        /**
         * 采购员
         */
        private String purchaserName;

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
    public static class ViewDTO extends CommonDTO{

        /**
         * 主键id
         */
        private String id;

        /**
         * 委外订单编号
         */
        private String code;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 收料组织名称
         */
        private String receiveOrgName;
        /**
         * 采购组织名称
         */
        private String purchaseOrgName;
        /**
         * 采购员名称
         */
        private String purchaserName;
        /**
         * 采购部门名称
         */
        private String deptName;
        /**
         * 委外组织名称
         */
        private String   subcontractOrgName;

        /**
         * 明细集合
         */
        private List<SubcontractOrderDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
      /**
       * 明细集合
       */
      private List<SubcontractOrderDetailDTO.AddDTO> detailList;

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

        /**
         * 明细集合
         */
        private List<SubcontractOrderDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 单据日期
         */
        private LocalDate billDate;
        /**
         * 收料组织id
         */
        private String  receiveOrgId;
        /**
         * 采购组织id
         */
        private String purchaseOrgId;
        /**
         * 采购员id
         */
        private String purchaserId;
        /**
         * 采购部门id
         */
        private String deptId;
        /**
         * 委外组织id
         */
        private String subcontractOrgId;
        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;
        /**
        * 来源id
        */
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;
        /**
        * 来源类型
        */
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;
        /**
        * 来源编码
        */
        @Size(max = 50,message = "来源编码最大长度不能超过50位")
        private String sourceCode;

    }

    @Data
    @NoArgsConstructor
    public static class ViewGeneratePoDTO {

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
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 采购组织名称
         */
        private String purchaseOrgName;

        /**
         * 收料组织id
         */
        private String receiveOrgId;

        /**
         * 收料组织名称
         */
        private String receiveOrgName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 最小起订量
         */
        private Integer moq;

        /**
         * 采购交期
         */
        private Integer deliveryDay;

        /**
         * 采购数量
         */
        private Integer qty;

        /**
         * 待申请数量
         */
        private Integer applyQty;
    }

    @Data
    @NoArgsConstructor
    public static class GeneratePoDTO {
        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19, message = "来源id最大长度不能超过19位")
        private String sourceId;
        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        @StateEnumValue(clazz = SourceTypeEnum.class, message = "单据来源错误")
        @Size(max = 32, message = "来源类型最大长度不能超过32位")
        private String sourceType;
        /**
         * 来源编码
         */
        @NotBlank(message = "来源编码不能为空")
        @Size(max = 50, message = "来源编码最大长度不能超过50位")
        private String sourceCode;

        /**
         * 来源明细id
         */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19, message = "来源id最大长度不能超过19位")
        private String sourceDetailId;

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 采购数量
         */
        @NotNull(message = "采购数量不能为空")
        @Min(value = 1, message = "采购数量最小值为1")
        @Max(value = 99999999, message = "采购数量最大值为99999999")
        private Integer qty;

        /**
         * 是否赠品
         */
        private Boolean isGift;

    }

    @Data
    @NoArgsConstructor
    public static class GeneratePoAddDTO extends GeneratePoDTO{

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 报关型号
         */
        private String declareModel;

        /**
         * 报关名称
         */
        private String declareName;

        /**
         * 仓库id
         */
        private String deliveryWarehouseId;

        /**
         * 是否是父级SKU
         */
        private Boolean isParent;

        /**
         * 采购员id
         */
        private String purchaseUserId;

        /**
         * 采购部门id
         */
        private String purchaseDeptId;

        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 收料组织id
         */
        private String receiveOrgId;


        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

    }


    @Data
    @NoArgsConstructor
    public static class ViewAddDetailParamDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "委外订单id不能为空")
        private String id;

        /**
         * sku编号集合
         */
        private List<String> skuNoList;
    }

    @Data
    @NoArgsConstructor
    public static class ViewAddDetailDTO {

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 委外数量
         */
        private Integer qty;

        /**
         * 有效收货数量
         */
        private Integer receiveQty;

        /**
         * 有效入库数量
         */
        private Integer instockQty;

        /**
         * 是否自动生成采购单
         */
        private Boolean isGeneratePo;

        /**
         * 备注
         */
        private String remark;

        /**
         * 子件信息
         */
        private List<ViewAddDetailDTO> childList;
    }

}