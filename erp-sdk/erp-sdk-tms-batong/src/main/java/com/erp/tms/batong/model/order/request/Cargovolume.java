package com.erp.tms.batong.model.order.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname Cargovolume
 * @Description 包裹材积信息
 * @Date 2024-01-12 15:19
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cargovolume implements Serializable {


    /**
     * 箱号(子单号)
     */
    @JSONField(name = "child_number")
    private String childNumber;

    /**
     * 长，1位小数，单位CM
     */
    @JSONField(name = "involume_length")
    private String inVolumeLength;


    /**
     * 宽，1位小数，单位CM
     */
    @JSONField(name = "involume_width")
    private String inVolumeWidth;


    /**
     * 高，1位小数，单位CM
     */
    @JSONField(name = "involume_height")
    private String inVolumeHeight;


    /**
     * 毛重，3位小数，单位KG
     */
    @JSONField(name = "involume_grossweight")
    private String inVolumeGrossWeight;
}
