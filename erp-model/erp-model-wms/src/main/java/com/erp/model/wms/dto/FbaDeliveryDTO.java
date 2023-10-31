package com.erp.model.wms.dto;

import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * FBI发货单请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@NoArgsConstructor
public class FbaDeliveryDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {
         /**
          * 标识
          * 描述：waitSubmit:待提交, approveIng:审核中, reject:审核不通过, approve:已审核
          */
         @StateEnumValue(clazz = ApproveStatusEnum.class, message = "tab类型有误")
         @NotBlank(message = "tab不能为空")
         private String tabFlag;

         /**
         * 数量
         */
         private Integer count;

     }
     /**
     * 分页列表查询参数
     */
     @Data
     @NoArgsConstructor
     public static class PagingParamDTO extends SortDTO {
         /**
         * tab：/wms/common/enumDropDown?type=ApproveStatusEnum
         * 描述：waitSubmit:待提交, approveIng:审核中, reject:审核不通过, approve:已审核
         */
         private String  tabFlag;
         /**
         * 审核时间
         */
         private List<String> approveTimeList;
         /**
         * 单据编号
         */
         private String code;
         /**
         * sku编号
         */
         private List<String> skuNoList;
         /**
         * 来源单号
         */
         private List<String> sourceCodeList;
         /**
          * 备货类型:/wms/common/enumDropDown?type=FbaDemandType
          * 描述：demandPlatformWarehouse:备货平台仓  demandOverseasWarehouse:备货海外仓
          */
         private String demandType;
         /**
         * 店铺id
         */
         private List<String> shopIdList;
         /**
         * 国家二字码
         */
         private List<String> countryIdList;
         /**
         * 发货仓id
         */
         private List<String> deleverWarehouseIdList;
         /**
         * 目的仓id
         */
         private List<String> destWarehouseIdList;
         /**
         * 审核状态
         */
         private List<String> approveStatusList;
         /**
         * 物流方式
         */
         private List<String> logisticsMethodList;
         /**
         * 是否组合品
         */
         private Boolean isCombo;
         /**
         * 平台sku
         */
         private String asin;
         /**
         * fnSku
         */
         private String fnSku;
         /**
         * 卖家sku
         */
         private String mSku;
         /**
         * 创建时间
         */
         private List<String> createTimeList;
     }

    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键Id
         */
        private String id;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 来源单号
         */
        private List<String> sourceCodeList;

        /**
         * 备货类型
         */
        private List<String> demandTypeList;

        /**
         * 备货类型名称
         */
        private String demandTypeName;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 作废状态
         */
        private String invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 国家
         */
        private String countryId;

        /**
         * 国家名称
         */
        private String countryName;

        /**
         * 发货仓id
         */
        private String deleverWarehouseId;

        /**
         * 发货仓名称
         */
        private String deleverWarehouseName;

        /**
         * 目的仓id
         */
        private String destWarehouseId;

        /**
         * 目的仓名称
         */
        private String destWarehouseName;

        /**
         * 物流方式
         */
        private String logisticsMethod;

        /**
         * 物流方式名称
         */
        private String logisticsMethodName;

        /**
         * 平台sku
         */
        private String asin;

        /**
         * 卖家sku
         */
        private String mSku;

        /**
         * FNSKU
         */
        private String fnSku;

        /**
         * ERP的SKU
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 库存sku
         */
        private String stockSku;

        /**
         * 应发数量
         */
        private Integer planQty;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建用户名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 审核人（最新）
         */
        private String approveUserName;

        /**
         * 审核时间
         */
        private String approveTime;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
    }

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
        * code
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
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人
        */
        private String approveUserName;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源类型名称
        */
        private String sourceTypeName;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 备货类型
        */
        private String demandType;

        /**
        * 备货类型名称
        */
        private String demandTypeName;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 国家二字码
        */
        private String countryId;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 发货仓id
        */
        private String deliveryWarehouseId;

        /**
        * 发货仓名称
        */
        private String deliveryWarehouseName;

        /**
        * 目的仓id
        */
        private String destWarehouseId;

        /**
        * 目的仓名称
        */
        private String destWarehouseName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 平台物流中心
        */
        private String fulfillmentCenter;

        /**
        * 库存组织id
        */
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        private String inventoryOrgName;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 物流信息
         */
        private FbaDeliveryLogisticsDTO.ViewDTO logisticsView;

        /**
         * 产品信息
         */
        private List<FbaDeliveryDetailDTO.ViewDTO> itemList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

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
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 来源编码
        */
        @NotBlank(message = "来源编码不能为空")
        @Size(max = 50,message = "来源编码最大长度不能超过50位")
        private String sourceCode;

        /**
        * 备货类型
        */
        @NotBlank(message = "备货类型不能为空")
        @Size(max = 64,message = "备货类型最大长度不能超过64位")
        private String demandType;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 64,message = "店铺id最大长度不能超过64位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255,message = "店铺名称最大长度不能超过255位")
        private String shopName;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 10,message = "国家二字码最大长度不能超过10位")
        private String countryId;

        /**
        * 国家名称
        */
        @NotBlank(message = "国家名称不能为空")
        @Size(max = 64,message = "国家名称最大长度不能超过64位")
        private String countryName;

        /**
        * 发货仓id
        */
        @NotBlank(message = "发货仓id不能为空")
        @Size(max = 19,message = "发货仓id最大长度不能超过19位")
        private String deliveryWarehouseId;

        /**
        * 发货仓名称
        */
        @NotBlank(message = "发货仓名称不能为空")
        @Size(max = 255,message = "发货仓名称最大长度不能超过255位")
        private String deliveryWarehouseName;

        /**
        * 目的仓id
        */
        @NotBlank(message = "目的仓id不能为空")
        @Size(max = 19,message = "目的仓id最大长度不能超过19位")
        private String destWarehouseId;

        /**
        * 目的仓名称
        */
        @NotBlank(message = "目的仓名称不能为空")
        @Size(max = 255,message = "目的仓名称最大长度不能超过255位")
        private String destWarehouseName;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 500,message = "备注最大长度不能超过500位")
        private String remark;

        /**
        * 平台物流中心
        */
        @NotBlank(message = "平台物流中心不能为空")
        @Size(max = 64,message = "平台物流中心最大长度不能超过64位")
        private String fulfillmentCenter;

        /**
        * 库存组织id
        */
        @NotBlank(message = "库存组织id不能为空")
        @Size(max = 19,message = "库存组织id最大长度不能超过19位")
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        @NotBlank(message = "库存组织名称不能为空")
        @Size(max = 255,message = "库存组织名称最大长度不能超过255位")
        private String inventoryOrgName;


    }

    /**
     * 更新物流信息列表查询
     */
    @Data
    @NoArgsConstructor
    public static class DeliveryLogisticsView {
        /**
         * 主键id
         */
        private String id;
        /**
         * 发货单id
         */
        private String mainId;
        /**
         * 发货单号
         */
        private String deliverCode;
        /**
         * 物流方式
         */
        private String logisticsMethod;
        /**
         * 物流方式
         */
        private String logisticsMethodName;
        /**
         * 物流渠道
         */
        private String logisticsChannel;
        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;
        /**
         * 物流跟踪号
         */
        private String trackingNo;
        /**
         * 发货时间
         */
        private LocalDateTime deliverTime;

    }

    /**
     * 下推加工单列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateMachineView {
        /**
         * 主键id
         */
        private String id;
        /**
         * 事务类型
         */
        private String workType;
        /**
         * 事务类型名称
         */
        private String workTypeName;
        /**
         * ERP的SKU
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 仓库Id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 仓位名称
         */
        private String warehouseLocationName;
        /**
         * 组装数量
         */
        private Integer assembleQty;
        /**
         * bom版本
         */
        private Integer bomVersion;
        /**
         * 子件信息
         */
        private List<SonItem> sonItemList;
    }

    /**
     * 子件信息
     */
    @Data
    @NoArgsConstructor
    public static class SonItem {
        /**
         * 子sku
         */
        private String sonSkuNo;
        /**
         * bom用量
         */
        private String quantity;
        /**
         * 子件数量
         */
        private String sonQty;
        /**
         * 及时库存
         */
        private Integer curInventoryQty;
    }

    /**
     * 打印子件产品列表
     */
    @Data
    @NoArgsConstructor
    public static class PrintSonItem {
        /**
         * 主键id
         */
        private String id;
        /**
         * 发货单号
         */
        private String code;
        /**
         * 组合sku
         */
        private String skuNo;
        /**
         * 组合产品名称
         */
        private String productName;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 子件信息
         */
        private List<PrintSonItemDetail> sonItemList;
    }

    /**
     * 子件产品详情信息
     */
    @Data
    @NoArgsConstructor
    public static class PrintSonItemDetail {
        /**
         * 子sku
         */
        private String sonSkuNo;
        /**
         * 子sku产品名称
         */
        private String sonProductName;
        /**
         * bom用量
         */
        private String quantity;
        /**
         * 子件发货数量
         */
        private Integer sonDeliveryQty;
    }
}