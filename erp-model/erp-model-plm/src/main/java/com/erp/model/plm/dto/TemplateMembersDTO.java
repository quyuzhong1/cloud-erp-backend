package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 成员DTO
 * @date 2022/11/15 14:01
 */
@Data
@NoArgsConstructor
public class TemplateMembersDTO implements Serializable {

    /**
     * id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 成员id
     */
    @NotBlank(message = "成员id不能为空")
    private String memberId;
}
