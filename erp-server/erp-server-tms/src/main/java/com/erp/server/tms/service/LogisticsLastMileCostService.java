package com.erp.server.tms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description: 尾程费用 服务类
 * @author Will
 * @date: 2024/5/9 18:20
 */
public interface LogisticsLastMileCostService {

    /**
     * @description: tab列表
     * @author Will
     * @date: 2024/5/9 18:26
     * @param dto
     * @return List<TabListDTO> 
     */
    List<LogisticsBillCostDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * @description: 分页列表
     * @author Will
     * @date: 2024/5/9 18:27
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<LogisticsBillCostDTO.ListDTO> paging(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto);
    /**
     * @description: 更新数据
     * @author Will
     * @date: 2024/5/9 18:29
     * @param dto
     * @param isImport
     */
    Boolean update(LogisticsBillCostDTO.UpdateDTO dto,Boolean isImport);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2024/5/9 18:30
     * @param id
     * @return ViewDTO
     */
    LogisticsBillCostDTO.ViewDTO view(String id);
    /**
     * @description: 更新状态
     * @author Will
     * @date: 2024/5/9 18:31
     * @param id
     * @param reconciliationStatus
     * @return BatchResultDTO
     */
    BatchResultDTO updateReconciliationStatus(String id, String reconciliationStatus);
    /**
     * @description: 下载模板
     * @author Will
     * @date: 2024/5/9 18:32
     * @param response
     */
    Boolean downloadTemplate(HttpServletResponse response);
    /**
     * @description: 导入
     * @author Will
     * @date: 2024/5/9 18:32
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2024/5/9 18:32
     */
    Boolean exportExcel(LogisticsBillCostDTO.PagingParamDTO dto);

    PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsLastMileCost(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto);
}
