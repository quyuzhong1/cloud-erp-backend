package com.erp.model.bi.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname LayoutDTO
 * @Description TODO
 * @Date 2022-12-09 16:19
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class LayoutDTO  implements Serializable {




    /**
     * 高度
     */
    @NotNull(message = "高度不能为空")
    private Integer height=0;

    /**
     * 块的编号
     */
    @NotBlank(message = "模块编号不能为空")
    @StateEnumValue(strValues = {"L01","L02","L04","L06"},message = "模块编号有误")
    private String blockNo;


    /**
     * 模块id集合
     */
    private List<LayoutRefModuleDTO>  moduleIdList;
}
