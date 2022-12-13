package com.erp.model.bi.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 设置仪表盘的分享信息
 *
 * @Classname
 * @Description TODO
 * @Date 2022-12-08 15:40
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UpdateSubjectShareDTO implements Serializable {


    @NotBlank(message = "表id不能为空")
    private String id;

    @NotBlank(message = "名字不能为空")
    @Size(max = 20,message = "最大20字符")
    private String name;

    @NotBlank(message = "分享标识")
    @StateEnumValue(strValues = {"personal", "share"}, message = "分享标识有误")
    private String shareFlag;

    private List<String> shareUserIdList;
}
