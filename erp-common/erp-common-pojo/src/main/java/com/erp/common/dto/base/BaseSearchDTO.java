package com.erp.common.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname BaseSearchDTO
 * @Description TODO
 * @Date 2022-07-12 17:36
 * @Created by yl
 */
@NoArgsConstructor
@Data
public class BaseSearchDTO implements Serializable {

    private String searchKeyword;

    private String flagId;

}
