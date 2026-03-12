package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * b2b发货拦截单详情请求响应实体
 * </p>
 *
 * @author Codex
 */
@Data
@NoArgsConstructor
public class SoB2bDeliveryInterceptDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        private String id;
        private String mainId;
        private String skuId;
        private String skuNo;
        private String productName;
        private Integer deliveryQty;
        private String warehouseId;
        private String warehouseName;
        private String warehouseLocation;
        private String sourceDetailId;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {
        @NotBlank(message = "主键id不能为空")
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        @NotBlank(message = "产品id不能为空")
        @Size(max = 19, message = "产品id最大长度不能超过19位")
        private String skuId;

        @NotBlank(message = "产品编号不能为空")
        @Size(max = 64, message = "产品编号最大长度不能超过64位")
        private String skuNo;

        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19, message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 255, message = "仓库名称最大长度不能超过255位")
        private String warehouseName;

        private String warehouseLocation;

        @NotBlank(message = "来源详情id不能为空")
        @Size(max = 19, message = "来源详情id最大长度不能超过19位")
        private String sourceDetailId;
    }
}
