package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SoReturnDTO {
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
        private Map<String,String> sqlMap;
    }

    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingView {
        /**
         * 主键id
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 销售单id
         */
        private String sourceId;
        /**
         * 销售单详情表id
         */
        private String sourceDetailId;
        /**
         * 退货订单号
         */
        private String code;
        /**
         * 销售单号
         */
        private String sourceCode;
        /**
         * 单据类型
         */
        private String type;
        /**
         * 单据类型名称
         */
        private String typeName;
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
         * 客户名称id
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
         * 销售员Id
         */
        private String sellerId;
        /**
         * 销售员名称
         */
        private String sellerName;
        /**
         * skuId
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
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
        /**
         * 已出库数量
         */
        private Integer deliveryQty;
        /**
         * 剩余未出数量
         */
        private Integer unDeliveryQty;
        /**
         * 退货入库
         */
        private Integer returnInStockQty;
        /**
         * 单位
         */
        private String unit;
        /**
         * 销售金额
         */
        private BigDecimal salesAmount;
        /**
         * 币种
         */
        private String currency;
        /**
         * 币别符号
         */
        private String currencySymbol;
        /**
         * 退货日期
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
    }

    /**
     * 列表状态数量查询
     */
    @Data
    @NoArgsConstructor
    public static class StatusCountDTO {

        /**
         * 类型(toBeApprove 待审核，reject 审核不通过，approve 已审核)
         */
        private String tabFlag;

        /**
         * tab名称
         */
        private String tabFlagName;

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
         * 销售单id
         */
        private String sourceId;
        /**
         * 来源类型：界面新增可传空值
         */
        private String sourceType;
        /**
         * 退货日期
         */
        private LocalDate billDate;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 明细信息
         */
        private List<SoReturnDetailDTO.Add> detailList;
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
         * 销售单id
         */
        private String sourceId;
        /**
         * 退货日期
         */
        private LocalDate billDate;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 明细信息
         */
        private List<SoReturnDetailDTO.Update> detailList;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class View {
        /**
         * 主键id
         */
        private String id;
        /**
         * 销售单id
         */
        private String sourceId;
        /**
         * 销售单编号
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
        private LocalDate billDate;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 收货人
         */
        private String receiverName;
        /**
         * 联系电话
         */
        private String telNumber;
        /**
         * 收货地址
         */
        private String receiveAddress;
        /**
         * 交货方式
         */
        private String deliveryModeDict;
        /**
         * 币别
         */
        private String currency;
        /**
         * 币种符号
         */
        private String currencySymbol;
        /**
         * 是否含税
         */
        private Boolean isTax;
        /**
         * 地址类型
         */
        private String addressTypeDict;
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
         * 库存组织id
         */
        private String inventoryOrgId;
        /**
         * 库存组织id
         */
        private String inventoryOrgName;

        /**
         * 能否编辑销售单号
         */
        private Boolean canChangeSoInfo;
        /**
         * 详情信息
         */
        private List<SoReturnDetailDTO.View> detailList;
    }

    /**
     * 下推退货通知单-列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateSoReturnNoticeView {
        /**
         * id
         */
        private String id;
        /**
         * 主表id
         */
        private String mainId;
        /**
         * 销售单id
         */
        private String sourceId;
        /**
         * 销售单详情表id
         */
        private String sourceDetailId;
        /**
         * 销售单号
         */
        private String sourceCode;
        /**
         * 单据编号
         */
        private String code;
        /**
         * 退货客户
         */
        private String customerName;
        /**
         * skuId
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
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 已出库数量
         */
        private Integer deliveryQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 销售类型名称
         */
        private String returnTypeDictName;
        /**
         * 退货原因 调用字典接口 类型=ReturnReason
         */
        private String returnReasonDict;
        /**
         * 退货原因名称
         */
        private String returnReasonDictName;
        /**
         * 备注
         */
        private String remark;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 库存组织id
         */
        private String inventoryOrgId;
        /**
         * 库存组织id
         */
        private String inventoryOrgName;
        /**
         * 退货物流单号
         */
        private String returnLogisticCode;
    }

    /**
     * PDA:销售退货单查询列表
     */
    @Data
    @NoArgsConstructor
    public static class PdaSoReturn {
        /**
         * 退货单id
         */
        private String id;

        /**
         * 退货单号
         */
        private String soReturnCode;

        /**
         * 销售订单号
         */
        private String soCode;

        /**
         * 销售员
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
    }

    /**
     * PDA:销售退货单查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PdaSoReturnParam {
        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 销售退货单号
         */
        private String code;
    }

    @Data
    @NoArgsConstructor
    public static class PlatformSkuDTO {
        /**
         * 平台sku
         */
        private List<String> platformSkuNoList;
        /**
         * 退货销售单id
         */
        private String id;
    }
}
