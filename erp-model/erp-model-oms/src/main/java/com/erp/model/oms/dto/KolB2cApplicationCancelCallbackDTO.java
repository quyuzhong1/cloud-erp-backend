package com.erp.model.oms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * B2C KOL cancel callback payload.
 */
@Data
public class KolB2cApplicationCancelCallbackDTO implements Serializable {

    @NotBlank(message = "拆分单id不能为空")
    private String subOrderId;

    private String syncTaskId;

    private String responseMsg;
}
