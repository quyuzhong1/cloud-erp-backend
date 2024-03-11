package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 委外变更单请求响应实体
 *
 * @author will
 * @since 2023-06-08
*/
@Data
@NoArgsConstructor
public class SubcontractChangeDTO implements Serializable {


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
          * 页面高级查询
          */
         private List<AdvanceQueryDTO> advanceQueryDTOList;

         /**
          * sqlMap 默认key default
          */
         private Map<String, String> sqlMap;


         /**
         * 搜索类型
         */
         private String  searchType;

         private List<String> approveStatusList;

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
         * 明细id
         */
        private String detailId;

        /**
         * 单据编号
         */
        private String code;
        /**
         * 委外订单号
         */
        private String sourceCode;

        /**
         * 供应商名称
         */
        private String supplierName;

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
         * 作废状态名称（false未作废，true已作废）
         */
        private String invalidStatusName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变更类型
         */
        private String optType;

        /**
         * 变更类型名称
         */
        private String optTypeName;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 金额
         */
        private BigDecimal amount;

        /**
         * 采购数量
         */
        private Integer qty;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 原采购数量
         */
        private Integer oldQty;

        /**
         * 原采购单价
         */
        private BigDecimal oldPrice;

        /**
         * 原采购金额
         */
        private BigDecimal oldAmount;

        /**
         * 备注
         */
        private String remark;

        /**
        * 变更人名称
        */
        private String changerName;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

        /**
         * 币种符号
         */
        private String currencySymbol;
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
         * 变更单号
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
        * 变更日期
        */
        private LocalDate billDate;

        /**
         * 委外订单编号
         */
        private String sourceCode;

        /**
        * 采购组织id
        */
        private String purchaseOrgId;

        /**
         * 采购组织名称
         */
        private String purchaseOrgName;

        /**
         * 变更人名称
         */
        private String changerName;

        /**
         * 采购部门名称
         */
        private String deptName;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 变更明细
         */
        private List<SubcontractChangeDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 变更明细
         */
        @NotEmpty(message = "变更明细不能为空")
        private List<SubcontractChangeDetailDTO.AddDTO> detailList;
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
        private String id;
        /**
         * 变更明细
         */
        @NotEmpty(message = "变更明细不能为空")
        private List<SubcontractChangeDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * 变更人id
        */
        @NotBlank(message = "变更人id不能为空")
        @Size(max = 19,message = "变更人id最大长度不能超过19位")
        private String changerId;

        /**
        * 变更人部门id
        */
        @Size(max = 19,message = "变更人部门id最大长度不能超过19位")
        private String deptId;

        /**
        * 变更原因
        */
        @Size(max = 255,message = "变更原因最大长度不能超过255位")
        private String changeReason;

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