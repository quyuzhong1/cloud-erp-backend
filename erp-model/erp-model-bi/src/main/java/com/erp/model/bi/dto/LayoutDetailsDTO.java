package com.erp.model.bi.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 *  布局详情
 * @Classname
 * @Description TODO
 * @Date 2022-12-13 17:23
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class LayoutDetailsDTO  implements Serializable {

    /**
     * 高度
     */
    private Integer height=0;


    /**
     * 列的个数
     */
    private Integer columnCount=0;


    /**
     * 块的编号
     */
    @NotBlank(message = "模块编号不能为空")
    @StateEnumValue(strValues = {"L01","L02","L04","L06"},message = "模块编号有误")
    private String blockNo;


    /**
     * 模块id集合
     */
    private List<String> moduleIdList;

}
