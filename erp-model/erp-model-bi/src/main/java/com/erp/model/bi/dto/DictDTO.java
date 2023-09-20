package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zdy
 * @ClassName DictDTO
 * @description: 字典请求
 * @date 2023年09月20日
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class DictDTO implements Serializable {

    /**
     * 表id
     */
    private String id;
    /**
     * 值
     */
    @NotBlank(message = "字典值不能为空")
    private String value;
    /**
     * 类型
     */
    @NotBlank(message = "字典类型不能为空")
    private String type;
    /**
     * 名
     */
    @NotBlank(message = "字典名称不能为空")
    private String name;
    /**
     * 备注
     */
    private String remark;
    /**
     * 序号
     */
    private Integer order_index;
    /**
     * 状态0 未开启 1 已开启
     */
    @NotNull(message = "字典状态不能为空")
    private Integer state;

}
