package com.erp.model.plm.dto;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ServiceCodeNameEnum;
import com.erp.model.plm.enums.MouldRefundStatusEnum;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 模具主表请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Data
@NoArgsConstructor
public class MouldInfoDTO implements Serializable {


    /**
     * 分页
     */
    @Getter
    @Setter
    public static class PagingViewDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 项目编号
         */
        private String projectNo;

        /**
         * 项目名称
         */
        private String name;

        /**
         * 状态
         */
        private String status;
        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
        /**
         * 审核人名称
         */
        private String approveUserName;
        /**
         * 明细id
         */
        private String detailId;

        /**
         * 模具编号
         */
        private String mouldNo;

        /**
         * 外部模具编号(供应商)
         */
        private String thirdMouldNo;

        /**
         * 模具类型
         */
        @Dict(tableName = "cfg_mould_setting", queryFieldName = "id")
        private String typeId;

        /**
         * 模具穴数
         */
        private String mouldHoles;

        /**
         * 模具长
         */
        private BigDecimal length;

        /**
         * 模具宽
         */
        private BigDecimal width;

        /**
         * 模具高
         */
        private BigDecimal height;

        /**
         * 模具材质
         */
        private String material;

        /**
         * 模具寿命(万)(啤)
         */
        private Integer lifeCycle;

        /**
         * 开模周期(自然日)
         */
        private Integer developCycle;

        /**
         * 启用时间
         */
        private LocalDate enableDate;

        /**
         * 供应商id
         */
        @Dict(tableName = "supplier", serviceCode = ServiceCodeNameEnum.SCM, queryFieldName = "id")
        private String supplierId;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建人名称
         */
        private String createUserName;
        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 修改时间
         */
        private LocalDateTime updateTime;

        /**
         * 产品明细
         */
        private List<MouldProductDTO.ViewDTO> productList;

        /**
         * 存放位置
         */
        private MouldStoreLocationDTO.ViewDTO storeLocation;

    }

    /**
     * 参数
     */
    @Getter
    @Setter
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

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
        private String id;

        /**
         * 项目编号
         */
        private String projectNo;

        /**
         * 项目名称
         */
        private String name;

        /**
         * 状态
         */
        @Dict(enumClass = ApproveStatusEnum.class)
        private String status;

        /**
         * 产品经理
         */
        private String productManagerId;

        /**
         * 备注
         */
        private String remark;

        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 模具分类编码
         */
        private String mouldCategoryCode;

        /**
         * 模具明细
         */
        private List<MouldDetailDTO.ViewDTO> mouldDetailList;

        /**
         * 文档明细
         */
        private List<MouldDocInfoDTO.ViewDTO> mouldDocInfoList;
    }

    /**
     * 修改
     */
    @Getter
    @Setter
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 明细数据
         */
        @Valid
        private List<MouldDetailDTO.UpdateDTO> detailList;

        /**
         * 文档数据
         */
        private List<MouldDocInfoDTO.UpdateDTO> docList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 项目编号
         */
        @Size(max = 50, message = "项目编号最大长度不能超过50位")
        private String projectNo;

        /**
         * 项目名称
         */
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 255, message = "项目名称最大长度不能超过255位")
        private String name;

        /**
         * 产品经理
         */
        @NotBlank(message = "产品经理不能为空")
        @Size(max = 19, message = "产品经理最大长度不能超过19位")
        private String productManagerId;

        /**
         * 备注
         */
        @Size(max = 255, message = "备注最大长度不能超过100位")
        private String remark;

        /**
         * 分类id
         */
        @NotBlank(message = "分类id不能为空")
        @Size(max = 19, message = "分类id最大长度不能超过19位")
        private String categoryId;

        /**
         * 模具分类编码
         */
        private String mouldCategoryCode;

    }

    /**
     * 存放位置
     */
    @Getter
    @Setter
    public static class StoreLocationDTO {
        /**
         * 明细id
         */
        @Size(min = 1, message = "明细不能为空")
        private List<String> detailId;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id")
        private String warehouseId;
        /**
         * 库位
         */
        @NotBlank(message = "库位")
        private String warehouseLocation;
        /**
         * 明细地址
         */
        private String address;
    }

    @Getter
    @Setter
    public static class EnableTimeDTO {
        /**
         * 明细id
         */
        @Size(min = 1, message = "明细不能为空")
        private List<String> detailIdList;

        /**
         * 启用时间
         */
        @NotNull(message = "启用时间")
        private LocalDate enableTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 类型，
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
     * 下单跟踪
     */
    @Getter
    @Setter
    public static class OrderTrackingViewDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 项目编号
         */
        private String projectNo;

        /**
         * 项目名称
         */
        private String name;

        /**
         * 状态
         */
        private String status;
        /**
         * 明细id
         */
        private String detailId;

        /**
         * 模具编号
         */
        private String mouldNo;

        /**
         * 外部模具编号(供应商)
         */
        private String thirdMouldNo;
        /**
         * 供应商id
         */
        @Dict(tableName = "supplier", serviceCode = ServiceCodeNameEnum.SCM, queryFieldName = "id")
        private String supplierId;
        /**
         * 数量
         */
        private Integer qty;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 结算方式
         */
        private String payMethodId;

        /**
         * 付款条件
         */
        private String paymentCondition;

        /**
         * 币种
         */
        private String currency;
        /**
         * 汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 关联产品
         */
        private List<MouldRefProductDTO.ViewDTO> refProductList;

        /**
         * 是否费用返还
         */
        private Boolean isNeedRefund;

        /**
         * 返还标准
         */
        private String refundStandard;

        /**
         * 退款单量
         */
        private Integer refundOrderQty;

        /**
         * 返还金额
         */
        private BigDecimal refundAmount;

        /**
         * 费用返还状态
         */
        @Dict(enumClass = MouldRefundStatusEnum.class)
        private String refundStatus;
        /**
         * 采购数量
         */
        private Integer purchaseQty;
        /**
         * 收货数量
         */
        private Integer receiveQty;
        /**
         * 入库数量
         */
        private Integer stockInQty;
        /**
         * 修改时间
         */
        private LocalDateTime updateTime;

        /**
         * 差异数量
         */
        private Integer diffQty;
    }

    @Getter
    @Setter
    public static class ReturnConfirmDTO {

        /**
         * 模具id
         */
        private String mouldDetailId;

        /**
         * 文件地址
         */
        private String fileUrl;

        /**
         * 文件名字
         */
        private String fileName;

        /**
         * 备注
         */
        private String remark;

    }

    @Getter
    @Setter
    public static class OrderTrackingDetailParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        /**
         * 模具id
         */
        private String mouldDetailId;

    }

    @Getter
    @Setter
    public static class RefProductDTO {
        /**
         * 模具id
         */
        private String mouldDetailId;

        /**
         * 关联产品
         */
        @Valid
        private List<MouldRefProductDTO.UpdateDTO> refProductList;

    }

    @Getter
    @Setter
    public static class OrderTrackingDetailDTO {
        /**
         * 采购单号
         */
        private String purchaseOrderCode;

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 供应商id
         */
        @Dict(tableName = "supplier", serviceCode = ServiceCodeNameEnum.SCM, queryFieldName = "id")
        private String supplierId;

        //创建时间
        private LocalDateTime createTime;

        /**
         * 采购数量
         */
        private Integer purchaseQty;
        /**
         * 收货数量
         */
        private Integer receiveQty;
        /**
         * 入库数量
         */
        private Integer stockInQty;
    }
}