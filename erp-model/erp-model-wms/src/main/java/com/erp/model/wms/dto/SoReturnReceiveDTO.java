package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SoReturnReceiveDTO {
    private SoReturnReceiveDTO() {
        throw new IllegalStateException("Utility SoReturnReceiveDTO class");
    }
    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParam extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
        /**
         * 主键id
         */
        private List<String> ids;
        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;
        /**
         * 单据编号
         */
        private String code;
        /**
         * sku编号
         */
        private List<String> skuNoList;
        /**
         * 销售单号
         */
        private String sourceCode;
        /**
         * 单据类型
         */
        private String type;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 审核状态
         */
        private List<String> approveStatusList;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 客户id
         */
        private List<String> customerIdList;
        /**
         * 库存组织id
         */
        private List<String> inventoryOrgIdList;
        /**
         * 销售员id
         */
        private List<String> sellerIdList;
        /**
         * 退货日期
         */
        private List<LocalDate> returnDateList;
        /**
         * 签收日期
         */
        private List<LocalDate> billDateList;
        /**
         * 创建人id
         */
        private List<String> createUserIdList;
    }

    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingView {
        /**
         * id
         */
        private String id;

        private String type;
        /**
         * 明细表id
         */
        private String receiveDetailId;
        /**
         * 退货单id
         */
        private String sourceId;
        /**
         * 退货单明细id
         */
        private String sourceDetailId;
        /**
         * 退货签收单号
         */
        private String code;
        /**
         * 退货单号
         */
        private String sourceCode;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 库存组织
         */
        private String inventoryOrgName;
        /**
         * 单据状态编号
         */
        private String approveStatus;
        /**
         * 单据状态名称
         */
        private String approveStatusName;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * sku_id
         */
        private String skuId;
        /**
         * SKU
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 退货类型名称
         */
        private String returnTypeDictName;
        /**
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
        /**
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 销售员
         */
        private String sellerName;
        /**
         * 退货日期
         */
        private LocalDate returnDate;
        /**
         * 签收日期
         */
        private LocalDate billDate;
        /**
         * 审核人
         */
        private String approveUserName;
        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 仓库Id
         */
        private String warehouseId;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;
        /**
         * 币别
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;
        /**
         *退货金额
         */
        private BigDecimal returnAmount;
        /**
         *含税退货金额
         */
        private BigDecimal taxReturnAmount;
        /**
         *销售金额
         */
        private BigDecimal amount;
        /**
         *含税销售金额
         */
        private BigDecimal taxAmount;
        /**
         *退货金额（本位币）
         */
        private BigDecimal returnAmountLocalCurrency;
        /**
         *含税退货金额（本位币）
         */
        private BigDecimal taxReturnAmountLocalCurrency;
        /**
         *汇率
         */
        private BigDecimal exchangeRate;
    }

    /**
     * 列表状态数量查询
     */
    @Data
    @NoArgsConstructor
    public static class StatusCountDTO {
        /**
         * 类型(waitSubmit 待提交，approveIng 审核中，reject 审核不通过，approve 已审核)
         */
        private String type;
        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class Add {
        /**
         * 退货单id
         */
        private String sourceId;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 库存组织id
         */
        @NotBlank(message = "库存组织不能为空")
        private String inventoryOrgId;
        /**
         * 仓管员
         */
        private String warehouseKeeperId;
        /**
         * 签收日期
         */
        private LocalDate billDate;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 销售员名称
         */
        private String sellerName;
        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 退货日期
         */
        private LocalDate returnDate;

        /**
         * 销售组织id
         */
        @NotBlank(message = "销售组织不能为空")
        private String salesOrgId;

        /**
         * 单据类型
         */
        @NotBlank(message = "单据类型不能为空")
        private String type;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;
        /**
         * 明细信息
         */
        private List<SoReturnReceiveDetailDTO.Add> detailList;
        /**
         * 汇率
         */
        private BigDecimal exchangeRate;
        /**
         * 比重
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class Update {
        /**
         * id
         */
        private String id;
        /**
         * 退货单id
         */
        private String sourceId;
        /**
         * 库存组织id
         */
        @NotBlank(message = "库存组织不能为空")
        private String inventoryOrgId;
        /**
         * 仓管员
         */
        private String warehouseKeeperId;
        /**
         * 签收日期
         */
        private LocalDate billDate;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 退货日期
         */
        private LocalDate returnDate;

        /**
         * 销售组织id
         */
        private String salesOrgId;

        /**
         * 单据类型
         */
        private String type;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;
        /**
         * 明细信息
         */
        private List<SoReturnReceiveDetailDTO.Update> detailList;
        /**
         * 汇率
         */
        private BigDecimal exchangeRate;
        /**
         * 比重
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class View {
        /**
         * id
         */
        private String id;
        /**
         * 退货单id
         */
        private String sourceId;
        /**
         * 退货单编号
         */
        private String sourceCode;
        /**
         * 单据编号
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
         * 单据类型
         */
        private String type;
        /**
         * 单据类型名称
         */
        private String typeName;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 销售组织id
         */
        private String salesOrgId;
        /**
         * 销售组织名称
         */
        private String salesOrgName;
        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售部门名称
         */
        private String salesDeptName;
        /**
         * 销售员id
         */
        private String sellerId;
        /**
         * 销售员名称
         */
        private String sellerName;
        /**
         * 退货日期
         */
        private LocalDate returnDate;
        /**
         * 签收日期
         */
        private LocalDate billDate;
        /**
         * 库存组织
         */
        private String inventoryOrgId;
        /**
         * 库存组织名称
         */
        private String inventoryOrgName;
        /**
         * 仓管员id
         */
        private String warehouseKeeperId;
        /**
         * 仓管员名称
         */
        private String warehouseKeeperName;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * 作废描述
         */
        private String invalidRemark;
        /**
         * 审核人id
         */
        private String approveUserId;
        /**
         * 审核人名称
         */
        private String approveUserName;
        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;
        /**
         * 明细信息
         */
        private List<SoReturnReceiveDetailDTO.View> detailList;
        /**
         * 汇率
         */
        private BigDecimal exchangeRate;
        /**
         * 比重
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    /**
     * 签收单下推退货入库单-列表查询
     */
    @Data
    @NoArgsConstructor
    public static class ReceiveGenerateSoReturnInstockView {
        /**
         * id
         */
        private String id;
        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 主表id
         */
        private String mainId;
        /**
         * 单据编号
         */
        private String code;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         * 单据日期
         */
        private LocalDate billDate;
        /**
         * 退货客户id
         */
        private String customerId;
        /**
         * 退货客户
         */
        private String customerName;
        /**
         * 销售组织
         */
        private String salesOrgId;
        /**
         * 销售组织名称
         */
        private String salesOrgName;
        /**
         * 销售员id
         */
        private String sellerId;
        /**
         * 销售员
         */
        private String sellerName;
        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售部门名称
         */
        private String salesDeptName;
        /**
         * 仓库
         */
        private String warehouseId;
        /**
         * 仓库
         */
        private String warehouseName;
        /**
         * 单据类型
         */
        private String type;

        /**
         * skuId
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 退货数量
         */
        private Integer returnQty;
        /**
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 已出库数量
         */
        private Integer deliveryQty;
        /**
         * 应退数量
         */
        private Integer mustQty;
        /**
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 实退数量
         */
        private Integer realQty;
        /**
         * 退货类型
         */
        private String returnTypeDict;
        /**
         * 退货类型名称
         */
        private String returnTypeDictName;
        /**
         * 退货原因
         */
        private String  returnReasonDict;
        /**
         * 退货原因名称
         */
        private String  returnReasonDictName;
        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 备注
         */
        private String remark;
        /**
         * 入库日期
         */
        private LocalDate instockDate;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;

        /**
         * 是否子skuNo
         */
        private Boolean isChildSkuNo;
        /**
         * 平台sku
         */
        private String platformSkuNo;
    }

    /**
     * PDA:分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PdaPagingView {
        /**
         * 主键id
         */
        private String id;
        /**
         * 单 据编号
         */
        private String code;
        /**
         * 销售员名称
         */
        private String sellerName;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
        /**
         * 产品数量
         */
        private Integer detailCount;
        /**
         * 产品信息
         */
        private List<PdaItemDTO> itemList;
    }

    /**
     * PDA:商品信息
     */
    @Data
    @NoArgsConstructor
    public static class PdaItemDTO {
        /**
         * 明细id
         */
        private String id;
        /**
         * sku
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
         * 收货数量
         */
        private Integer receiveQty;
    }

    /**
     * PDA:列表查询参数
     * @Author Luo_WG
     * @Date 2023/8/15 11:19
     **/
    @Data
    @NoArgsConstructor
    public static class PdaPagingParamDTO extends SortDTO {
        /**
         * 审核状态：根据tab页传审核状态
         */
        private List<String> approveStatusList;

        /**
         * 签收日期
         */
        private List<LocalDate> billDateList;
    }

    /**
     * PDA:列表状态
     * @Author Luo_WG
     * @Date 2023/8/11 9:15
     **/
    @Data
    @NoArgsConstructor
    public static class PdaSoReturnReceiveCount {
        /**
         * 类型(waitSubmitAndReject 待提交/审核不通过，approveIng 审核中，approve 已审核)
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * PDA:查询销售退货签收单
     * @Author Luo_WG
     * @Date 2023/8/18 11:57
     **/
    @Data
    @NoArgsConstructor
    public static class PdaSoReceive {
        /**
         * 主键id
         */
        private String id;
        /**
         * 签收单号
         */
        private String code;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 销售员名称
         */
        private String sellerName;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
    }

    /**
     * PDA:销售退货单查询参数
     * @Author Luo_WG
     * @Date 2023/8/18 12:00
     **/
    @Data
    @NoArgsConstructor
    public static class PdaSoReceiveParam {
        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 采购收货单号
         */
        private String code;
    }
}
