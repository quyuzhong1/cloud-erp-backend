package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.SoB2cDeliveryInterceptStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * b2b发货拦截单请求响应实体
 * </p>
 *
 * @author Codex
 */
@Data
@NoArgsConstructor
public class SoB2bDeliveryInterceptDTO implements Serializable {

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 拦截单编号
         */
        private String code;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源类型名称
         */
        private String sourceTypeName;

        /**
         * 处理状态
         */
        private String handleStatus;

        /**
         * 处理状态名称
         */
        private String handleStatusName;

        /**
         * 处理结果
         */
        private String handleResult;

        /**
         * 处理结果名称
         */
        private String handleResultName;

        /**
         * 处理备注
         */
        private String handleRemark;

        /**
         * 销售单号
         */
        private String soCode;

        /**
         * 三方发货单号
         */
        private String thirdDeliveryCode;

        /**
         * 海外仓单号
         */
        private String thirdWarehouseOrderCode;

        /**
         * 销售出库单号
         */
        private String soOutstockCode;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;

        /**
         * 运单号
         */
        private String transportNo;

        /**
         * 备注
         */
        private String remark;

        /**
         * 处理人id
         */
        private String handleUserId;

        /**
         * 处理人名称
         */
        private String handleUserName;

        /**
         * 处理时间
         */
        private LocalDateTime handleTime;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 明细列表
         */
        private List<SoB2bDeliveryInterceptDetailDTO.ViewDTO> detailList;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 明细列表
         */
        private List<SoB2bDeliveryInterceptDetailDTO.AddDTO> detailList;
    }

    /**
     * 通用字段
     */
    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19, message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
         * 来源单号
         */
        @NotBlank(message = "来源单号不能为空")
        @Size(max = 50, message = "来源单号最大长度不能超过50位")
        private String sourceCode;

        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 64, message = "来源类型最大长度不能超过64位")
        private String sourceType;

        /**
         * 销售单号
         */
        @NotBlank(message = "销售单号不能为空")
        @Size(max = 50, message = "销售单号最大长度不能超过50位")
        private String soCode;

        /**
         * 三方发货单号
         */
        @Size(max = 50, message = "三方发货单号最大长度不能超过50位")
        private String thirdDeliveryCode;

        /**
         * 海外仓单号
         */
        @Size(max = 255, message = "海外仓单号最大长度不能超过255位")
        private String thirdWarehouseOrderCode;

        /**
         * 物流渠道id
         */
        @Size(max = 19, message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
         * 物流渠道名称
         */
        @Size(max = 255, message = "物流渠道名称最大长度不能超过255位")
        private String logisticsChannelName;

        /**
         * 运单号
         */
        @Size(max = 64, message = "运单号最大长度不能超过64位")
        private String transportNo;

        /**
         * 销售单id
         */
        @NotBlank(message = "销售单id不能为空")
        @Size(max = 64, message = "销售单id最大长度不能超过64位")
        private String soId;

        /**
         * 单据类型
         */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 64, message = "单据类型最大长度不能超过64位")
        private String billType;

        /**
         * 备注
         */
        private String remark;

        /**
         * 处理状态
         */
        private String handleStatus;

        /**
         * 处理结果
         */
        private String handleResult;

        /**
         * 取消状态
         */
        private String cancelStatus;

        /**
         * 拦截状态
         */
        private String interceptStatus;

        /**
         * 处理人id
         */
        private String handleUserId;

        /**
         * 处理人名称
         */
        private String handleUserName;

        /**
         * 处理时间
         */
        private LocalDateTime handleTime;
    }

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * tab标识
         */
        @StateEnumValue(clazz = SoB2cDeliveryInterceptStatusEnum.class, message = "tab类型有误")
        @NotBlank(message = "tab不能为空")
        private String tabFlag;

        /**
         * 标签名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 列表查询参数
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
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 拦截单编号
         */
        private String code;

        /**
         * 销售单号
         */
        private String soCode;

        /**
         * 发货单号
         */
        private String soDeliveryCode;

        /**
         * 三方发货单号
         */
        private String thirdDeliveryCode;

        /**
         * 销售出库单号
         */
        private String soOutstockCode;

        /**
         * 产品id
         */
        private String skuId;

        /**
         * 产品编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;

        /**
         * 交货方式
         */
        private String deliveryMethod;

        /**
         * 交货方式名称
         */
        private String deliveryMethodName;

        /**
         * 单据类型
         */
        private String billType;

        /**
         * 单据类型名称
         */
        private String billTypeName;

        /**
         * 运单号
         */
        private String transportNo;

        /**
         * 取消状态
         */
        private String cancelStatus;

        /**
         * 取消状态名称
         */
        private String cancelStatusName;

        /**
         * 拦截状态
         */
        private String interceptStatus;

        /**
         * 拦截状态名称
         */
        private String interceptStatusName;

        /**
         * 处理状态
         */
        private String handleStatus;

        /**
         * 处理状态名称
         */
        private String handleStatusName;

        /**
         * 处理结果
         */
        private String handleResult;

        /**
         * 处理结果名称
         */
        private String handleResultName;

        /**
         * 处理备注
         */
        private String handleRemark;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 处理人名称
         */
        private String handleUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 处理时间
         */
        private LocalDateTime handleTime;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源类型名称
         */
        private String sourceTypeName;

        /**
         * 发货单状态
         */
        private String status;

        /**
         * 发货单状态名称
         */
        private String statusName;

        /**
         * 平台订单号
         */
        private String platformOrderCode;

        /**
         * 海外仓单号
         */
        private String thirdWarehouseOrderCode;
    }
}
