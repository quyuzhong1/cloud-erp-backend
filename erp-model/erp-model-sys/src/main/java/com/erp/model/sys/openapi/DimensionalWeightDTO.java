package com.erp.model.sys.openapi;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class DimensionalWeightDTO implements Serializable {

    /**
     * 快递条码
     */
    @NotBlank(message = "快递条码不能为空")
    private String barCode;
    /**
     * 体积重（KG）
     */
    @NotNull(message = "体积重不能为空")
    private BigDecimal volumeWeight;
    /**
     * 长度（cm）
     */
    @NotNull(message = "长度不能为空")
    private BigDecimal length;
    /**
     * 宽度（cm）
     */
    @NotNull(message = "宽度不能为空")
    private BigDecimal width;
    /**
     * 高度（cm）
     */
    @NotNull(message = "高度不能为空")
    private BigDecimal height;
    /**
     * 重量（kg）
     */
    @NotNull(message = "重量不能为空")
    private BigDecimal weight;
    /**
     * 体积（m³）
     */
    @NotNull(message = "体积不能为空")
    private String volume;
    /**
     * 操作人
     */
    private String operatorId;
    /**
     * 图片路径
     */
    @NotBlank(message = "图片不能为空")
    private String imageUrl;
    /**
     * 视频路径
     */
    @NotBlank(message = "视频不能为空")
    private String videoUrl;
    /**
     * 仓库名
     */
    private String warehouseId;
}
