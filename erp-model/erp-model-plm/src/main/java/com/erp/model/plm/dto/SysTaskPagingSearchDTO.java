package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.common.business.dto.base.BaseSearchDTO;

/**
 * @author Will
 * @version 1.0
 * @description: 系统任务列表查询
 * @date 2022/11/21 14:24
 */
@NoArgsConstructor
@Data
public class SysTaskPagingSearchDTO extends BaseSearchDTO {

    /**
     * 1立项模板（查立项任务），2默认模板（查非立项任务）
     */
    private String type;

    /**
     * 模板id
     */
    private String templateId;

}
