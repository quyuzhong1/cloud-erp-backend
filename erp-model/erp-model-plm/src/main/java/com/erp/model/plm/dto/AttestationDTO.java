package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname AttestationDTO
 * @Description TODO
 * @Date 2023-02-27 11:18
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class AttestationDTO implements Serializable {


    private String id;

    private String skuId;
    /**
     * 字典表id不能为空
     */
    @NotBlank(message = "字典表id不能为空")
    private String dictId;

    @NotBlank(message = "值不能为空")
    private String dictValue;

    private String type;
}
