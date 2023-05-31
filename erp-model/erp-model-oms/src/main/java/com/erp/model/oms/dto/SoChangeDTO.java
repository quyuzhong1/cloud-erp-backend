package com.erp.model.oms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.StateEnumValue;
import com.erp.model.oms.enums.BillTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname SoChangeDTO
 * @Description TODO
 * @Date 2023-05-11 9:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SoChangeDTO implements Serializable {


    @Data
    @NoArgsConstructor
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

    @Data
    @NoArgsConstructor
    public static class ApproveCountDTO {
        /**
         * 类型
         */
        private String approveStatus;

        /**
         * 数量
         */
        private Integer count;
    }


    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * code
         */
        private String code;

        /**
         * 销售订单code
         */
        private String soCode;


        /**
         * 销售订单id
         */
        private String soId;


        /**
         * 审核状态code
         */
        private ApproveStatusEnum approveStatus;


        /**
         * 审核状态名
         */
        private String approveStatusName;
        /**
         * 类型
         */
        private BillTypeEnum orderType;

        /**
         * 类型名称
         */
        private String orderTypeName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名
         */
        private String invalidStatusName;


        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户
         */
        private String customerName;


        /**
         * sku id
         */
        private String skuId;


        /**
         * sku no
         */
        private String skuNo;

        /**
         * 产品单位
         */
        private String unit;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 新销售数量
         */
        private Integer qty;

        /**
         * 原销售数量
         */
        private Integer oldQty;

        /**
         * 新销售金额
         */
        private BigDecimal amount;

        /**
         * 销售金额 字符串 导出用到
         */
        private String amountStr;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 原销售金额
         */
        private BigDecimal oldAmount;


        /**
         * 原销售金额 字符串 导出用到
         */
        private String oldAmountStr;

        /**
         * 原币种符号
         */
        private String oldCurrencySymbol;

        /**
         * 最新审核人
         */
        private String approveUserName;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;


    }

    /**
     * 分页参数信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {


        /**
         * all 全部
         * waitApprove 待审核
         * approve 已审核
         * reject 审核不通过
         */
        @StateEnumValue(strValues = {"all", "waitApprove", "approve", "reject"}, message = "搜索类型有误")
        @NotBlank(message = "搜索类型不能为空")
        private String searchType;
        /**
         * sku no 集合
         */
        private List<String> skuNoList;

        /**
         * code
         */
        private String code;

        /**
         * 销售订单code
         */
        private String soCode;

        /**
         * 类型
         */
        private String orderType;

        /**
         * 审核列表集合
         */
        private List<String> approveStatusList;

        /**
         * 作废状态
         * true 已作废
         * false 未作废
         */
        private Boolean invalidStatus;

        /**
         * 客户 集合
         */
        private List<String> customerIdList;

        /**
         * 创建人 id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

    }


    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * 销售订单id
         */
        private String soId;


        /**
         * 变更日期
         */
        @NotNull(message = "变更日期不能为空")
        private LocalDate billDate;


        /**
         * 变更部门id
         */
        private String deptId;

        /**
         * 变更人
         */
        private String userId;


        /**
         * 变更原因
         */
        @NotBlank(message = "变更原因不能为空")
        @Size(max = 200, message = "变更原因最大200字符")
        private String remark;


        /**
         * 产品信息
         */
        @Valid
        @Size(min = 1, message = "销售变更订单详情不能为空")
        private List<SoChangeDetailDTO.AddDTO> detailList;


    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 变更单号
         */
        private String code;


        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;


        /**
         * 单据状态名
         */
        private String approveStatusName;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单code
         */
        private String soCode;


        /**
         * 变更日期
         */
        private LocalDate billDate;


        /**
         * 变更部门id
         */
        private String deptId;

        /**
         * 变更员id
         */
        private String userId;


        /**
         * 单据类型
         */
        private String orderType;

        /**
         * 单据类型名
         */
        private String orderTypeName;


        /**
         * 销售组织
         */
        private String salesOrgName;

        /**
         * 销售组织
         */
        private String salesOrgId;


        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 销售员
         */
        private String sellerName;


        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户
         */
        private String customerName;


        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 电话
         */
        private String telNumber;

        /**
         * 收货地址
         */
        private String receiveAddress;

        /**
         * 交货方式
         */
        private String deliveryModeName;


        /**
         * 币种
         */
        private String currency;



        /**
         * 币种
         */
        private String currencySymbol;

        /**
         * 是否含税
         * true 是
         */
        private Boolean isTax;



        /**
         * 地址类型
         */
        private String addressTypeName;


        /**
         * 变更原因
         */
        private String remark;


        /**
         * 产品信息
         */
        private List<SoChangeDetailDTO.ViewDTO> detailList;


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * id
         */
        @NotBlank(message = "销售变更单不能为空")
        private String id;


        /**
         * 变更日期
         */
        @NotNull(message = "变更日期不能为空")
        private LocalDate billDate;


        /**
         * 变更部门id
         */
        private String deptId;

        /**
         * 变更员id
         */
        private String userId;


        /**
         * 变更原因
         */
        private String remark;

        /**
         * 产品信息
         */
        private List<SoChangeDetailDTO.UpdateDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        private List<String> ids;
    }

    @Data
    @NoArgsConstructor
    public static class SoRefDTO {


        /**
         * id
         */
        private String id;

        /**
         * 变更单号
         */
        private String code;

        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据状态名
         */
        private String approveStatusName;


        /**
         * 单据类型
         */
        private String orderType;

        /**
         * 单据类型名
         */
        private String orderTypeName;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户名
         */
        private String customerName;


        /**
         * 单位
         */
        private String unit;
        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;


        /**
         * 产品名称
         */
        private String productName;

        /**
         * 新销售数量
         */
        private Integer qty;

        /**
         * 原销售数量
         */
        private Integer oldQty;

        /**
         * 新销售金额
         */
        private BigDecimal amount;

        /**
         * 销售金额 字符串 导出用到
         */
        private String amountStr;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 原销售金额
         */
        private BigDecimal oldAmount;


        /**
         * 原销售金额 字符串 导出用到
         */
        private String oldAmountStr;

        /**
         * 原币种符号
         */
        private String oldCurrencySymbol;

        /**
         * 最新审核人
         */
        private String approveUserName;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;




    }
}
