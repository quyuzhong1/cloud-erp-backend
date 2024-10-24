package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.StateEnumValue;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 头程发货单请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@NoArgsConstructor
public class FirstMileDeliveryDTO implements Serializable {

    /**
     * 统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsReq {

        private LocalDateTime beginDate;

        private LocalDateTime endDate;

        private String status;

        private String orderType;

        private String permissionSql;
    }

    /**
     * 统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogisticStatisticsDTO {

        /**
         * 年份
         */
        private Integer year;

        /**
         * 月份
         */
        private Integer month;

        /**
         * 数量
         */
        private Integer count = 0;
    }


    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusDTO {
        @NotNull(message = "ids不能为空")
        private List<String> ids;
        /**
         * 物流单状态
         */
        private String logisticsStatus;

        /**
         * 报关单状态
         */
        private String declareStatus;
    }

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
          * 页面高级查询
          */
         private List<AdvanceQueryDTO> advanceQueryDTOList;

         /**
          * sqlMap 默认key default
          */
         private Map<String, String> sqlMap;
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
         * 装箱任务id
         */
        private String taskId;
        /**
         * 明细主键Id
         */
        private String detailId;
        /**
         * 编号
         */
        private String code;
        /**
         * 物流单状态编码
         */
        private String logisticsStatus;
        /**
         * 物流单状态中文
         */
        private String logisticsStatusName;

        /**
         * 报关单状态编码
         */
        private String declareStatus;
        /**
         * 报关单状态中文
         */
        private String declareStatusName;

        /**
         * 服务商编码
         */
        private String provideCode;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 入库单号
         */
        private String overseasInboundCode;

        /**
         * 备货类型
         */
        private String demandType;

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
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * 装箱状态
         */
        private String packingStatus;

        /**
         * 装箱状态名称
         */
        private String packingStatusName;

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
        private String platformSpuNo;

        /**
         * 卖家sku
         */
        private String platformSkuNo;

        /**
         * 第三方仓SKU
         */
        private String thirdWarehouseSku;

        /**
         * FNSKU
         */
        private String fnSku;

        /**
         * ERP的SKU主键
         */
        private String skuId;

        /**
         * ERP的SKU
         */
        private String skuNo;

        /**
         * 是否组合产品
         */
        private Boolean isCombination;

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
         * 装箱数量
         */
        private Integer packingQty;

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
        private LocalDateTime createTime;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 待审核人
         */
        private String waitApproveUserName;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
         * FBA货件编码
         */
        private String fbaShipmentCode;

    }
    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListFirstMileDTO {
        /**
         * 发货单id
         */
        private String sourceId;
        /**
         * 发货单号 【可排序】
         */
        private String sourceCode;
        /**
         * 发货单明细主键Id
         */
        private String sourceDetailId;
        /**
         * 发货单类型（默认 delivery）
         */
        private String sourceType;

        /**
         * 业务单号【可排序】
         */
        private String businessCode;
        /**
         * FBA
         */
        private String fbaShipmentCode;
        /**
         * 海外仓入库编号
         */
        private String overseasWarehouseCode;

        /**
         * 业务单号类型
         */
        private String businessType;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称【可排序】
         */
        private String shopName;

        /**
         * 发货仓库id
         */
        private String warehouseId;

        /**
         * 发货仓库名称【可排序】
         */
        private String warehouseName;

        /**
         * ERP的SKU主键
         */
        private String skuId;

        /**
         * ERP的SKU【可排序】
         */
        private String skuNo;
        /**
         * 平台sku
         */
        private String platformSkuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 应发数量【可排序】
         */
        private Integer planQty;

        /**
         * 实发数量【可排序】
         */
        private Integer deliveryQty;

        /**
         * 装箱数量
         */
        private Integer packingQty;
    }

    /**
    * 导出Excel
    */
    @EqualsAndHashCode(callSuper = true)
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
         * 中转仓库集合
         */
        private List<String> transferWarehouseIdList;

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
        private FirstMileDeliveryDTO.ViewLogisticDTO logisticsView;

        /**
         * 产品信息
         */
        private List<FirstMileDeliveryDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @EqualsAndHashCode(callSuper = true)
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

        /**
         * 产品信息
         */
        private List<FirstMileDeliveryDetailDTO.AddDTO> detailList;
    }

    /**
    * 修改
    */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 中转仓库集合
         */
        private List<String> transferWarehouseIdList;
        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 产品信息
         */
        private List<FirstMileDeliveryDetailDTO.UpdateDTO> detailList;
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
        @NotBlank(message = "发货仓id不能为空")
        @Size(max = 19,message = "发货仓id最大长度不能超过19位")
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

    }

    /**
     * 下推加工单列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateMachineView {
        /**
         * 明细id
         */
        private String id;
        /**
         * 主表id
         */
        private String mainId;
        /**
         * 发货单号
         */
        private String code;
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
        private String skuId;
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
        private String bomVersion;
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
         * 明细id
         */
        private String id;
        /**
         * 子sku
         */
        private String sonSkuNo;
        /**
         * bom用量
         */
        private Integer quantity;
        /**
         * 子件数量
         */
        private Integer sonQty;
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
        private Integer quantity;
        /**
         * 子件发货数量
         */
        private Integer sonDeliveryQty;
    }

    /**
     * 根据版本获取子件详情信息
     */
    @Data
    @NoArgsConstructor
    public static class SonItemDetailByVersion {
        /**
         * 主键id(取值：下推列表的id)
         */
        private String id;

        /**
         * bom版本
         */
        private String bomVersion;
    }

    /**
     * 发货记录
     */
    @Data
    @NoArgsConstructor
    public static class DeliverRecordView {
        /**
         * 发货单号
         */
        private String deliveryCode;
        /**
         * 发货状态编码
         */
        private String deliveryStatus;
        /**
         * 发货状态名称
         */
        private String deliveryStatusName;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;

        /**
         * 货件/入库单号
         */
        private String refCode;
    }

    /**
     * 分组汇总sku
     */
    @Data
    @NoArgsConstructor
    public static class GroupSkuDTO {
        /**
         * 发货单id
         */
        private String id;
        /**
         * 箱子id
         */
        private String cartonId;
        /**
         * 产品id
         */
        private String skuId;

        /**
         * 产品编号
         */
        private String skuNo;

        /**
         * 产品产品名称
         */
        private String productName;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 待装箱数量
         */
        private Integer waitPackQty;

        /**
         * 装箱数量
         */
        private Integer packQty;
    }

    /**
     * 生成状态修改入参
     */
    @Data
    @NoArgsConstructor
    public static class GenerateStatusUpdateDTO {
        /**
         * 单据id
         */
        private List<String> ids;
        /**
         * 单据类型
         * 接口：/wms/dict/list?key=fmDeliveryBillType
         */
        private List<String> billTypes;
    }

    /**
     * 生成物流单传的DTO
     */
    @Data
    @NoArgsConstructor
    public static class GenerateLogisticReqDTO {

        /**
         * 装箱状态
         */
        private String packingStatus;

        /**
         * 物流单状态
         */
        private String logisticsStatus;

        /**
         * 发货单ids
         */
        private List<String> ids;
        /**
         * 搜索发货单编码
         */
        private String searchKey;
    }
    /**
     * 生成物流单传的DTO
     */
    @Data
    @NoArgsConstructor
    public static class GenerateLogisticDTO {

        /**
         * 来源id（海外仓，FBA）
         */
        private String sourceId;

        /**
         * 来源编号（海外仓，FBA）
         */
        private String sourceCode;

        /**
         * 来源类型（海外仓，FBA）
         */
        private String sourceType;

        /**
         * 发货单id
         */
        private String outstockId;

        /**
         * 发货单单号
         */
        private String outstockCode;

        /**
         * 店铺Id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 店铺负责人
         */
        private String chargeId;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
         * 发货仓库ID
         */
        private String fromWarehouseId;
        /**
         * 发货仓库名称
         */
        private String fromWarehouseName;

        /**
         * 发货国家
         */
        private String fromCountryName;

        /**
         * 发货详细地址
         */
        private String fromAddress;

        /**
         * 目的仓库名称
         */
        private String toWarehouseName;

        /**
         * 目的国家
         */
        private String toCountry;

        /**
         * 目的国家
         */
        private String toCountryName;

        /**
         * 目的详细地址
         */
        private String toAddress;


        /**
         * 备注
         */
        private String remark;

        /**
         * 装箱信息
         */
        private List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDTOList;
    }

    /**
     * 物流详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewLogisticDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 发货单号
         */
        private String deliveryCode;

        /**
         * 物流方式:/wms/common/enumDropDown?type=LogisticsMethod
         * 描述：airfreight:空运, express:快递, oceanFreightBulk:海运散装
         * , oceanFreightFCL:海运整箱, railwayTransportationBulk:铁运散装
         * , railwayTransportationFCL:铁运整箱
         */
        private String logisticsMethod;

        /**
         * 物流方式名称
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
         * 发货时间
         */
        private LocalDateTime deliveryTime;

        /**
         * 备注
         */
        private String logisticsRemark;

        /**
         * 物流运单号
         */
        private List<String> trackingNoList;

        /**
         * 发货地址
         */
        private String deliveryFromAddress;

        /**
         * 收货地址
         */
        private String receiveToAddress;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RequestReceiveDTO {
        /**
         * 业务单号
         */
        private List<String> businessCodes;
        /**
         * 发货单号
         */
        private List<String> sourceCodes;
        /**
         * 发货单id集合
         */
        private List<String> deliveryIds;
        /**
         * 日期
         */
        private LocalDate month;
    }
    @Data
    @NoArgsConstructor
    public static class ReceiveDTO {
        /**
         * 业务单号
         */
        private String businessCode;
        private String skuId;
        private String skuNo;
        private String platformSkuNo;
        /**
         * 上月签收数量
         */
        private Integer lastMonthReceiveQty;
        /**
         * 本月签收数量
         */
        private Integer currentMonthReceiveQty;
        /**
         * 截止本月签收数量
         */
        private Integer asCurrentMonthReceiveQty;
        /**
         * 截止上月签收数量
         */
        private Integer asLastMonthReceiveQty;
    }

    @Data
    @NoArgsConstructor
    public static class BusinessDTO {
        /**
         * 发货单id
         */
        private String id;
        private String code;
        /**
         * 发货单明细id
         */
        private String detailId;
        /**
         * 业务单号
         */
        private String businessCode;
        /**
         * FBA单号
         */
        private String fbaShipmentCode;
        /**
         * 第三方发货单号
         */
        private String overseasWarehouseCode;
    }
}