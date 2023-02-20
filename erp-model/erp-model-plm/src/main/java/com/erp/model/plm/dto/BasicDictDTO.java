package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname BasicDictDTO
 * @Description TODO
 * @Date 2022-09-16 11:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BasicDictDTO  implements Serializable {

    private String id;

    private String value;

    //字段类型
   // @StateEnumValue(intValues = {0, 1}, message = "账户状态只能是0或者1")
    @NotBlank(message = "类型不能为空")
    private String type;

    private String remark;

    private String name;
}
