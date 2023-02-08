package com.erp.common.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

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
public class SortDTO implements Serializable {


    private List<SortParamDTO>  sortList;
}
