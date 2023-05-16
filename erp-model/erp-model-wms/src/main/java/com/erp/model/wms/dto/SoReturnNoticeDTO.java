package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.wms.enums.ReturnTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SoReturnNoticeDTO {
    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParam extends SortDTO {
        /**
         * 主键id
         */
        private List<String> ids;
        /**
         * sku编号
         */
        private List<String> skuNoList;
        /**
         * 单据编号
         */
        private String code;
        /**
         * 销售单号
         */
        private String sourceCode;
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
         * 审核状态
         */
        private List<String> approveStatusList;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 退货日期
         */
        private List<LocalDate> billDateList;
        /**
         * 创建人id
         */
        private List<String> createUserIdList;
        /**
         * 创建时间
         */
        private List<LocalDateTime> createTimeList;
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
         * 退货订单id
         */
        private String sourceId;
        /**
         * 退货订单详情id
         */
        private String sourceDetailId;
        /**
         * 退货通知单号
         */
        private String code;
        /**
         * 退货订单号
         */
        private String sourceCode;
        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 库存组织
         */
        private String inventoryOrgName;
        /**
         * 销售员
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
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private ReturnTypeEnum returnTypeDict;
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
         * 销售单id
         */
        private String sourceId;
        /**
         * 库存组织id
         */
        private String inventoryOrgId;
        /**
         * 仓管员
         */
        private String warehouseKeeperId;
        /**
         * 明细信息
         */
        private List<SoReturnNoticeDetailDTO.Add> detailList;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class Update {
        /**
         * 主键id
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
         * 库存组织id
         */
        private String inventoryOrgId;
        /**
         * 仓管员
         */
        private String warehouseKeeperId;
        /**
         * 明细信息
         */
        private List<SoReturnNoticeDetailDTO.Update> detailList;
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
         * 审核状态
         */
        private String approveStatusName;
        /**
         * 单据类型
         */
        private String type;
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
        private String deptId;
        /**
         * 销售部门名称
         */
        private String deptName;
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
         * 作废状态
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
         * 明细信息
         */
        private List<SoReturnNoticeDetailDTO.View> detailList;
    }

    /**
     * 下推退货签收单列表集合
     */
    @Data
    @NoArgsConstructor
    public static class ListGenerateSoReturnReceiveView {
        private List<GenerateSoReturnReceiveView> list;
    }

    /**
     * 下推退货签收单-列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateSoReturnReceiveView {
        /**
         * 退货单id
         */
        private String sourceId;
        /**
         * 退货单号
         */
        private String sourceCode;

        /**
         * 客户id
         */
        private String customerId;
        /**
         * skuId
         */
        private String  skuId;
        /**
         * sku编码
         */
        private String  skuNo;
        /**
         * 产品名称
         */
        private String  productName;
        /**
         * 销售数量
         */
        private Integer salesQty;
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
         * 退货原因
         */
        private String  returnReasonDict;
        /**
         * 备注
         */
        private String  remark;
    }
}
