package com.cloud.erp.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class FileTaskParamsDTO extends PermissionsDTO {

    private String event;
    private Boolean owner;
    private String createUserId;
}
