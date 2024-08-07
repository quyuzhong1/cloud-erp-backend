package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.oms.dto.SplitSkuDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.tms.dto.transfer.TransferCancelOrderReq;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * <p>
 * 中转报关表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@NoArgsConstructor
public class TransferDeclareDTO implements Serializable {


    /**
     * 取消订单预报实体
     */
    @Data
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class CancelOrderForecastDTO {

        /**
         * 中转物流商id
         */
        private String transferLogisticsSupplierId;

        private TransferCancelOrderReq transferCancelOrderReq;

    }
    /**
     * B2C订单预报实体
     */
    @Data
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class B2cOrderForecastDTO {

        @NotNull(message = "b2c销售订单不能为空")
        private SoB2cEntity soB2cEntity;

        @NotNull(message = "b2c销售物流订单不能为空")
        private SoB2cLogisticsEntity soB2cLogisticsEntity;

        @NotNull(message = "b2c销售订单卖家信息不能为空")
        private SoB2cReceiverEntity soB2cReceiverEntity;

        @NotNull(message = "店铺信息不能为空")
        private ShopInfoEntity shopInfoEntity;

        @NotEmpty(message = "b2c销售订单商品信息不能为空")
        private List<SplitSkuDTO> transferDeclareProductDTOList;

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
        * 单据编号
        */
        private String code;

        /**
        * 预计中转日期
        */
        private LocalDate planTransferDate;

        /**
        * 上传状态
        */
        private String uploadStatus;

        /**
        * 发货物流商id
        */
        private String deliveryLogisticsSupplierId;

        /**
        * 发货物流商中文
        */
        private String deliveryLogisticsSupplierName;

        /**
        * 中转物流商id
        */
        private String transferLogisticsSupplierId;

        /**
        * 中转物流服务商
        */
        private String transferLogisticsSupplierName;

        /**
        * 中转渠道id
        */
        private String transferChannelId;

        /**
        * 中转渠道中文
        */
        private String transferChannelName;

        /**
        * 包裹总数量
        */
        private Integer packageTotalQty;

        /**
        * 包裹总重量
        */
        private BigDecimal packageTotalWeight;

        /**
        * 包裹重量单位
        */
        private String weightUnit;

        /**
         * 详情
         */
        private List<TransferDeclareDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 生效日期（无就传当前时间LocalTime.now()）
         */
        @NotNull(message = "生成日期不能为空")
        private LocalTime generateTime;

        /**
         * 详情
         */
        private List<TransferDeclareDetailDTO.AddDTO> detailList;
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
         * 详情
         */
        private List<TransferDeclareDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 发货物流商id
        */
        @NotBlank(message = "发货物流商id不能为空")
        @Size(max = 19,message = "发货物流商id最大长度不能超过19位")
        private String deliveryLogisticsSupplierId;

        /**
        * 中转物流商id
        */
        @NotBlank(message = "中转物流商id不能为空")
        @Size(max = 19,message = "中转物流商id最大长度不能超过19位")
        private String transferLogisticsSupplierId;

        /**
        * 中转渠道id
        */
        @NotBlank(message = "中转渠道id不能为空")
        @Size(max = 19,message = "中转渠道id最大长度不能超过19位")
        private String transferChannelId;

        /**
        * 预计报关日期
        */
        private LocalDate planTransferDate;

        /**
         * 上传状态
         */
        private String uploadStatus;

    }

    /**
     * 列表查询入参
     */
    @Data
    public static class PagingParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * ids
         */
        private List<String> ids;

        /**
         * tabFlag
         * 来源：/tms/drop/down/dict/list/key=TransferDeclareTabFlag
         */
        private String tabFlag;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 销售单号
         */
        private List<String> soCodeList;

        /**
         * 物流渠道id
         * 地址：http://172.16.100.11:3002/project/128/interface/api/25999
         */
        private List<String> logisticsChannelIdList;

        /**
         * 发货物流商id
         * 地址：http://172.16.100.11:3002/project/128/interface/api/26440
         */
        private List<String> deliveryLogisticsSupplierIdList;

        /**
         * 中转物流商id
         * 地址：http://172.16.100.11:3002/project/128/interface/api/28031
         */
        private List<String> transferLogisticsSupplierIdList;

        /**
         * 上传状态
         * 来源：/tms/drop/down/dict/list/key=transferDeclareUploadStatus
         */
        private List<String> uploadStatusList;

        /**
         * 出库状态
         * 来源：/tms/drop/down/dict/list/key=transferOutstockStatus
         */
        private List<String> outstockStatusList;
        /**
         * 入库预报状态
         * 来源：/tms/drop/down/dict/list/key=instockForecastStatus
         */
        private List<String> instockForecastStatusList;

        /**
         * 中转状态
         */
        private List<String> transferStatusList;

        /**
         * 创建人
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;


        public PagingParamDTO() {
            this.ids = new ArrayList<>();
            this.logisticsChannelIdList = new ArrayList<>();
            this.deliveryLogisticsSupplierIdList = new ArrayList<>();
            this.transferLogisticsSupplierIdList = new ArrayList<>();
            this.instockForecastStatusList = new ArrayList<>();
            this.uploadStatusList = new ArrayList<>();
            this.outstockStatusList = new ArrayList<>();
            this.transferStatusList = new ArrayList<>();
            this.createUserIdList = new ArrayList<>();
            this.createTimeList = new ArrayList<>();
            this.createTimeList = new ArrayList<>();
        }
    }

    /**
     * 分页列表信息
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id[可排序]
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 中转报关单号[可排序]
         */
        private String code;
        /**
         * 预计中转时间[可排序]
         */
        private String planTransferDate;
        /**
         * 入库预报日期（推送保宏入库预报成功的日期）[可排序]
         */
        private String instockForecastDate;
        /**
         * 上传状态(批次)[可排序]
         */
        private String uploadBatchStatus;
        /**
         * 入库预报状态[可排序]
         */
        private String instockForecastStatus;
        /**
         * 入库预报状态中文
         */
        private String instockForecastStatusName;
        /**
         * 入库预报异常原因[可排序]
         */
        private String instockForecastRemark;
        /**
         * 总件数（页面录入）[可排序]
         */
        private Integer totalQty;
        /**
         * 上传状态（批次）中文
         */
        private String uploadBatchStatusName;
        /**
         * 发货物流商中文
         */
        private String deliveryLogisticsSupplierName;
        /**
         * 中转物流商中文
         */
        private String transferLogisticsSupplierName;
        /**
         * 中转渠道中文
         */
        private String transferChannelName;
        /**
         * 包裹总数量
         */
        private String packageTotalQty;
        /**
         * 包裹总重量
         */
        private String packageTotalWeight;
        /**
         * 重量单位
         */
        private String weightUnit;
        /**
         * 销售单id
         */
        private String soId;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 组包单号
         */
        private String packageForecastCode;

        /**
         * 物流渠道中文
         */
        private String logisticsChannelName;
        /**
         * 物流跟踪号
         */
        private String trackNo;
        /**
         * 上传状态(订单)
         */
        private String uploadOrderStatus;
        /**
         * 上传状态(订单)中文
         */
        private String uploadOrderStatusName;
        /**
         * 出库状态
         */
        private String outstockStatus;
        /**
         * 出库状态中文
         */
        private String outstockStatusName;
        /**
         * 中转状态
         */
        private String transferStatus;
        /**
         * 中转状态中文
         */
        private String transferStatusName;
        /**
         * 失败原因
         */
        private String failureReason;
        /**
         * 创建人[可排序]
         */
        private String createUserName;
        /**
         * 创建时间[可排序]
         */
        private LocalDateTime createTime;
        /**
         * 明细记录
         */
        private List<TransferDeclareDetailEntity> detailEntityList;
    }

    /**
     * tab页
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {
        /**
         * tab
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;

    }

    /**
     * 详情列表查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ViewDetailParamDTO {
        /**
         * 主表id
         */
        @NotBlank(message = "mainId不能为空")
        private String mainId;

        /**
         * 物流跟踪号
         */
        private String trackNo;

        /**
         * 销售单号
         */
        private String soCode;

        /**
         * 物流渠道id
         * 地址：http://172.16.100.11:3002/project/128/interface/api/25999
         */
        private List<String> logisticsChannelIdList;
    }

    /**
     * 预报设置入参
     */
    @Data
    @NoArgsConstructor
    public static class ForcastSettingParamDTO {
        /**
         * 主键id（报关设置id）
         */
        private String id;

        /**
         * 发货物流商id
         */
        @NotEmpty(message = "发货物流商id不能为空")
        private List<String> deliveryLogisticsSupplierIdList;

        /**
         * 中转物流商/中转渠道Id
         */
        @NotBlank(message = "中转渠道Id不能为空")
        private String transferChannelId;

    }

    /**
     * 第三方中转服务商发货单
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingOrderDTO {
        /**
         * 订单id
         */
        @NotBlank(message = "订单id不能为空")
        private String soId;

        /**
         * 单据编号
         */
        private String code;

        private Boolean success;

        /**
         * 第三方中转服务商的发货单号
         */
        private String shippingOrderNo;
        /**
         * 异常标识 为空则成功
         */
        private String sign;
        /**
         * 异常类型
         */
        @NotBlank(message = "异常类型不能为空")
        private String type;
        /**
         * 错误信息
         */
        private String message;

        public static ShippingOrderDTO fail(String id, String code, String msg) {
            return ShippingOrderDTO.builder()
                    .soId(id)
                    .code(code)
                    .message(msg)
                    .success(false)
                    .build();
        }

        public static ShippingOrderDTO fail(String id, String code, String msg,String type) {
            return ShippingOrderDTO.builder()
                    .soId(id)
                    .code(code)
                    .message(msg)
                    .type(type)
                    .sign(type)
                    .success(false)
                    .build();
        }
        public static ShippingOrderDTO success(String id, String code, String shippingOrderNo) {
            return ShippingOrderDTO.builder()
                    .soId(id)
                    .code(code)
                    .shippingOrderNo(shippingOrderNo)
                    .success(true)
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    public static class UpdateOutstockStatusDTO {
        /**
         * 订单id
         */
        private List<String> soIds;
        /**
         * 状态
         */
        private String status;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateForcastStatusDTO {
        /**
         * 订单id
         */
        private String soId;
        /**
         * 状态
         */
        private String status;
    }
    @Data
    @NoArgsConstructor
    public static class ExportListDTO {
        /**
         * 主键id[可排序]
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 中转报关单号[可排序]
         */
        private String code;
        /**
         * 预计中转时间[可排序]
         */
        private String planTransferDate;
        /**
         * 入库预报日期（推送保宏入库预报成功的日期）[可排序]
         */
        private String instockForecastDate;
        /**
         * 上传状态(批次)[可排序]
         */
        private String uploadBatchStatus;
        /**
         * 入库预报状态[可排序]
         */
        private String instockForecastStatus;
        /**
         * 入库预报状态中文
         */
        private String instockForecastStatusName;
        /**
         * 入库预报异常原因[可排序]
         */
        private String instockForecastRemark;
        /**
         * 总件数（页面录入）[可排序]
         */
        private Integer totalQty;
        /**
         * 上传状态（批次）中文
         */
        private String uploadBatchStatusName;
        /**
         * 发货物流商中文
         */
        private String deliveryLogisticsSupplierName;
        /**
         * 中转物流商中文
         */
        private String transferLogisticsSupplierName;
        /**
         * 中转渠道中文
         */
        private String transferChannelName;
        /**
         * 包裹总数量
         */
        private String packageTotalQty;
        /**
         * 包裹总重量
         */
        private String packageTotalWeight;
        /**
         * 重量单位
         */
        private String weightUnit;
        /**
         * 销售单id
         */
        private String soId;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 物流渠道中文
         */
        private String logisticsChannelName;
        /**
         * 物流跟踪号
         */
        private String trackNo;
        /**
         * 上传状态(订单)
         */
        private String uploadOrderStatus;
        /**
         * 上传状态(订单)中文
         */
        private String uploadOrderStatusName;
        /**
         * 出库状态
         */
        private String outstockStatus;
        /**
         * 出库状态中文
         */
        private String outstockStatusName;
        /**
         * 中转状态
         */
        private String transferStatus;
        /**
         * 中转状态中文
         */
        private String transferStatusName;
        /**
         * 失败原因
         */
        private String failureReason;
        /**
         * 创建人[可排序]
         */
        private String createUserName;
        /**
         * 创建时间[可排序]
         */
        private LocalDateTime createTime;

        /**
         * 组包单号
         */
        private String packageForecastCode;
    }
}