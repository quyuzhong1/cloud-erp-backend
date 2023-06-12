package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
         * 类型
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
        * 审核状态
        */
        private String approveStatus;
        /**
        * 单据编号
        */
        private String code;
        /**
        * 单据日期
        */
        private LocalDate billDate;
        /**
        * 采购部门id
        */
        private String deptId;
        /**
        * 采购部门名称
        */
        private String deptName;
        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;
        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;
        /**
        * 审核时间
        */
        private LocalDateTime approveTime;
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
        * 审核状态名称
        */
        private String approveStatusName;
        /**
        * 作废状态名称
        */
        private String invalidStatusName;
        /**
        * sku id
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
         * 数量
         */
        private Integer qty;

        /**
         * 收货数量
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

    @Data
    @NoArgsConstructor
    public static class PurchaseOrderListDTO {

        /**
         * 是否是父级SKU
         */
        private Boolean isParent;

        /**
         * 采购列表信息
         */
        private PurchaseOrderDTO.ListDTO purchaseOrderDTO;
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
         * 调拨日期
         */
        private  LocalDate  billDate;
        /**
         * 收料组织id
         */
        private String     receiveOrgId;
        /**
         * 采购组织id
         */
        private String    purchaseOrgId;
        /**
         * 采购员id
         */
        private String     purchaserId;
        /**
         * 采购部门id
         */
        private String     deptId;
        /**
         * 委外组织id
         */
        private String   subcontractOrgId;
        /**
         * 新品首批（false否,true是）
         */
        private  Boolean   isFirstMassProduct;
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


}