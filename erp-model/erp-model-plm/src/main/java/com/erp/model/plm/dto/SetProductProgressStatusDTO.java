package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname SetProductProgressStatusDTO
 * @Description TODO
 * @Date 2023-02-23 18:09
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SetProductProgressStatusDTO  implements Serializable {

    @StateEnumValue(strValues = {"normal","postpone","risk","no"},message = "进展状态有误")
    private String progressStatus;


    @NotBlank(message = "产品id不能为空")
    private String productId;
}
