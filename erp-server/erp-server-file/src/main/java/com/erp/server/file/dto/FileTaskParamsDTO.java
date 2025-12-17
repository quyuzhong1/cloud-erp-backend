package com.erp.server.file.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class FileTaskParamsDTO extends SortDTO {

    private static final long serialVersionUID = 6092242010755733893L;
    /**
     * 页面高级查询
     */
    private List<AdvanceQueryDTO> advanceQueryDTOList;

    /**
     * sqlMap 默认key default
     */
    private Map<String, String> sqlMap;

    /**
     * 下载任务类型
     * FileTaskTypeEnum
     * asyncImport 异步导入  asyncExport 异步导出
     */
    private String type;
}
