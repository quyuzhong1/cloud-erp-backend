package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.model.wms.enums.QcReCheckResultEnum;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname QcBill

 * @Date 2023-04-14 15:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcInfoDTO implements Serializable {


    /**
     * 添加质检单
     */
    @Data
    @NoArgsConstructor
    public static class SaveOrUpdateDTO extends PermissionsDTO {

        /**
         * 质检单id
         */
        private String id;


        /**
         * 质检日期
         */
        @NotNull(message = "质检日期不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private LocalDate qcDate;


        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 质检员id
         */
        private String qcUserId;


        /**
         * 质检部门id
         */
        private String qcDeptId;


        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 产品信息
         * 从这个 接口获取http://172.16.100.11:3002/project/83/interface/api/9511
         */
        @Valid
        private QcProductDTO.AddDTO qcProduct;


        /**
         * 质检信息
         */
        @Valid
        private QcResultDTO.AddDTO qcInfo;


        /**
         * 质检报告明细
         * 来源  http://172.16.100.11:3002/project/92/interface/api/9574
         */
        @Valid
        private List<QcReportDetailDTO.AddDTO> reportDetailList;

        /**
         * 质检单备注 集合
         */
        private List<QcRemarkDTO.AddDTO> remarkList;


    }


    /**
     * 收获单自动下推质检单
     */
    @Data
    @NoArgsConstructor
    public static class ReceiveToQcDTO {

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源type
         */
        private String sourceType;

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;

        /**
         * 来源详情id
         */
        private String sourceDetailId;

        /**
         * 是否新品
         */
        private Boolean isFirstMassProduct;
        /**
         * 质检类型
         */
        private String qcType;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单code
         */
        private String purchaseOrderCode;

        /**
         * 交货仓库id
         */
        private String deliveryWarehouseId;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 产品等级
         */
        private String productGrade;

        /**
         * 销售方式
         */
        private String saleMethod;


        /**
         * 变体属性
         */
        private String variantProperty;

        /**
         * 产品长
         */
        private BigDecimal productLength;

        /**
         * 产品宽
         */
        private BigDecimal productWidth;

        /**
         * 产品高
         */
        private BigDecimal productHeight;

        /**
         * 箱长
         */
        private BigDecimal boxLength;

        /**
         * 箱宽
         */
        private BigDecimal boxWidth;

        /**
         * 箱高
         */
        private BigDecimal boxHeight;

        /**
         * 产品净重
         */
        private BigDecimal productNetWeight;

        /**
         * 外箱重量
         */
        private BigDecimal boxWeight;

        /**
         * 质检总数量
         */
        private Integer totalQty;


    }

    /**
     * 销售退货签收单自动下推质检单
     */
    @Data
    @NoArgsConstructor
    public static class SoReturnReceiveToQcDTO {

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 来源type
         */
        private String sourceType;

        /**
         * 销售单id
         */
        private String soId;

        /**
         * 销售单编号
         */
        private String soCode;

        /**
         * 退货单id
         */
        private String soReturnId;

        /**
         * 退货单明细id
         */
        private String soReturnDetailId;

        /**
         * 退货单编号
         */
        private String soReturnCode;

        /**
         * 是否新品
         */
        private Boolean isFirstMassProduct;

        /**
         * 质检类型
         */
        private String qcType;

        /**
         * 交货仓库id
         */
        private String warehouseId;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 产品等级
         */
        private String productGrade;

        /**
         * 销售方式
         */
        private String saleMethod;


        /**
         * 变体属性
         */
        private String variantProperty;

        /**
         * 产品长
         */
        private BigDecimal productLength;

        /**
         * 产品宽
         */
        private BigDecimal productWidth;

        /**
         * 产品高
         */
        private BigDecimal productHeight;

        /**
         * 箱长
         */
        private BigDecimal boxLength;

        /**
         * 箱宽
         */
        private BigDecimal boxWidth;

        /**
         * 箱高
         */
        private BigDecimal boxHeight;

        /**
         * 产品净重
         */
        private BigDecimal productNetWeight;

        /**
         * 外箱重量
         */
        private BigDecimal boxWeight;

        /**
         * 质检总数量
         */
        private Integer totalQty;


    }

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
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


    }


    /**
     * 质检单详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends PermissionsDTO {

        /**
         * 质检单id
         */
        private String id;


        /**
         * code
         */
        private String code;

        /**
         * 质检日期
         */
        private LocalDate qcDate;


        /**
         * 采购订单id
         */
        private String purchaseOrderId;


        /**
         * 采购订单code
         */
        private String purchaseOrderCode;


        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名
         */
        private String supplierName;


        /**
         * 仓库 id
         */
        private String warehouseId;


        /**
         * 仓库名
         */
        private String warehouseName;

        /**
         * 质检员id
         */
        private String qcUserId;


        /**
         * 质检员
         */
        private String qcUserName;


        /**
         * 质检部门id
         */
        private String qcDeptId;

        /**
         * 质检状态
         */
        private QcBillStatusEnum qcStatus;

        /**
         * 质检状态名
         */
        private String qcStatusName;

        /**
         * 质检部门
         */
        private String qcDeptName;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源id
         */
        private String sourceId;


        /**
         * 产品信息
         * 从这个 接口获取http://172.16.100.11:3002/project/83/interface/api/9511
         */
        @Valid
        private QcProductDTO.ViewDTO qcProduct;


        /**
         * 质检信息
         */
        @Valid
        private QcResultDTO.ViewDTO qcInfo;


        /**
         * 质检报告明细
         * 来源  http://172.16.100.11:3002/project/92/interface/api/9574
         */
        @Valid
        private List<QcReportDetailDTO.ViewDTO> reportDetailList;

        /**
         * 质检单备注 集合
         */
        private List<QcRemarkDTO.AddDTO> remarkList;


    }


    /**
     * 分页参数
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
     * 仓库分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 质检单id
         */
        private String id;

        /**
         * 质检产品id
         */
        private String productId;

        /**
         * code
         */
        private String code;

        /**
         * 质检日期
         */
        private LocalDate qcDate;


        /**
         * 采购订单id
         */
        private String purchaseOrderId;


        /**
         * 采购订单code
         */
        private String purchaseOrderCode;

        /**
         * 质检员id
         */
        private String qcUserId;


        /**
         * 质检员
         */
        private String qcUserName;


        /**
         * 质检状态
         * draft 暂存
         * waitQc 待质检
         * exemption 免检
         * finishQc 已质检
         * cancel 取消
         */
        private QcBillStatusEnum qcStatus;

        /**
         * 质检状态名
         */
        private String qcStatusName;


        /**
         * 质检类型
         */
        private QcTypeEnum qcType;

        /**
         * 质检类型名
         */
        private String qcTypeName;

        /**
         * 是否内检  true 是
         */
        private Boolean isInside;


        /**
         * 内检 类型
         */
        private String insideType;


        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名
         */
        private String supplierName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku 名
         */
        private String skuName;


        /**
         * sku 名
         */
        private String skuNo;

        /**
         * 总量
         */
        private Integer totalQty;

        /**
         * 质检量
         */
        private Integer qcQty;


        /**
         * 质检合格量
         */
        private Integer qcGoodQty;

        /**
         * 质检不良量
         */
        private Integer qcBadQty;

        /**
         * 质检结果
         */
        private QcResultEnum qcResult;

        /**
         * 质检结果名
         */
        private String qcResultName;

        /**
         * 问题属性
         */
        private String qcProblemDict;

        /**
         * 问题属性名称
         */
        private String qcProblemName;

        /**
         * 处理措施
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890 type=handleModeType
         */
        private String handleModeDict;

        /**
         * 处理措施名
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890 type=handleModeType
         */
        private String handleModeName;

        /**
         * 不良现象
         */
        private String badDescription;


        /**
         * 质检合格率
         */
        private BigDecimal qcGoodRate;

        /**
         * 质检不良率
         */
        private BigDecimal qcBadRate;


        /**
         * 仓库 id
         */
        private String warehouseId;


        /**
         * 仓库名
         */
        private String warehouseName;

        /**
         * 备注
         */
        private String remark;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 产品长
         */
        private BigDecimal productLength;

        /**
         * 产品宽
         */
        private BigDecimal productWidth;

        /**
         * 产品高
         */
        private BigDecimal productHeight;

        /**
         * 外箱长
         */
        private BigDecimal boxLength;

        /**
         * 外箱宽
         */
        private BigDecimal boxWidth;

        /**
         * 外箱高
         */
        private BigDecimal boxHeight;

        /**
         * 产品净重
         */
        private BigDecimal productNetWeight;

        /**
         * 外箱重量
         */
        private BigDecimal boxWeight;

        /**
         * qc质检id
         */
        @JsonIgnore
        private String qcResultId;

        /**
         * 是否库内内检 默认 false
         */
        private Boolean isInsideQc;

        /**
         * 复检抽检结果
         */
        private String qcSampleResult;

        /**
         * 复检抽检结果描述
         */
        private String qcSampleResultName;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源编号
         */
        private String sourceCode;

        /**
         * 质检完成时间
         */
        private LocalDateTime qcFinishTime;
    }


    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }


    /**
     * 分配质检员
     */
    @Data
    @NoArgsConstructor
    public static class AssignDTO {

        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        @NotBlank(message = "质检员不能为空")
        private String qcUserId;
    }

    /**
     * 退货签收单下推质检单
     */
    @Data
    @NoArgsConstructor
    public static class ReceiveGenerateQcView {
        /**
         * id
         */
        private String id;
        /**
         * 主表id
         */
        private String mainId;
        /**
         * 退货通知单id
         */
        private String sourceId;
        /**
         * 退货通知单明细id
         */
        private String sourceDetailId;
        /**
         * 退货单号
         */
        private String sourceCode;
        /**
         * 单据编号
         */
        private String code;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 仓库
         */
        private String warehouseLocation;
        /**
         * 仓位
         */
        private String warehouseId;
        /**
         * 备注
         */
        private String remark;
        /**
         * 审核状态
         */
        private String approveStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseQcParamDTO {

        /**
         * 内外检类型
         */
        private String qcType;

        /**
         * 采购订单编号id集合
         */
        private List<String> purchaseOrderIds;

    }

    @Data
    @NoArgsConstructor
    public static class QcReceiveResultDTO {


        /**
         * 收货单号
         */
        private String receiveCode;


        /**
         * 采购明细id
         */
        private String purchaseDetailId;

        /**
         * 收货单号
         */
        private Integer qcGoodQty;

    }

    @Data
    @NoArgsConstructor
    public static class PurchaseQcInfoDTO {


        /**
         * 次品数量
         */
        private Integer defectiveQty;

    }

    /**
     * 质检日报导出
     */
    @Data
    @NoArgsConstructor
    public static class QcDailyReportDTO {

        /**
         * 检验日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate qcDate;

        /**
         * 内外检类型
         */
        private String qcInsideType;

        /**
         * 内外检类型名称
         */
        private String qcInsideTypeName;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 是否新品首批
         */
        Boolean isFirstMassProduct;

        /**
         * 产品状态
         */
        private String firstMassProductName;

        /**
         * 产品图片
         */
        private List<String> productImgUrl;
        private String productImg;


        /**
         * 箱唛图片
         */
        private List<String> boxMarkImgUrl;
        private String boxMarkImg;

        /**
         * 供应商
         */
        private String supplierName;

        /**
         * 产品图片
         */
        private String productName;

        /**
         * 回仓数量
         */
        private Integer totalQty;

        /**
         * 验货结果
         */
        private String qcResultName;

        /**
         * 质检员
         */
        private String qcUserName;

        /**
         * 质检数量
         */
        private Integer qcQty;

        /**
         * 质检不良量
         */
        private Integer qcBadQty;

        /**
         * 检验不良率
         */
        private String qcBadRate;

        /**
         * 问题属性
         */
        private String qcProblemDict;


        /**
         * 问题属性名称
         */
        private String qcProblemName;

        /**
         * 不良现象
         */
        private String badDescription;

        /**
         * 不良附图
         */
        private List<WmsAttachmentDTO.UpdateDTO> badAttachments;
        private String badAttachment;

        /**
         * 处理措施
         */
        private String handleModeDict;

        /**
         * 处理措施名
         */
        private String handleModeName;


        /**
         * 处理结果
         */
        private String handleResultName;

        /**
         * 产品长
         */
        private BigDecimal productLength;

        /**
         * 产品宽
         */
        private BigDecimal productWidth;

        /**
         * 产品高
         */
        private BigDecimal productHeight;

        private String productSize;

        /**
         * 产品净重
         */
        private BigDecimal productNetWeight;

        /**
         * 箱长
         */
        private BigDecimal boxLength;

        /**
         * 箱宽
         */
        private BigDecimal boxWidth;

        /**
         * 箱高
         */
        private BigDecimal boxHeight;

        private String boxSize;

        /**
         * 外箱重量
         */
        private BigDecimal boxWeight;

        /**
         * 整箱数量（个）
         */
        private Integer fullBoxQty;

        /**
         * 最新备注
         */
        private String remark;

        /**
         * 报告
         */
        private List<QcReportDetailDTO.ViewDTO> reportList;

        private String reportListLink;

    }

    /**
     * 质检日报信息
     */
    @Data
    @NoArgsConstructor
    public static class DailyListDTO {
        /**
         * 质检单id
         */
        private String id;

        /**
         * 质检产品id
         */
        private String productId;

        /**
         * code
         */
        private String code;

        /**
         * 质检日期
         */
        private LocalDate qcDate;


        /**
         * 采购订单id
         */
        private String purchaseOrderId;


        /**
         * 采购订单code
         */
        private String purchaseOrderCode;

        /**
         * 质检员id
         */
        private String qcUserId;


        /**
         * 质检员
         */
        private String qcUserName;


        /**
         * 质检状态
         * draft 暂存
         * waitQc 待质检
         * exemption 免检
         * finishQc 已质检
         * cancel 取消
         */
        private QcBillStatusEnum qcStatus;

        /**
         * 质检状态名
         */
        private String qcStatusName;


        /**
         * 质检类型
         */
        private QcTypeEnum qcType;

        /**
         * 质检类型名
         */
        private String qcTypeName;

        /**
         * 是否内检  true 是
         */
        private Boolean isInside;


        /**
         * 内检 类型
         */
        private String insideType;


        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名
         */
        private String supplierName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku 名
         */
        private String skuName;


        /**
         * sku 名
         */
        private String skuNo;

        /**
         * 总量
         */
        private Integer totalQty;

        /**
         * 质检量
         */
        private Integer qcQty;


        /**
         * 质检合格量
         */
        private Integer qcGoodQty;

        /**
         * 质检不良量
         */
        private Integer qcBadQty;

        /**
         * 质检结果
         */
        private QcResultEnum qcResult;

        /**
         * 质检结果名
         */
        private String qcResultName;

        /**
         * 问题属性
         */
        private String qcProblemDict;

        /**
         * 问题属性名称
         */
        private String qcProblemName;

        /**
         * 处理措施
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890 type=handleModeType
         */
        private String handleModeDict;

        /**
         * 处理措施名
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890 type=handleModeType
         */
        private String handleModeName;

        /**
         * 不良现象
         */
        private String badDescription;


        /**
         * 质检合格率
         */
        private BigDecimal qcGoodRate;

        /**
         * 质检不良率
         */
        private BigDecimal qcBadRate;


        /**
         * 仓库 id
         */
        private String warehouseId;


        /**
         * 仓库名
         */
        private String warehouseName;

        /**
         * 备注
         */
        private String remark;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 产品长
         */
        private BigDecimal productLength;

        /**
         * 产品宽
         */
        private BigDecimal productWidth;

        /**
         * 产品高
         */
        private BigDecimal productHeight;

        /**
         * 外箱长
         */
        private BigDecimal boxLength;

        /**
         * 外箱宽
         */
        private BigDecimal boxWidth;

        /**
         * 外箱高
         */
        private BigDecimal boxHeight;

        /**
         * 整箱数量
         */
        private Integer boxQty;

        /**
         * 产品净重
         */
        private BigDecimal productNetWeight;

        /**
         * 外箱重量
         */
        private BigDecimal boxWeight;

        /**
         * qc质检id
         */
        @JsonIgnore
        private String qcResultId;

    }

    /**
     * 复检抽检
     */
    @Data
    @NoArgsConstructor
    public static class ReQcDTO extends PermissionsDTO {

        @Size(min = 1, message = "请至少勾选一条数据")
        private List<String> ids;

        /**
         * 复检抽检结果
         */
        @NotEmpty(message = "请选择抽检结果")
        @StateEnumValue(clazz = QcReCheckResultEnum.class, message = "复检抽检结果有误")
        private String qcSampleResult;

        /**
         * 抽检备注
         */
        private String remark;

    }

}
