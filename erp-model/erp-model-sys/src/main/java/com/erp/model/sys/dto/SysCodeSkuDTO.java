package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0

 * @date 2022/11/21 11:54
 */
@Data
@NoArgsConstructor
public class SysCodeSkuDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 类目
     */
    private String category;

    /**
     * 顺序码
     */
    private Integer num;

    /**
     * 编码类型
     */
    @NotBlank(message = "编码类型不能为空")
    private String type;

}
