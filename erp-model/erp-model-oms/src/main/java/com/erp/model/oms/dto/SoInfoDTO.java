package com.erp.model.oms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.validator.AddGroup;
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

        /**
         * waitApprove 待审核
         * all 全部
         * waitDelivery 待发货
         * delivery 已发货
         * reject 不通过
         */
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
         * id
         */
        private String detailId;


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
        private String invalidStatusName;


        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户
         */
        private String customerName;


        /**
         * 销售组织名
         */
        private String salesOrgName;


        /**
         * 销售员
         */
        private String sellerName;


        /**
         * 发货状态
         */
        private String deliveryStatus;
        /**
         * 发货状态名
         */
        private String deliveryStatusName;


        /**
         * sku id
         */
        private String skuId;


        /**
         * sku no
         */
        private String skuNo;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;


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
        private Integer scarceQty;

        /**
         * 是否缺货
         * 如果可出数量小于销售数量，即显示缺货标识
         */
        private Boolean isScarce;

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
         * all 全部
         * waitApprove 待审核
         * waitDelivery 待发货
         * delivery 已发货
         * reject 审核不通过
         */
        @StateEnumValue(strValues = {"all", "waitApprove", "waitDelivery", "reject", "delivery"}, message = "搜索类型有误")
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
        private List<LocalDate> createTimeList;


    }


    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * id
         * 当暂存的时候会存在
         */
        private String id;


        /**
         * 类型 来源
         * http://172.16.100.11:3002/project/110/interface/api/13480
         * type=BillType
         */
        @NotBlank(message = "单据类型不能为空", groups = {AddGroup.class})
        @StateEnumValue(clazz = BillTypeEnum.class, message = "单据类型有误", groups = {AddGroup.class})
        private String type;

        /**
         * 要货日期
         */
        @NotNull(message = "要货日期不能为空", groups = {AddGroup.class})
        private LocalDate requireDate;

        /**
         * 组织id
         */
        @NotBlank(message = "销售组织不能为空", groups = {AddGroup.class})
        private String salesOrgId;

        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 销售员id
         */
        @NotBlank(message = "销售员不能为空", groups = {AddGroup.class})
        private String sellerId;

        /**
         * 是否收取手续费
         * true 收
         */
        private Boolean isCollectShippingFee;


        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空", groups = {AddGroup.class})
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
        @NotBlank(message = "客户不能为空", groups = {AddGroup.class})
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
         * 来源 http://172.16.100.11:3002/project/110/interface/api/13561
         *
         *  这个是地址下拉 http://172.16.100.11:3002/project/110/interface/api/13786
         */
        private String receiveAddressId;

        /**
         * 交货方式 oms/common/enumDropDown?type=DeliveryMode
         * 描述：deliverGoods（发货）selfExtraction（自提）
         */
        @StateEnumValue(strValues = {"deliverGoods", "selfExtraction"}, message = "交货方式有误", groups = {AddGroup.class})
        private String deliveryMode;


        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空", groups = {AddGroup.class})
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
        @StateEnumValue(strValues = {"forwarder", "deliver", "company"}, message = "地址类型有误", groups = {AddGroup.class})
        private String addressType;


        @Valid
        @Size(min = 1,message = "销售订单至少要需要添加一个sku",groups = {AddGroup.class})
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
         * 类型 来源
         * http://172.16.100.11:3002/project/110/interface/api/13480
         * type=BillType
         */
        @NotBlank(message = "单据类型不能为空")
        @StateEnumValue(clazz = BillTypeEnum.class, message = "单据类型有误")
        private String type;

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
         * 交货方式 oms/common/enumDropDown?type=DeliveryMode
         * 描述：deliverGoods（发货）selfExtraction（自提）
         */
        @StateEnumValue(strValues = {"deliverGoods", "selfExtraction"}, message = "交货方式有误", groups = {AddGroup.class})
        private String deliveryMode;


        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空", groups = {AddGroup.class})
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
        @StateEnumValue(strValues = {"forwarder", "deliver", "company"}, message = "地址类型有误", groups = {AddGroup.class})
        private String addressType;

        @Valid
        private List<SoDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }

    /**
     * 销售订单的客户信息
     */
    @Data
    @NoArgsConstructor
    public static class CustomerDTO {


        /**
         * 单据类型
         */
        private String type;

        private String code;

        private LocalDateTime createTime;

        /**
         *审核状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据类型名称
         */
        private String typeName;
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
        private String receiveAddress;

        /**
         * 收货人地址
         */
        private String receiveAddressId;

        /**
         * 交货方式
         */
        private String deliveryMode;

        /**
         * 交货方式
         */
        private String deliveryModeName;


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
         * 地址类型名
         */
        private String addressTypeName;


        /**
         * 要货日期
         */
        private LocalDate requireDate;

        /**
         * 销售组织
         */
        private String salesOrgName;


        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售部门
         */
        private String salesDeptName;


        /**
         * 销售员
         *
         * @author yl
         * @date 2023-05-18 10:24
         * @param null
         * @return
         */
        private String sellerName;

        private String sellerId;


    }


    /**
     * 导出销售订单的信息
     */
    @Data
    @NoArgsConstructor
    public static class ExportPdfDTO {

        /**
         * 合同号
         */
        private String code;

        /**
         * 甲方(购方)
         */
        private String customerName;

        /**
         * 纳税人识别号 (甲方)
         */
        private String taxpayerId;

        /**
         * 联系人 (甲方)
         */
        private String contactPerson;

        /**
         * 联系 电话 (甲方)
         */
        private String contactTelNumber;

        /**
         * 联系地址 (甲方)
         */
        private String contactAddress;

        /**
         * 合计
         */
        private BigDecimal totalAmount;

        /**
         * 大写
         */
        private String chineseAmount;

        /**
         * 合计数据
         */
        private Integer totalQty;

        /**
         * 币别
         */
        private String currency;


        /**
         * 签订日期(甲方)
         */
        private LocalDate firstSignDate;

        /**
         * 签订日期（乙方）
         */
        private LocalDate secondSignDate;


        //乙方
        private String company;

        //乙方 纳税识别号
        private String companyTaxpayerId;

        //乙方 联系人
        private String sellerName;

        //乙方 联系电话
        private String sellerTelNumber;

        //乙方 地址
        private String companyAddress;

        //明细
        private List<SoDetailDTO.ExportPdfDTO> details;


    }

    @Data
    @NoArgsConstructor
    public static class ViewGenerateSalesDemandDTO {

        /**
         * 审核状态
         */
        private String approveStatus;

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
         * 来源类型
         */
        private String sourceType;

        /**
         * 仓库id
         */
        private String warehouseId;

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
         * 销售数量
         */
        private Integer qty;

        /**
         * 缺货数量
         */
        private Integer scarceQty;

        /**
         * 备货数量
         */
        private Integer planStockQty;

        /**
         * 备注
         */
        private String remark;
    }



}
