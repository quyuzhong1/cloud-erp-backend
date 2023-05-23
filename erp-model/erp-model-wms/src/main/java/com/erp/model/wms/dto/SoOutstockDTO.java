package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
        private String type;

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
         * 详情
         */
        @Valid
        @Size(min = 1, message = "销售出库详情不能为空")
        private List<SoOutstockDetiailDTO.AddDTO> detailList;


    }



    /**
     * 下推销售出库单列表
     */
    @Data
    @NoArgsConstructor
    public static class GenerateSoOutstockViewDTO {
        /**
         * id
         */
        private String id;
        /**
         * 主键id
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
         * 客户
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
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 要货日期
         */
        private LocalDate requireDate;
        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;
        /**
         * 附件名集合
         */
        private List<String> attachNameList;
        /**
         * 附件url集合
         */
        private List<String> attachUrlList;
        /**
         * 备注
         */
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
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单id
         */
        private String soCode;



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


        private List<SoOutstockDetiailDTO.ViewDTO> detailList;

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
        private String type;


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
        private List<SoOutstockDetiailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }
}
