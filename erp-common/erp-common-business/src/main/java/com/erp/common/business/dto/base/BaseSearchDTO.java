package com.erp.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname BaseSearchDTO
 * @Description TODO
 * @Date 2022-07-12 17:36
 * @Created by yl
 */
@NoArgsConstructor
@Data
public class BaseSearchDTO extends PermissionsDTO {

    /**
     * 搜索关键字
     * @author yl
     * @date 2022-10-09 10:38
     * @param null
     * @return
     */
    private String searchKeyword;

    private String flagId;

}
