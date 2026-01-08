package com.erp.model.wms.dto;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.AttachDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.enums.WarehouseOperationTypeEnum;
import io.seata.common.util.StringUtils;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
         * 客户名称
         */
        private String customerName;
        private String customerId;

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
         */
        private List<WarehouseOperationTypeDTO> warehouseOperationTypeDTOList;

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
         * DeliveryModeEnum
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
        private String postCode;
        /**
         * 详细地址
         */
        private String receiveAddress;
        /**
         * 地址2
         */
        private String address2;

        /**
         * 地址3
         */
        private String address3;
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
        private List<WmsAttachmentDTO.UpdateDTO> attachList;
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
        @Size(max = 1,message = "附件最大数量不能超过1个")
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
        private List<B2bThirdDeliveryDetailDTO.AddDTO> detailList;
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

        private String virtualWarehouseId;

        private List<WarehouseOperationTypeDTO> warehouseOperationTypeDTOList;
        /**
        * 备注
        */
//        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 渠道id
        */
//        @NotBlank(message = "渠道id不能为空")
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
         * DeliveryModeEnum
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
        private String countryId;

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
        private String postCode;
        /**
         * 详细地址
         */
        private String receiveAddress;
        /**
         * 地址2
         */
        private String address2;

        /**
         * 地址3
         */
        private String address3;
        private String customerId;
        private String customerName;

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
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
         * 单据编号 [可排序]
         */
        private String code;

        /**
         * 单据状态[可排序]
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
         * 收货人
         */
        private String receiverName;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;

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
         * 发货仓库名称[可排序]
         */
        private String deliveryWarehouseName;

        /**
         * 仓库操作类型[可排序]
         * WarehouseOperationTypeEnum
         */
        private String warehouseOperationType;
        private String warehouseOperationTypeName;


        /**
         * 交货方式[可排序]
         * DeliveryModeEnum
         */
        private String deliveryMethod;
        /**
         * 交货方式名称
         */
        private String deliveryMethodName;

        /**
         * 是否API发货
         */
        private Boolean isApiDelivery;
        /**
         * 产品明细
         */
        /**
         * 明细id
         */
        private String  detailId;

        /**
         * 产品id
         */
        private String skuId;

        /**
         * 产品编号
         */
        private String skuNo;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 单箱数量
         */
        private Integer perBoxQty;

        /**
         * 发货sku
         */
        private String deliverySkuNo;

        /**
         * 发货skuid
         */
        private String deliverySkuId;

        /**
         * 三方仓SKU
         */
        private String warehousePlatformSku;

        /**
         * 发货箱数
         */
        private Integer boxQty;

        /**
         * 异常原因[可排序]
         */

        private String errorMessage;
        /**
         * 推送类型
         * B2BDeliveryPushTypeEnum
         */
        private String pushType;

        private String pushTypeName;
        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 备注
         */
        private String remark;
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
    public static class ViewQueryDTO extends BaseIdDTO {
        /**
         * 订单id
         */
        private String id;
        /**
         * 销售订单id
         */
        private String soId;
        /**
         * 销售订单明细id
         */
        private List<String> soDetailIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseOperationTypeDTO {

        private String warehouseOperationType;

        private String warehouseOperationTypeName;

        private String operationDesc;

        public static WarehouseOperationTypeDTO getDefault(){
            return new WarehouseOperationTypeDTO(WarehouseOperationTypeEnum.NO_OPEN_RELABLE.getCode(),WarehouseOperationTypeEnum.NO_OPEN_RELABLE.getName(),"");
        }

        public static List<WarehouseOperationTypeDTO> convert(String warehouseOperationType,String operationDesc){
            if(StringUtils.isBlank(warehouseOperationType)){
                return new ArrayList<>();
            }
            List<String> splitWarehouseOperationType =  Arrays.asList(warehouseOperationType.split(","));
            List<String> splitOperationDesc =  Arrays.asList(operationDesc.split(","));
            List<WarehouseOperationTypeDTO> list = new ArrayList<>();
            for (int i = 0; i < splitWarehouseOperationType.size(); i++) {
                String type = splitWarehouseOperationType.get(i);
                String name = WarehouseOperationTypeEnum.getName(type);
                String desc = splitOperationDesc.size()<= i ? "" : splitOperationDesc.get(i);
                list.add(new WarehouseOperationTypeDTO(type,name,desc));
            }
            return list;
        }
    }
}