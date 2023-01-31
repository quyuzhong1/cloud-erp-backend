package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname UpdateBomDTO
 * @Description TODO
 * @Date 2023-01-11 18:15
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UpdateBomDTO implements Serializable {


    @NotBlank(message = "id不能为空")
    private String id;



    @Valid
    private List<BomSkuDTO> skuList;
}
