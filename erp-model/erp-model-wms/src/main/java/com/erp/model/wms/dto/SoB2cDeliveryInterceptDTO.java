package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.SoB2cDeliveryInterceptStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * b2c发货拦截单请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
*/
@Data
@NoArgsConstructor
public class SoB2cDeliveryInterceptDTO implements Serializable {

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
        * 单据状态 waitHandle:待处理 handle:已处理 cancel:已取消
        */
        private String handleStatus;

        /**
        * 单据状态中文
        */
        private String handleStatusName;

        /**
        * 处理结果 success：拦截成功  failure：拦截失败
        */
        private String handleResult;

        /**
        * 处理结果中文
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
        * 发货单号
        */
        private String soDeliveryCode;

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
        * 创建人
        */
        private String createUserId;

        /**
        * 创建人名称
        */
        private String createUserName;

        /**
        * 创建人时间
        */
        private LocalDateTime createTime;

        /**
         * 详情
         */
        private List<SoB2cDeliveryInterceptDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 详情
         */
        private List<SoB2cDeliveryInterceptDetailDTO.AddDTO> detailList;
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
        private List<SoB2cDeliveryInterceptDetailDTO.UpdateDTO> detailList;
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
        * 来源单号
        */
        @NotBlank(message = "来源单号不能为空")
        @Size(max = 50,message = "来源单号最大长度不能超过50位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 64,message = "来源类型最大长度不能超过64位")
        private String sourceType;

        /**
        * 销售单号
        */
        @NotBlank(message = "销售单号不能为空")
        @Size(max = 50,message = "销售单号最大长度不能超过50位")
        private String soCode;

        /**
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        @Size(max = 19,message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 物流渠道名称
        */
        @NotBlank(message = "物流渠道名称不能为空")
        @Size(max = 255,message = "物流渠道名称最大长度不能超过255位")
        private String logisticsChannelName;

        /**
        * 运单号
        */
        @NotBlank(message = "运单号不能为空")
        @Size(max = 64,message = "运单号最大长度不能超过64位")
        private String transportNo;

        /**
        * 销售单id
        */
        @NotBlank(message = "销售单id不能为空")
        @Size(max = 64,message = "销售单id最大长度不能超过64位")
        private String soId;

        /**
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 64,message = "单据类型最大长度不能超过64位")
        private String billType;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 标识：wms/common/enumDropDown?type=SoB2cDeliveryInterceptStatus
         * 描述：waitHandle:待处理, handle:已处理, cancel:已取消
         */
        @StateEnumValue(clazz = SoB2cDeliveryInterceptStatusEnum.class, message = "tab类型有误")
        @NotBlank(message = "tab不能为空")
        private String tabFlag;

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
         * 单据状态
         */
        private String tabFlag;
        /**
         * 单据编号
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
         * 销售出库单号
         */
        private String soOutstockCode;
        /**
         * sku编号
         */
        private List<String> skuNoList;
        /**
         * 物流渠道名称
         */
        private List<String> logisticsChannelIdList;
        /**
         * 运单号
         */
        private String transportNo;
        /**
         * 取消状态：wms/common/enumDropDown?type=CancelStatus
         * 描述：success:取消成功, failure:取消失败
         */
        private String cancelStatus;
        /**
         * 拦截状态：wms/common/enumDropDown?type=InterceptStatus
         * 描述：success:拦截成功, failure:拦截失败
         */
        private String interceptStatus;
        /**
         * 处理状态：wms/common/enumDropDown?type=SoB2cDeliveryInterceptStatus
         * 描述：waitHandle:待处理 handle:已处理 cancel:已取消
         */
        private String handleStatus;
        /**
         * 处理结果：wms/common/enumDropDown?type=HandleResult
         * 描述：success:拦截成功, failure:拦截失败
         */
        private String handleResult;
        /**
         * 创建人
         */
        private List<String> createUserIdList;
        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;
    }

    /**
     * 列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 发货单编号
         */
        private String code;
        /**
         * 订单编号
         */
        private String soCode;
        /**
         * 订单发货单号
         */
        private String soDeliveryCode;
        /**
         * 销售出库单号
         */
        private String soOutstockCode;
        /**
         * skuId
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
         * 处理结果中文
         */
        private String handleResultName;
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
    }

    /**
     * 拦截处理仓库DTO
     */
    @Data
    @NoArgsConstructor
    public static class InterceptInventoryDTO {

        /**
         * 拦截单号
         */
        private String interceptCode;

        /**
         * 发货单号
         */
        private String deliveryCode;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 拣货明细id
         */
        private String pickDetailId;

        /**
         * 拦截单明细Id
         */
        private String interceptDetailId;
        /**
         * 拦截单Id
         */
        private String id;

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;


        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 拣货仓位
         */
        private String pickWarehouseLocation;

        /**
         * 返还仓位
         */
        private String warehouseLocation;

        /**
         * 拣货仓位名称
         */
        private String pickWarehouseLocationName;

        /**
         * 返还仓位名称
         */
        private String warehouseLocationName;
        /**
         * 数量
         */
        private Integer qty;
    }

    /**
     * 拦截结果确认DTO
     */
    @Data
    @NoArgsConstructor
    public static class InterceptSuccessDTO {
        //拦截单id
        private List<String> ids;

        //明细处理弹窗
        private List<SoB2cDeliveryInterceptDTO.InterceptInventoryDTO> interceptInventoryDTOList = new ArrayList<>();

        /**
         * 备注
         */
        private String resultRemark;
    }

    /**
     * 拦截结果确认DTO
     */
    @Data
    @NoArgsConstructor
    public static class InterceptFailureDTO {
        //拦截单id
        private List<String> ids;
        /**
         * 是否自动出库
         */
        private Boolean isAutoOut;
        /**
         * 备注
         */
        private String resultRemark;

    }

    /**
     * 拦截结果确认DTO
     */
    @Data
    @NoArgsConstructor
    public static class InterceptResultConfirmDTO {
        /**
         * 单据id
         */
        private List<String> ids;
        /**
         * 处理结果：wms/common/enumDropDown?type=HandleResult
         * 描述 success：拦截成功，failure：拦截失败
         */
        private String handleResult;
        /**
         * 处理结果描述
         */
        /**
         * 处理结果描述
         */
        private String resultRemark;

        /**
         * 是否海外仓
         */
        private boolean isThirdWarehouse;

    }

    /**
     * 拦截结果确认DTO
     */
    @Data
    @NoArgsConstructor
    public static class ConfirmDTO {
        /**
         * 单据id
         */
        private String ids;
        /**
         * 处理结果：wms/common/enumDropDown?type=HandleResult
         * 描述 success：拦截成功，failure：拦截失败
         */
        private String handleResult;
        /**
         * 处理结果描述
         */
        private String resultRemark;
    }

    /**
     * 拦截标识
     */
    @Data
    @NoArgsConstructor
    public static class IsInterceptDTO {
        /**
         * 订单id
         */
        private String id;
        /**
         * 确认结果状态
         */
        private String handleResult;
        /**
         * 是否拦截
         */
        private Boolean isIntercept;
    }
}