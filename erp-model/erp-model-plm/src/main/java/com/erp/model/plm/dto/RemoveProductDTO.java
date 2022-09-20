package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname RemoveProductDTO
 * @Description TODO
 * @Date 2022-09-17 11:33
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class RemoveProductDTO implements Serializable {


    @NotBlank(message = "产品id 不能为空")
    private String productId;

    @NotBlank(message = "产品名 不能为空")
    private String productName;
}
