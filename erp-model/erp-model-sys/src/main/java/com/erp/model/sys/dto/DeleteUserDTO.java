package com.erp.model.sys.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zdy
 * @ClassName DeleteUserDTO
 * @date 2024年01月11日
 * @version: 1.0
 */
@Data
public class DeleteUserDTO implements Serializable {
    @NotNull(message = "用户id不能为空")
    private String uid;
}
