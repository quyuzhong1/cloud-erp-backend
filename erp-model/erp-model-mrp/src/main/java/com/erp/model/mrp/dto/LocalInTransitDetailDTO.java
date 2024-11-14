package com.erp.model.mrp.dto;

import com.erp.model.mrp.enums.LocalInTransitTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class LocalInTransitDetailDTO {

    /**
     * 在途类型
     * @see LocalInTransitTypeEnum
     */
    @NotBlank(message = "本地在途类型不能为空")
    private String type;
    /**
     * 详细id
     */
    @NotBlank(message = "补货建议不能为空")
    private String id;
}
