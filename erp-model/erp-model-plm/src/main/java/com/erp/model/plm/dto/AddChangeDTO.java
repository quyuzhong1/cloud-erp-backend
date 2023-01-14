package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname AddChangeDTO
 * @Description TODO
 * @Date 2023-01-14 14:54
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class AddChangeDTO implements Serializable {


    @NotBlank(message = "源数据id不能为空")
    private String sourceId;

    @NotBlank(message = "变更类型不能为空")
    private String type;


    @NotBlank(message = "变更实体json 字符串不能为空")
    private String  detailsJson;

}
