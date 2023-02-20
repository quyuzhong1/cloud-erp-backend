package com.erp.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname SortDTO
 * @Description TODO
 * @Date 2023-02-08 11:50
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SortDTO extends PermissionsDTO implements Serializable {

    @Valid
    private List<SortParamDTO>  sortList;
}
