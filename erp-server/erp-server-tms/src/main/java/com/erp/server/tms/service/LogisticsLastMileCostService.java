package com.erp.server.tms.service;

import cn.hutool.json.JSONObject;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.server.tms.listener.LogisticsLastMileCostExcelListener;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    BatchResultDTO updateReconciliationStatus(String id, String reconciliationStatus , LocalDateTime confirmTime);
    /**
     * @description: 下载模板
     * @author Will
     * @date: 2024/5/9 18:32
     * @param response
     */
    Boolean downloadTemplate(HttpServletResponse response);

    void handleImportSuccessList(List<JSONObject> successList, List<JSONObject> errorList, List<String> headList, Map<Integer, String> headMap);

    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2024/5/9 18:32
     */
    Boolean exportExcel(LogisticsBillCostDTO.PagingParamDTO dto);

    PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsLastMileCost(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto);

    Boolean importExcel(BaseDTO.ImportDTO dto);

    void importLogisticsLastMileCost(BaseDTO.ImportDTO dto);
}
