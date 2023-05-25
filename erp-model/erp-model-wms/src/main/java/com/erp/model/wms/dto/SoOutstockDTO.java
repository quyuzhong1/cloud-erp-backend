package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售出库
 *
 * @author Lambda
 * @Classname SoOutstockDTO
 * @Description TODO
 * @Date 2023-05-11 10:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SoOutstockDTO implements Serializable {

    /**
     * tab list
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {
        /**
         * 搜索类型
         */
        private String searchType;

        /**
         * 数量
         */
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
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单code
         */
        private String soCode;


        /**
         * 来源code
         * 对应发货通知单code
         */
        private String sourceCode;

        /**
         * 来源id
         *
         */
        private String sourceId;


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
        private String orderType;

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
         * 仓库id
         */
        private String warehouseId;

        /**
         * 出货仓库
         */
        private String warehouseName;


        /**
         * 销售组织名
         */
        private String salesOrgName;

        /**
         * 发货组织
         */
        private String deliveryOrgId;

        /**
         * 发货组织名
         */
        private String deliveryOrgName;


        /**
         * 出库 日期
         */
        private LocalDate actualDeliveryDate;


        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 打包日期
         */
        private LocalDate packDate;


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
         * 单位
         */
        private String unit;

        /**
         * 应发数量
         */
        private Integer planQty;

        /**
         * 实发数量
         */
        private Integer actualQty;


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
         * 销售code
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
         * 出库日期
         */
        private List<LocalDate> actualDeliveryDateList;

        /**
         * 出库仓库
         */
        private List<String> warehouseIdList;

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
        @NotBlank(message = "销售订单不能为空")
        private String soId;

        /**
         * 来源id
         */
        private String sourceId;


        /**
         * 来源id
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 仓库id
         */
        @NotBlank(message = "出货仓库不能为空")
        private String warehouseId;

        /**
         * 仓管员
         */
        private String warehouseKeeperId;

        /**
         * 发货组织
         */
        @NotBlank(message = "发货组织不能为空")
        private String deliveryOrgId;


        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 打包日期
         */
        private LocalDate packDate;


        /**
         * 客户id
         */
        private String customerId;

        /**
         * 实际发货日期
         */
        private LocalDate actualDeliveryDate;

        /**
         * 运输单号
         */
        private String trackNo;

        /**
         * 承运商id 来源供应商
         * http://172.16.100.11:3002/project/83/interface/api/14038    categoryType=logistics
         */
        private String carrierId;

        /**
         * 详情
         */
        @Valid
        @Size(min = 1, message = "销售出库详情不能为空")
        private List<SoOutstockDetailDTO.AddDTO> detailList;


    }


    /**
     * 下推销售出库单列表
     */
    @Data
    @NoArgsConstructor
    public static class GenerateSoOutstockViewDTO {
        /**
         * 销售订单id
         */
        @NotBlank(message = "销售订单不能为空")
        private String soId;
        /**
         * 来源id
         */
        @NotBlank(message = "来源不能为空")
        private String sourceId;


        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

        /**
         * 来源code
         */
        @NotBlank(message = "来源code不能为空")
        private String sourceCode;

        /**
         * 发货组织
         */
        private String deliveryOrgId;

        /**
         * 发货组织
         */
        private String deliveryOrgName;

        /**
         * 订单类型
         */
        private String orderType;

        /**
         * 预计发货时间
         */
        private LocalDate planDeliveryDate;

        /**
         * 承运商
         */
        private String carrierId;

        /**
         * 运输单号
         */
        private String trackNo;


        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 仓库名
         */
        private String warehouseName;


        @NotBlank(message = "来源明细不能为空")
        private String sourceDetailId;

        @NotBlank(message = "sku不能为空")
        private String skuId;

        @NotBlank(message = "sku no不能为空")
        private String skuNo;


        private String warehouseLocation;


        /**
         * 箱麦附件名集合
         */
        private List<String> attachNameList;

        /**
         * 箱麦附件url集合
         */
        private List<String> attachUrlList;


        /**
         * 发货数量
         */
        @NotNull(message = "发货数量不能为空")
        @DecimalMin(value = "1", message = "发货数最小值为1")
        @DecimalMax(value = "999999999", message = "发货数最大值为999999999")
        private Integer deliveryQty;


        private String remark;


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
         * 出库单号
         */
        private String code;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单id
         */
        private String soCode;

        private ApproveStatusEnum approveStatus;

        private String approveStatusName;


        /**
         * 单据类型名
         */
        private String typeName;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;


        /**
         * 销售组织
         */
        private String salesOrgId;

        /**
         * 销售组织名
         */
        private String salesOrgName;

        /**
         * 销售员
         */
        private String sellerName;



        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 销售部门id
         */
        private String salesDeptName;

        /**
         * 发货组织
         */
        private String deliveryOrgId;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓管员
         */
        private String warehouseKeeperId;


        /**
         * 客户
         */
        private String customerName;

        /**
         * 要货日期
         */
        private LocalDate requireDate;

        /**
         * 承运商id 来源供应商
         */
        private String carrierId;


        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 打包日期
         */
        private LocalDate packDate;

        /**
         * 实际发货日期
         */
        private LocalDate actualDeliveryDate;

        /**
         * 运输单号
         */
        private String trackNo;


        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 联系电话
         */
        private String telNumber;

        /**
         * 联系地址
         */
        private String receiveAddress;


        /**
         * 交货方式
         */
        private String deliveryModeName;


        private List<SoOutstockDetailDTO.ViewDTO> detailList;

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
        @NotBlank(message = "销售出库单不能为空")
        private String id;

        /**
         * 销售订单id
         */
        @NotBlank(message = "销售订单不能为空")
        private String soId;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;


        /**
         * 单据类型
         */
        private String orderType;




        /**
         * 仓库id
         */
        @NotBlank(message = "出货仓库不能为空")
        private String warehouseId;

        /**
         * 仓管员
         */
        private String warehouseKeeperId;


        /**
         * 客户id
         */
        private String consumerId;

        /**
         * 发货组织
         */
        @NotBlank(message = "发货组织不能为空")
        private String deliveryOrgId;


        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 打包日期
         */
        private LocalDate packDate;

        /**
         * 实际发货日期
         */
        private LocalDate actualDeliveryDate;

        /**
         * 运输单号
         */
        private String trackNo;

        /**
         * 承运商id 来源供应商
         */
        private String carrierId;


        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 联系电话
         */
        private String telNumber;

        /**
         * 联系地址
         */
        private String receiverAddress;

        /**
         * 交货方式
         */
        private String deliveryModeDict;

        /**
         * 详情
         */
        @Size(min = 1, message = "销售出库详情不能为空")
        private List<SoOutstockDetailDTO.UpdateDTO> detailList;

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
         * 出库单号
         */
        private String code;

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
         * 发货组织id
         */
        private String deliveryOrgId;

        /**
         * 发货组织名
         */
        private String deliveryOrgName;


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
         * 应发数量
         */
        private Integer planQty;

        /**
         * 实发数量
         */
        private Integer actualQty;

        /**
         * 库存单位
         */
        private String unit;

        /**
         * 出货仓库id
         */
        private String warehouseId;

        /**
         * 出货仓库
         */
        private String warehouseName;


        /**
         * 出库 日期
         */
        private LocalDate outStockDate;


        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 打包日期
         */
        private LocalDate packDate;


        /**
         * 实际发货时间
         */
        private LocalDate actualDeliveryDate;


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
}
