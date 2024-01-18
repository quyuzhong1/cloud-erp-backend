package com.erp.tms.batong.model.order.request;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname CargoVolume
 * @Description 包裹材积信息
 * @Date 2024-01-12 15:19
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoVolume implements Serializable {


    /**
     * 箱号(子单号)
     */
    @Alias("child_number")
    private String childNumber;

    /**
     * 长，1位小数，单位CM
     */
    @Alias("involume_length")
    private String inVolumeLength;


    /**
     * 宽，1位小数，单位CM
     */
    @Alias("involume_width")
    private String inVolumeWidth;


    /**
     * 高，1位小数，单位CM
     */
    @Alias("involume_height")
    private String inVolumeHeight;


    /**
     * 毛重，3位小数，单位KG
     */
    @Alias("involume_grossweight")
    private String inVolumeGrossWeight;
}
