package com.common.business.dto.base;

import com.common.core.anno.StateEnumValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/15 18:16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveOneDTO extends PermissionsDTO {

    /**
     * 主键id集合
     */
    @NotEmpty(message = "审核id不能为空")
    private String id;

    /**
     * 类型（pass、审核通过，reject、审核不通过）
     */
    @NotBlank(message = "审核类型不能为空")
    @StateEnumValue(strValues = {"pass","reject","reject_appoint","revoke"}, message = "审核类型有误")
    private String type;

    /**
     * 意见
     */
    @Size(max = 255, message = "审核意见最大255个字符")
    private String comment;

    /**
     * 是否需要流程，false则跳过
     */
    private Boolean isNeedProcess;


    /**
     * 是否是pc端访问
     */
    private Boolean pcShow = false;



    public ApproveOneDTO (String id,String type,String comment) {
        this.id = id;
        this.type = type;
        this.comment = comment;

    }
}
