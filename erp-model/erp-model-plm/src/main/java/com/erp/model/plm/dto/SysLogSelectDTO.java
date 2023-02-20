package com.erp.model.plm.dto;

import com.erp.common.business.dto.base.BaseSearchDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 21:26
 */
@Data
@NoArgsConstructor
public class SysLogSelectDTO extends BaseSearchDTO {

    /**
     * 表名路径
     */
    @NotNull(message = "表名路径不能为空")
    private List<String> classPaths;

    /**
     * 业务id(对应模块id)
     */
    private String businessId;

    /**
     * 父级id(用于综合数据查询)
     */
    private String pid;
}
