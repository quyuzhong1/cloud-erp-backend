package com.erp.model.bi.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class BiBatchShareDTO implements Serializable {

    /**
     * 主实体IDS
     */
    @NotEmpty(message = "ids不能为空")
    private List<String> ids;

    /**
     * 分享标示
     * share 按多用户ID共享
     * role 按多角色ID
     */
    @NotBlank(message = "分享类型不能为空")
    @StateEnumValue(strValues = {"personal","share","role"},message = "分享类型有误")
    private String shareFlag= "share";

    /**
     * 分享的身份id列表(用户ID/角色ID)
     */
    @NotNull(message = "分享的身份id列表(shareFlagIdList)不能为空")
    @Size(min = 1, message = "分享的身份id列表数量至少为1")
    private List<@NotBlank(message = "分享的身份id不能为空") String> shareFlagIdList;

}
