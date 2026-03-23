package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 质检通知单请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-04-21
*/
@Data
@NoArgsConstructor
public class QcNoticeDTO implements Serializable {


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
         * 类型
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
        private String  id;
        /**
        * 主键id
        */
        private String  detailId;

        /**
        * 单据状态
        */
        private String approveStatus;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 最新审核人ID
        */
        private String approveUserId;

        /**
        * 最新审核人
        */
        private String approveUserName;

        /**
        * 质检通知单号
        */
        private String code;


        /**
         * 质检单号
         */
        private String qcInfoCode;

        /**
        * 质检仓库
        */
        private String qcWarehouseId;
        private String qcWarehouseName;

        /**
        * 上架仓库
        */
        private String putawayWarehouseId;
        private String putawayWarehouseName;

        /**
        * 质检类型 stockIn 入库质检   outsideQc 外检质检  insideQc  在库质检   newProductStockIn 新品入库质检  b2bOutsideQc  B2B外检  
        */
        private String qcType;
        private String qcTypeName;

        /**
         * 质检状态 QcBillStatusEnum
         */
        private String qcStatus;
        private String qcStatusName;

        /**
         * 质检时效
         */
        private Integer qcTimeliness;

        /**
        * 备注
        */
        private String remark;

        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;
        /**
         * 是否作废名称
         */
        private String invalidStatusName;


        /**
        * 作废原因
        */
        private String invalidRemark;


        /**
         * sku_id
         */
        private String skuId;

        /**
         * sku_no
         */
        private String skuNo;

        /**
         * sku_name
         */
        private String productName;

        /**
         * 质检通知数量
         */
        private Integer qcNoticeQty;

        /**
         * 质检数量
         */
        private Integer qcQty;

        /**
         * 送检差异数量
         */
        private Integer qcDiffQty;

        /**
         * 良品数量
         */
        private Integer qcGoodQty;

        /**
         * 不良品数量
         */
        private Integer qcBadQty;

        /**
         * 上架数量
         */
        private Integer putawayQty;

        /**
         * 不良备注
         */
        private String badDesc;

        /**
         * 质检员id
         */
        private String qcUserId;

        /**
         * 质检员
         */
        private String qcUserName;

        /**
         * 问题属性 type=qcProblemType
         */
        private String qcProblemDict;

        /**
         * 质检状态 QcBillStatusEnum
         */
        private String qcDetailStatus;
        private String qcDetailStatusName;

        /**
         * 质检时间
         */
        private LocalDateTime qcDate;

        /**
         * 上架状态 待上架:wait  部分上架：part  已上架：finish
         */
        private String putawayStatus;
        private String putawayStatusName;

        /**
         * 上架时间
         */
        private LocalDateTime putawayDate;

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
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单编码
         */
        private String purchaseOrderCode;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;
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
        * 单据状态
        */
        private ApproveStatusEnum approveStatus;

        private String approveStatusName;

        /**
        * 质检通知单号
        */
        private String code;

        /**
        * 质检仓库
        */
        private String qcWarehouseId;
        private String qcWarehouseName;

        /**
        * 上架仓库
        */
        private String putawayWarehouseId;
        private String putawayWarehouseName;

        /**
        * 质检类型 stockIn 入库质检   outsideQc 外检质检  insideQc  在库质检   newProductStockIn 新品入库质检  b2bOutsideQc  B2B外检  
        */
        private String qcType;
        private String qcTypeName;

        /**
         * 质检状态 QcNoticeStatusEnum
         */
        private String qcStatus;

        /**
         * 质检状态名称 QcNoticeStatusEnum
         */
        private String qcStatusName;

        /**
         * 质检人名称
         */
        private String qcUserName;

        /**
         * 质检时间
         */
        private LocalDateTime qcDate;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
        * 备注
        */
        private String remark;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 采购单id
         */
        private String purchaseOrderId;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        private List<QcNoticeDetailDTO.ViewDTO> detailList;


    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class QcInfoView  {
        /**
         *
         */
        private String id;
        /**
         *
         */
        private String detailId;
        /**
         * 通知单号
         */
        private String code;
        /**
         * 质检单号
         */
        private String qcBillCode;
        /**
         * 质检类型
         */
        private String qcType;
        /**
         * 质检类型
         */
        private String qcTypeName;
        /**
         * 抽样方案id
         */
        private String samplingPlanId;
        /**
         * 抽样方案名称
         */
        private String samplingPlanName;
        /**
         *
         */
        private String skuId;
        /**
         *
         */
        private String skuNo;
        /**
         *
         */
        private String productName;
        /**
         *
         */
        private String qcWarehouseId;
        /**
         * 质检通知数量
         */
        private Integer qcNoticeQty;
        /**
         * 建议抽样数量
         */
        private Integer suggestSamplingQty;
        /**
         * 质检数量
         */
        private Integer qcQty;
        /**
         *差异数量
         */
        private Integer qcDiffQty;
        /**
         *
         */
        @NotNull(message = "良品数量不能为空")
        @Min(value = 0, message = "良品数量不能小于0")
        private Integer qcGoodQty;
        /**
         *
         */
        @NotNull(message = "不良品数量不能为空")
        @Min(value = 0, message = "不良品数量不能小于0")
        private Integer qcBadQty;
        /**
         *问题属性
         */
        private String qcProblemDict;
        private String qcProblemDictName;
        /**
         * 不良描述
         */
        private String badDesc;
        /**
         *
         */
        private String qcUserId;
        private String qcUserName;
        /**
         *
         */
        @NotNull(message = "质检日期不能为空")
        private LocalDate qcDate;
        /**
         *
         */
        private List<String> attachNameList;
        private List<String> attachUrlList;

        /**
         * 检验结果 QcResultEnum
         */
        private String qcResult;
        /**
         * 检验结果名称
         */
        private String qcResultName;
        /**
         * 批次合格量
         */
        private Integer lotQualifiedQty;

        /**
         * 质检标准
         */
        private QcStandardView qcStandardViewList;

        /**
         * 缺陷信息
         */
        private List<DefectView> defectViewList;
    }

    @Data
    @NoArgsConstructor
    public static class QcStandardView{

        /**
         * 抽样方案id
         */
        private String samplingPlanId;
        /**
         * 抽样方案名称
         */
        private String samplingPlanName;

        /**
         * 建议抽样数量
         */
        private Integer suggestSamplingQty;

        /**
         * 附件url
         */
        private String attachUrl;

        /**
         * 附件名称
         */
        private String attachName;

        /**
         * 质检项目
         */
        private List<QcInspectItemView>  qcInspectItemViewDTOList;

        /**
         * 参考图片
         */
        private List<QcImageView>  qcImageViewDTOList;
    }

    @Data
    @NoArgsConstructor
    public static class DefectView{
        /**
         * 缺陷等级,枚举接口待定
         */
        private String defectLevl;
        /**
         * 缺陷数量
         */
        private String defectQty;
        /**
         * 问题属性
         */
        private String issueProperty;
        /**
         * 缺陷描述
         */
        private String defectDesc;
        /**
         * 不良图片url
         */
        private String imageUrl;

        /**
         * 不良图片名称
         */
        private String imageName;
    }

    @Data
    @NoArgsConstructor
    public static class QcInspectItemView{

        /**
         * 质检项目
         */
        private String inspectItem;

        /**
         * 质检要求
         */
        private String inspectRequirement;
    }

    @Data
    @NoArgsConstructor
    public static class QcImageView{

        /**
         * 图片类型
         */
        private String imageType;

        /**
         *
         */
        private String imageUrl;
    }

    @Data
    @NoArgsConstructor
    public static class QcNoticeParamDTO{

        /**
         * 质检通知单号
         */
        @NotBlank(message = "质检通知单号不允许为空")
        private String qcNoticeCode;

        /**
         * skuId
         */
        @NotBlank(message = "skuId不允许为空")
        private String skuId;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        @NotEmpty
        private List<QcNoticeDetailDTO.@Valid AddDTO> detailList;
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

        @NotEmpty
        private List< QcNoticeDetailDTO.@Valid UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 质检仓库
        */
        @NotBlank(message = "质检仓库不能为空")
        private String qcWarehouseId;

        /**
        * 上架仓库
        */
        @NotBlank(message = "上架仓库不能为空")
        private String putawayWarehouseId;

        /**
        * 质检类型 stockIn 入库质检   outsideQc 外检质检  insideQc  在库质检   newProductStockIn 新品入库质检  b2bOutsideQc  B2B外检  
        */
        @NotBlank(message = "质检类型不能为空")
        private String qcType;

        /**
        * 备注
        */
        @Size(max = 500,message = "质检类型最大长度不能超过255位")
        private String remark;

    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<QcNoticeDetailDTO.AddDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }

    /**
    * 作废DTO
    */
    @Data
    @NoArgsConstructor
    public static class InvalidDTO {
        /**
        * ID集合
        */
        @NotEmpty(message = "ID不能为空")
        private List<String> ids;

        /**
        * 作废备注
        */
        @Size(max = 500, message = "作废备注最大长度不能超过500位")
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class QcStandardAddDTO{

        /**
         * 抽样方案id
         */
        private String samplingPlanId;
        /**
         * 抽样方案名称
         */
        private String samplingPlanName;

        /**
         * 建议抽样数量
         */
        private Integer suggestSamplingQty;

        /**
         * 附件url
         */
        private String attachUrl;

        /**
         * 附件名称
         */
        private String attachName;

        /**
         * 质检项目
         */
        private List<QcInspectItemAddDTO>  qcInspectItemAddDTOList;

        /**
         * 参考图片
         */
        private List<QcImageAddDTO>  qcImageAddDTOList;
    }

    @Data
    @NoArgsConstructor
    public static class QcInspectItemAddDTO{

        /**
         * 质检项目
         */
        private String inspectItem;

        /**
         * 质检要求
         */
        private String inspectRequirement;
    }

    @Data
    @NoArgsConstructor
    public static class QcImageAddDTO{

        /**
         * 图片类型
         */
        private String imageType;

        /**
         *
         */
        private String imageUrl;
    }
}