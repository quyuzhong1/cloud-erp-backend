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

    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        private String id;
        private String code;
        private String sourceId;
        private String sourceCode;
        private String sourceType;
        private String sourceTypeName;
        private String handleStatus;
        private String handleStatusName;
        private String handleResult;
        private String handleResultName;
        private String handleRemark;
        private String soCode;
        private String thirdDeliveryCode;
        private String thirdWarehouseOrderCode;
        private String soOutstockCode;
        private String logisticsChannelId;
        private String logisticsChannelName;
        private String transportNo;
        private String remark;
        private String handleUserId;
        private String handleUserName;
        private LocalDateTime handleTime;
        private String createUserId;
        private String createUserName;
        private LocalDateTime createTime;
        private List<SoB2bDeliveryInterceptDetailDTO.ViewDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        private List<SoB2bDeliveryInterceptDetailDTO.AddDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19, message = "来源id最大长度不能超过19位")
        private String sourceId;

        @NotBlank(message = "来源单号不能为空")
        @Size(max = 50, message = "来源单号最大长度不能超过50位")
        private String sourceCode;

        @NotBlank(message = "来源类型不能为空")
        @Size(max = 64, message = "来源类型最大长度不能超过64位")
        private String sourceType;

        @NotBlank(message = "销售单号不能为空")
        @Size(max = 50, message = "销售单号最大长度不能超过50位")
        private String soCode;

        @Size(max = 50, message = "三方发货单号最大长度不能超过50位")
        private String thirdDeliveryCode;

        @Size(max = 255, message = "海外仓单号最大长度不能超过255位")
        private String thirdWarehouseOrderCode;

        @Size(max = 19, message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        @Size(max = 255, message = "物流渠道名称最大长度不能超过255位")
        private String logisticsChannelName;

        @Size(max = 64, message = "运单号最大长度不能超过64位")
        private String transportNo;

        @NotBlank(message = "销售单id不能为空")
        @Size(max = 64, message = "销售单id最大长度不能超过64位")
        private String soId;

        @NotBlank(message = "单据类型不能为空")
        @Size(max = 64, message = "单据类型最大长度不能超过64位")
        private String billType;

        private String remark;
        private String handleStatus;
        private String handleResult;
        private String cancelStatus;
        private String interceptStatus;
        private String handleUserId;
        private String handleUserName;
        private LocalDateTime handleTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        @StateEnumValue(clazz = SoB2cDeliveryInterceptStatusEnum.class, message = "tab类型有误")
        @NotBlank(message = "tab不能为空")
        private String tabFlag;

        private Integer count;
    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        private List<AdvanceQueryDTO> advanceQueryDTOList;
        private Map<String, String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {
        private String id;
        private String code;
        private String soCode;
        private String thirdDeliveryCode;
        private String soOutstockCode;
        private String skuId;
        private String skuNo;
        private String productName;
        private Integer deliveryQty;
        private String warehouseId;
        private String warehouseName;
        private String logisticsChannelId;
        private String logisticsChannelName;
        private String billType;
        private String billTypeName;
        private String transportNo;
        private String cancelStatus;
        private String cancelStatusName;
        private String interceptStatus;
        private String interceptStatusName;
        private String handleStatus;
        private String handleStatusName;
        private String handleResult;
        private String handleResultName;
        private String remark;
        private String createUserName;
        private String handleUserName;
        private LocalDateTime createTime;
        private LocalDateTime handleTime;
        private String sourceType;
        private String sourceTypeName;
        private String platformOrderCode;
        private String thirdWarehouseOrderCode;
    }
}
