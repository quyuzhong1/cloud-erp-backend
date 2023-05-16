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
 * @Classname SoInfoDTO
 * @Description TODO
 * @Date 2023-05-10 17:55
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SoInfoDTO implements Serializable {

    /**
     * tab list
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {

        private String searchType;

        private Integer count;
    }

    /**
     * 分页数据
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
        private String type;

        /**
         * 类型名称
         */
        private String typeName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名
         */
        private Boolean invalidStatusName;


        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户
         */
        private String customerName;

        /**
         * 销售组织
         */
        private String orgId;

        /**
         * 销售组织名
         */
        private String orgName;

        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 发货状态
         */
        private String deliveryStatus;

        /**
         * 发货状态
         */
        private String deliveryName;

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
         * 销售数量
         */
        private Integer qty;

        /**
         * 缺货数量
         */
        private Integer lackQty;

        /**
         * 可出数量
         */
        private Integer availableQty;

        /**
         * 已经出库数量
         */
        private Integer deliveryQty;

        /**
         * 剩余数量
         */
        private Integer waitQty;


        /**
         * 单位
         */
        private String unit;

        /**
         * 要货 日期
         */
        private LocalDate requireDate;

        /**
         * 销售金额
         */
        private BigDecimal amount;

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
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * sku no 集合
         */
        private List<String> skuNoList;

        /**
         * code
         */
        private String code;



        /**
         * 类型
         */
        private String type;

        /**
         * 审核状态集合
         */
        private List<String> approveStatusList;

        /**
         * 作废状态
         * true 已作废
         * false 未作废
         */
        private Boolean invalidStatus;

        /**
         * 发货状态状态
         */
        private String deliveryStatus;

        /**
         * 要货日期集合
         */
        private List<LocalDate> requireDateList;

        /**
         * 客户 集合
         */
        private List<String> customerIdList;

        /**
         * 销售员 集合
         */
        private List<String> sellerIdList;

        /**
         * 创建人 id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDateTime> createTimeList;


    }


    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * 类型
         */
        @NotNull(message = "单据类型不能为空")
        @StateEnumValue(clazz = BillTypeEnum.class, message = "单据类型有误")
        private BillTypeEnum type;

        /**
         * 要货日期
         */
        @NotNull(message = "要货日期不能为空")
        private LocalDate requireDate;

        /**
         * 组织id
         */
        @NotBlank(message = "销售组织不能为空")
        private String salesOrgId;

        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 销售员id
         */
        @NotBlank(message = "销售员不能为空")
        private String sellerId;

        /**
         * 是否收取手续费
         * true 收
         */
        private Boolean isCollectShippingFee;


        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;


        /**
         * 银行手续费
         */
        private BigDecimal bankServiceFee;

        /**
         * 运费
         */
        private BigDecimal shippingFee;

        /**
         * 客户id
         */
        @NotBlank(message = "客户不能为空")
        private String customerId;


        /**
         * 收货人
         * 来源 http://172.16.100.11:3002/project/110/interface/api/13561
         */
        @Size(max = 50, message = "收货人最大50字符")
        private String receiverName;

        /**
         * 电话
         */
        @Size(max = 20, message = "联系电话最大20字符")
        private String telNumber;

        /**
         * 收货人地址
         */
        @Size(max = 50, message = "收货人地址最大50字符")
        private String receiverAddress;

        /**
         *
         *
         *  交货方式 oms/common/enumDropDown?type=DeliveryMode
         *    描述：deliverGoods（发货）selfExtraction（自提）
         *
         */
        @StateEnumValue(strValues = {"deliverGoods","selfExtraction"},message = "交货方式有误")
        private String deliveryMode;


        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
         * 是否含税
         * true 是
         */
        private Boolean isTax;

        /**
         * 地址类型
         * http://172.16.100.11:3002/project/110/interface/api/13480
         * type=AddressType
         */
        @StateEnumValue(strValues = {"forwarder","deliver","company"},message = "地址类型有误")
        private String addressType;


        @Valid
        private List<SoDetailDTO.AddDTO> detailList;

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
         * code
         */
        private String code;


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
        private String type;

        /**
         * 要货日期
         */
        private LocalDate requireDate;

        /**
         * 组织id
         */
        private String salesOrgId;

        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 销售员id
         */
        private String sellerId;


        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库组织id
         */
        private String warehouseOrgId;

        /**
         * 仓库组织名
         */
        private String warehouseOrgName;

        /**
         * 仓库id
         */
        private String warehouseName;


        /**
         * 银行手续费
         */
        private BigDecimal bankServiceFee;

        /**
         * 运费
         */
        private BigDecimal shippingFee;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户id
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
         * 收货人地址
         */
        private String receiverAddress;

        /**
         * 交货方式
         */
        private String deliveryMode;


        /**
         * 币种
         */
        private String currency;

        /**
         * 是否含税
         * true 是
         */
        private Boolean isTax;

        /**
         * 地址类型
         */
        private String addressType;

        /**
         * 订单产品详情
         */
        private List<SoDetailDTO.ViewDTO> detailList;
    }


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {


        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 类型
         */
        private String type;

        /**
         * 要货日期
         */
        private LocalDate requireDate;

        /**
         * 组织id
         */
        private String salesOrgId;

        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 销售员id
         */
        private String sellerId;


        /**
         * 仓库id
         */
        private String warehouseId;


        /**
         * 银行手续费
         */
        private BigDecimal bankServiceFee;

        /**
         * 运费
         */
        private BigDecimal shippingFee;

        /**
         * 客户id
         */
        private String customerId;


        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 电话
         */
        private String telNumber;

        /**
         * 收货人地址
         */
        private String receiverAddress;

        /**
         * 交货方式
         */
        private String deliveryMode;


        /**
         * 币种
         */
        private String currency;

        /**
         * 是否含税
         * true 是
         */
        private Boolean isTax;

        /**
         * 地址类型
         */
        private String addressType;

        private List<SoDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends SoChangeDTO.PagingParamDTO {

        private List<String> ids;
    }
}
