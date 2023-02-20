package com.erp.model.sys.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname FindUserByThirdDTO
 * @Description TODO
 * @Date 2022-11-14 10:21
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class FindUserByThirdDTO  implements Serializable {

    /**
     * 第三方平台类型
     */
    @NotBlank(message = "第三方绑定平台不能为空")
    @StateEnumValue(strValues = {"FS","DD","QYWX"},message = "类型有误")
    private String thirdPartyType;

    /**
     * 第三方UnionId
     */
    @NotBlank(message = "unionid 不能为空")
    private String thirdPartyUnionId;
}
