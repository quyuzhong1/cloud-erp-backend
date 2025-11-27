package com.erp.model.wms.dto;

import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.AttachDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * B2B三方发货单请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
*/
@Data
@NoArgsConstructor
public class B2bThirdDeliveryDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 单据编号
        */
        private String code;

        /**
        * 单据状态
         * ThirdDeliveryStatusEnum
        */
        private String status;
        /**
         * 单据状态名称
         */
        private String statusName;

        /**
        * 销售订单id
        */
        private String soId;

        /**
        * 销售订单编码
        */
        private String soCode;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 仓库组织id
        */
        private String warehouseOrgId;

        /**
        * 仓库组织名称
        */
        private String warehouseOrgName;

        /**
        * 平台订单编号
        */
        private String platformOrderCode;

        /**
        * 发货仓库id
        */
        private String deliveryWarehouseId;

        /**
        * 发货仓库名称
        */
        private String deliveryWarehouseName;
        /**
        * 虚拟仓库id
        */
        private String virtualWarehouseId;

        /**
        * 仓库操作类型
         * WarehouseOperationTypeEnum
        */
        private String warehouseOperationType;
        private String warehouseOperationTypeName;

        /**
        * 仓库操作描述
        */
        private String operationDesc;

        /**
        * 备注
        */
        private String remark;

        /**
        * 渠道id
        */
        private String logisticsChannelId;

        /**
        * 渠道名称
        */
        private String logisticsChannelName;

        /**
        * 交货方式
         * DeliveryMethodEnum
        */
        private String deliveryMethod;
        /**
         * 交货方式名称
         */
        private String deliveryMethodName;

        /**
        * 物流跟踪号
        */
        private String trackNo;

        /**
        * 收货人
        */
        private String receiverName;

        /**
        * 联系人电话
        */
        private String telNumber;
        /**
         * 国家id
         */
        private String countryId;
        /**
        * 收货国家
        */
        private String countryName;


        /**
        * 省/州
        */
        private String province;

        /**
        * 城市
        */
        private String city;

        /**
        * 邮编
        */
        private String postalCode;
        /**
         * 详细地址
         */
        private String receiveAddress;

        /**
        * 是否API发货
        */
        private Boolean isApiDelivery;
        /**
         * 产品明细
         */
        private List<B2bThirdDeliveryDetailDTO.ViewDTO> detailList;
        /**
         * 附件
         */
        private List<AttachDTO> attachList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 明细
         */
        private List<B2bThirdDeliveryDetailDTO.AddDTO> detailList;
        /**
         * 附件
         */
        private List<AttachDTO> attachList;
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 明细
         */
        private List<B2bThirdDeliveryDetailDTO.UpdateDTO> detailList;
        /**
         * 附件
         */
        private List<AttachDTO> attachList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 单据状态 
        */
//        @NotBlank(message = "单据状态 不能为空")
        @Size(max = 50,message = "单据状态 最大长度不能超过50位")
        private String status;

        /**
        * 销售订单id
        */
        @NotBlank(message = "销售订单id不能为空")
        @Size(max = 19,message = "销售订单id最大长度不能超过19位")
        private String soId;

        /**
        * 销售订单编码
        */
//        @NotBlank(message = "销售订单编码不能为空")
        @Size(max = 50,message = "销售订单编码最大长度不能超过50位")
        private String soCode;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 仓库组织id
        */
        @NotBlank(message = "仓库组织id不能为空")
        @Size(max = 19,message = "仓库组织id最大长度不能超过19位")
        private String warehouseOrgId;

        /**
        * 仓库组织名称
        */
//        @NotBlank(message = "仓库组织名称不能为空")
        @Size(max = 50,message = "仓库组织名称最大长度不能超过50位")
        private String warehouseOrgName;

        /**
        * 平台订单编号
        */
//        @NotBlank(message = "平台订单编号不能为空")
        @Size(max = 255,message = "平台订单编号最大长度不能超过255位")
        private String platformOrderCode;

        /**
        * 发货仓库id
        */
        @NotBlank(message = "发货仓库id不能为空")
        @Size(max = 19,message = "发货仓库id最大长度不能超过19位")
        private String deliveryWarehouseId;

        /**
        * 发货仓库名称
        */
//        @NotBlank(message = "发货仓库名称不能为空")
        @Size(max = 255,message = "发货仓库名称最大长度不能超过255位")
        private String deliveryWarehouseName;

        /**
        * 仓库操作类型
        */
        @NotBlank(message = "仓库操作类型不能为空")
        @Size(max = 50,message = "仓库操作类型最大长度不能超过50位")
        private String warehouseOperationType;

        /**
        * 仓库操作描述
        */
        @NotBlank(message = "仓库操作描述不能为空")
        @Size(max = 255,message = "仓库操作描述最大长度不能超过255位")
        private String operationDesc;

        /**
        * 备注
        */
//        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 渠道id
        */
        @NotBlank(message = "渠道id不能为空")
        @Size(max = 19,message = "渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 渠道名称
        */
//        @NotBlank(message = "渠道名称不能为空")
        @Size(max = 255,message = "渠道名称最大长度不能超过255位")
        private String logisticsChannelName;

        /**
        * 交货方式
         * DeliveryMethodEnum
        */
        @NotBlank(message = "交货方式不能为空")
        @Size(max = 50,message = "交货方式最大长度不能超过50位")
        private String deliveryMethod;

        /**
        * 物流跟踪号
        */
//        @NotBlank(message = "物流跟踪号不能为空")
        @Size(max = 255,message = "物流跟踪号最大长度不能超过255位")
        private String trackNo;

        /**
        * 收货人
        */
//        @NotBlank(message = "收货人不能为空")
        @Size(max = 50,message = "收货人最大长度不能超过50位")
        private String receiverName;

        /**
        * 联系人电话
        */
//        @NotBlank(message = "联系人电话不能为空")
        @Size(max = 50,message = "联系人电话最大长度不能超过50位")
        private String telNumber;

        /**
        * 收货国家
        */
//        @NotBlank(message = "收货国家不能为空")
        @Size(max = 255,message = "收货国家最大长度不能超过255位")
        private String countryName;

        /**
        * 省/州
        */
        private String province;

        /**
        * 城市
        */
        private String city;

        /**
        * 邮编
        */
        private String postalCode;

        /**
        * 是否API发货
        */
//        @NotNull(message = "是否API发货不能为空")
        private Boolean isApiDelivery;

    }


    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {

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
     * 分页视图
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 主键id
         */
        private String  id;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 单据状态
         */
        private String status;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单编码
         */
        private String soCode;

        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;

        /**
         * 仓库组织id
         */
        private String warehouseOrgId;

        /**
         * 仓库组织名称
         */
        private String warehouseOrgName;

        /**
         * 平台订单编号
         */
        private String platformOrderCode;

        /**
         * 发货仓库id
         */
        private String deliveryWarehouseId;

        /**
         * 发货仓库名称
         */
        private String deliveryWarehouseName;

        /**
         * 仓库操作类型
         */
        private String warehouseOperationType;

        /**
         * 仓库操作描述
         */
        private String operationDesc;

        /**
         * 备注
         */
        private String remark;

        /**
         * 渠道id
         */
        private String logisticsChannelId;

        /**
         * 渠道名称
         */
        private String logisticsChannelName;

        /**
         * 交货方式
         * DeliveryMethodEnum
         */
        private String deliveryMethod;

        /**
         * 物流跟踪号
         */
        private String trackNo;

        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 联系人电话
         */
        private String telNumber;

        /**
         * 收货国家
         */
        private String countryName;

        /**
         * 省/州
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 邮编
         */
        private String postalCode;

        /**
         * 是否API发货
         */
        private Boolean isApiDelivery;
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
        private List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        private List<String> ids;

        /**
         * 动态数据源
         */
        private String dynamicDataSource;
    }

    @Data
    @NoArgsConstructor
    public static class ViewQueryDTO {
        /**
         * 订单id
         */
        private String id;
        /**
         * 销售订单id
         */
        private String soId;
    }
}