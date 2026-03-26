package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import com.common.business.enums.ApproveStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 质检申请单主表请求响应实体
 * </p>
 *
 * @author will
 * @since 2026-03-20
*/
@Data
@NoArgsConstructor
public class QcApplicationDTO implements Serializable {



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
          * 类型名称
          */
         private String tabFlagName;

         /**
         * 数量
         */
         private Integer count;

         public String getTabFlagName() {
             if (this.tabFlag == null) {
                 return null;
             }
             return ApproveStatusEnum.getName(this.tabFlag);
         }

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
        * 主键id【可排序】
        */
        private String  id;
        /**
         * 质检申请单明细id【可排序】
         */
        private String detailId;

        /**
        * 质检申请单号【可排序】
        */
        private String code;

        /**
        * 来源id【可排序】
        */
        private String sourceId;

        /**
        * 来源编码【可排序】
        */
        private String sourceCode;

        /**
        * 来源类型【可排序】
        */
        private String sourceType;

        /**
        * 单据状态【可排序】
        */
        private String approveStatus;
        /**
         * 单据状态名称
         */
        private String approveStatusName;
        /**
         * 最新审核人
         */
        private String approveUserName;

        /**
        * 审核完成时间【可排序】
        */
        private LocalDate approveTime;

        /**
        * 质检类型【可排序】
        */
        private String qcType;

        /**
         * 质检类型名称
         */
        private String qcTypeName;

        /**
        * 仓库id【可排序】
        */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * SKU【可排序】
         */
        private String skuNo;

        /**
         * 产品名称【可排序】
         */
        private String productName;

        /**
         * 供应商id【可排序】
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 质检状态【可排序】
         */
        private String qcStatus;

        /**
         * 质检状态名称
         */
        private String qcStatusName;

        /**
         * 质检结果【可排序】
         */
        private String qcResult;

        /**
         * 质检结果名
         */
        private String qcResultName;

        /**
         * 申请质检数量【可排序】
         */
        private Integer qty;

        /**
         * 批次合格量【可排序】
         */
        private Integer batchQty;

        /**
         * 质检数量【可排序】
         */
        private Integer qcQty;

        /**
         * 质检合格数量【可排序】
         */
        private Integer qcGoodQty;

        /**
         * 质检不良数量【可排序】
         */
        private Integer qcBadQty;

        /**
        * 期望质检日期【可排序】
        */
        private LocalDate planQcDate;

        /**
        * 备注【可排序】
        */
        private String remark;

        /**
        * 创建时间【可排序】
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称【可排序】
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
        * 质检申请单号
        */
        private String code;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
         * 来源类型名称
         */
        private String sourceTypeName;

        /**
        * 单据状态
        */
        private ApproveStatusEnum approveStatus;
        /**
         * 单据状态名称
         */
        private String approveStatusName;

        /**
        * 质检类型
        */
        private String qcType;

        /**
         * 质检类型名称
         */
        private String qcTypeName;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
        * 期望质检日期
        */
        private LocalDate planQcDate;

        /**
        * 备注
        */
        private String remark;

        /**
         * 明细数据
         */
        private List<QcApplicationDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 质检申请单明细列表
         */
        @NotEmpty(message = "质检申请单明细不能为空")
        private List<QcApplicationDetailDTO.AddDTO> detailList;
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
        * 质检申请单明细列表
         */
        @NotEmpty(message = "质检申请单明细不能为空")
        private List<QcApplicationDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
        * 来源id
        */
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源编码
        */
        @Size(max = 32,message = "来源编码最大长度不能超过32位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 质检类型
        */
        @NotBlank(message = "质检类型不能为空")
        @Size(max = 32,message = "质检类型最大长度不能超过32位")
        private String qcType;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 期望质检日期
        */
        private LocalDate planQcDate;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

    }

    @Data
    @NoArgsConstructor
    public static class ListPushQcNoticeDTO {
        /**
         * 质检申请id
         */
        private String id;
        /**
         * 质检申请单号
         */
        private String code;
        /**
         * 来源单号
         */
        private String sourceCode;
        /**
         * 质检类型
         */
        private String qcType;
        /**
         * 质检类型名称
         */
        private String qcTypeName;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 期望质检日期
         */
        private LocalDate planQcDate;
    }

    @Data
    @NoArgsConstructor
    public static class GenerateQcNoticeDTO {
        /**
         * 质检申请id
         */
        @NotBlank(message = "质检申请id不能为空")
        private String id;

        /**
         * 期望质检日期
         */
        @NotNull(message = "期望质检日期不能为空")
        private LocalDate planQcDate;

        /**
        * 质检员id
        */
        private String qcUserId;
    }


    @Data
    @NoArgsConstructor
    public static class GeneratePoRefQcApplicationDTO {
        /**
         * 采购订单id
         */
        @NotBlank(message = "采购订单id不能为空")
        private String poId;
        /**
         * 采购订单明细id
         */
        @NotBlank(message = "采购订单明细id不能为空")
        private String podId;
        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
         * 期望质检日期
         */
        private LocalDate planQcDate;
    }
}