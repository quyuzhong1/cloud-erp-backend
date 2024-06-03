package com.common.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.*;
import java.time.OffsetDateTime;


/**
 * 其他出库详情DTO
 *
 * @Author Jim
 * {@code @Date} 2024/03/06
 **/
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PlatformOtherOutStockDetailDTO {

    /**
     * skuId
     */
    @NotBlank(message = "skuId不能为空")
    private String skuId;

    /**
     * SKU
     */
    @NotBlank(message = "SKU不能为空")
    private String skuNo;

    /**
     * 实发数量
     */
    @NotNull(message = "实发数量不能为空")
    @Min(value = 1,message = "实发数量最小值为1")
    @Max(value = 999999999,message = "实发数量最大值为999999999")
    private Integer actualQty;

    /**
     * 库位id
     */
    private String warehouseLocation;

    /**
     * 备注
     */
    @Size(max = 255,message = "备注不能大于255字符")
    private String remark;
}
