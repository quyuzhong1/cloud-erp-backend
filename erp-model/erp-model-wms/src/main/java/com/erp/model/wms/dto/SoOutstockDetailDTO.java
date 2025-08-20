package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * 销售出库
 *
 * @author Lambda
 * @Classname SoOutstockDTO

 * @Date 2023-05-11 10:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SoOutstockDetailDTO implements Serializable {

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
         * 销售订单code
         */
        private String soCode;


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
        private String soOrgId;

        /**
         * 销售组织名
         */
        private String soOrgName;

        /**
         * 发货组织
         */
        private String deliveryOrgId;

        /**
         * 发货组织名
         */
        private String deliveryOrgName;


        /**
         * 承运商id 来源供应商
         */
        private String carrierId;

        /**
         * 承运商 来源供应商
         */
        private String carrierName;

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
        private List<LocalDateTime> createTimeList;


    }

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        /**
         * sku編號
         */
        private String skuNo;


        /**
         * 应发数量
         */
        @DecimalMin(value = "1", message = "应发数量最小值为1")
        @DecimalMax(value = "999999999", message = "应发数量最大值")
        private Integer planQty;

        /**
         * 实发数量
         */
        @DecimalMin(value = "1", message = "实发数量最小值为1")
        @DecimalMax(value = "999999999", message = "实发数量最大值")
        private Integer actualQty;

        /**
         * 库位
         */
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
         * 备注
         */
        @Size(max = 250, message = "备注最大255个字符")
        private String remark;

        /**
         * 来源明细id
         */
        private String sourceDetailId;


        /**
         * 销售明细id
         */
        @NotBlank(message = "销售明细id不能为空")
        private String soDetailId;


        /**
         * 所有历史映射关系
         */
        private LinkedList<ListingInfoWithSkuMappingGenDTO> historySkuMappingList;

        /**
         * 第三方单据编号
         */
        private String platformCode;

        /**
         * 平台销售出库单明细ID
         */
        private String platformDetailId;

        private String platformSoDetailId;

        private String warehouseId;

        private String warehouseName;
        private String virtualWarehouseId;
        private BigDecimal price;
        private BigDecimal amount;
        private BigDecimal taxRate;
        /**
         * 汇率
         */
        private BigDecimal exchangeRate;
        /**
         * 币别（原币）
         */
        private String currency;
        public AddDTO(ListingInfoWithSkuMappingGenDTO currentSkuMappingDTO, AddDTO addDTO, Integer currentQty) {
            this.skuId = currentSkuMappingDTO.getProductSkuId();
            this.skuNo = currentSkuMappingDTO.getPlatformSkuNo();
            this.planQty = currentQty;
            this.actualQty = currentQty;
            this.warehouseId = addDTO.getWarehouseId();
            this.warehouseLocation = addDTO.getWarehouseLocation();
            this.attachNameList = addDTO.getAttachNameList();
            this.attachUrlList = addDTO.getAttachUrlList();
            this.remark = addDTO.getRemark();
            this.sourceDetailId = addDTO.getSourceDetailId();
            this.soDetailId = addDTO.getSoDetailId();
            this.historySkuMappingList = addDTO.getHistorySkuMappingList();
            this.platformCode = addDTO.getPlatformCode();
            this.platformDetailId = addDTO.getPlatformDetailId();
            this.virtualWarehouseId = addDTO.getVirtualWarehouseId();
        }
    }


    @Data
    @NoArgsConstructor
    public static class ListingInfoWithSkuMappingGenDTO {

        /**
         * skuMapping的ID
         */
        private String tableId;

        /**
         * 店铺表id
         */
        private String shopId;

        /**
         * 平台字典值
         */
        private String dictPlatform;

        /**
         * 产品sku id
         */
        private String productSkuId;

        /**
         * 产品sku no
         */
        private String productSkuNo;

        /**
         * 生效时间
         */
        private LocalDateTime effectiveTime;

        /**
         * 失效时间
         */
        private LocalDateTime expireTime;

        /**
         * 是否失效
         * true 失效
         * false 未失效
         */
        private Boolean isExpire;

        /**
         * listing_id
         */
        private String listingId;

        /**
         * 平台sku no
         */
        private String platformSkuNo;

        /**
         * 平台产品(spu) no或id
         */
        private String platformSpuNo;

        /**
         * 匹配结果吧true 已匹配 false 未匹配
         */
        private String matchResult;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * id
         */
        private String id;
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
         * 变体信息
         */
        private String variantProperty;

        /**
         * 库存单位
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
         * 及时库存
         */
        private Integer curInventoryQty;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 库位名称
         */
        private String warehouseLocationName;

        /**
         * 箱麦附件名集合
         */
        private List<String> attachNameList;

        /**
         * 箱麦附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 备注
         */
        private String remark;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 销售明细id
         */
        private String soDetailId;
    }


    @Data
    @NoArgsConstructor
    public static class DeliveryQtyDTO {

        /**
         * id
         */
        private String id;
        /**
         * sku id
         */
        private String skuId;
        /**
         * sku no
         */
        private String skuNo;


        /**
         * 应发数量
         */
        private Integer planQty;

        /**
         * 实发数量
         */
        private Integer actualQty;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;


        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 销售订单详情id
         */
        private String soDetailId;


        private String approveStatus;




    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {

        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class InfoDTO {

        /**
         * 主表id
         */
        private String mainId;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * 应发数量
         */
        private Integer planQty;

        /**
         * 实发数量
         */
        private Integer actualQty;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 是否关闭
         */
        private Boolean isClose;

        /**
         * 备注
         */
        private String remark;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 审核状态
         */
        private String approveStatus;

    }
}
