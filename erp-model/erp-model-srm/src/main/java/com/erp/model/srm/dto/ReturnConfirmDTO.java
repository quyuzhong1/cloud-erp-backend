package com.erp.model.srm.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author zdy
 * @ClassName OrderAcceptDTO
 * @description: TODO
 * @date 2024年01月11日
 * @version: 1.0
 */
@Data
public class ReturnConfirmDTO implements Serializable {
    /**
     * 主键id
     */
    private String id;
    /**
     * 时长
     */
    private String duration;
    /**
     * 单位
     */
    private String unit;
    /**
     * 选中状态 0 没有  1 有
     */
    private int selectState;

    /**
     * Key值
     */
    private String key;

    /**
     * json数据
     */
    private String dataJson;

    /**
     * 是否禁用
     */
    private Boolean disabled;

    /**
     * 排序字段
     */
    @NotNull(message = "排序字段不能为空")
    private Integer index;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空")
    @Size(max = 255,message = "备注最大长度不能超过255位")
    private String remark;

    /**
     * 供应商id
     */
    private String supplierId;
}
