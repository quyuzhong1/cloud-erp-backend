package com.erp.server.tms.service;

import cn.hutool.json.JSONObject;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.ImportHistoryRecordEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 导入历史记录表 服务类
 * </p>
 *
 * @author will
 * @since 2026-01-19
 */
public interface ImportHistoryRecordService extends SuperService<ImportHistoryRecordEntity> {

    /**
     * 新增
     * @author will
     * @date: 2026-01-19
     * @param dto
     * @return
     */
    BaseResultDTO.AddDTO addOrUpdate(ImportHistoryRecordDTO.AddOrUpdateDTO dto);

    /**
     * 分页列表查询
     * @author will
     * @date: 2026-01-19
     * @param pagingParamDTO
     * @return PagingVO<ImportHistoryRecordDTO.ListDTO>>
     */
    PagingVO<ImportHistoryRecordDTO.ListDTO> paging(PagingDTO<ImportHistoryRecordDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 详情
     * @author will
     * @date: 2026-01-19
     * @param id
     * @return
     */
    ImportHistoryRecordDTO.ViewDTO view(String id);
    /**
     * 预处理导入的Excel数据
     * @author will
     * @date 2026/1/20 18:43
     * @param importDTO
     * @return BatchResultDTO
     */
    BatchResultDTO preprocessingImportExcel(ImportHistoryRecordDTO.ImportSyncDTO importDTO);
    /**
     * 导入数据处理
     * @author will
     * @date 2026/1/21 15:41
     * @param successList
     * @param errorList2
     * @param headList
     * @param headMap
     * @return void
     */
    void handleImportSuccessList(ImportHistoryRecordDTO.ImportSyncDTO dto,CfgLogisticsCostImportEntity costImportEntity, List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,List<JSONObject> successList, List<JSONObject> errorList2, List<String> headList, Map<Integer, String> headMap);
    /**
     * 重新生成
     * @author will
     * @date 2026/1/26 16:33
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO regenerateImportExcel(String id,String processingType);
    /**
     * 导入
     * @author will
     * @date 2026/1/28 11:43
     * @param importSyncDTO
     * @return null
     */
    BatchResultDTO importFile(ImportHistoryRecordDTO.ImportSyncDTO importSyncDTO);

    /**
     * 导入成功后批量新增或修改物流单成本数据
     * @author will
     * @date 2026/1/29 10:00
     * @param importDataList
     * @return void
     */
    void importBatchAddOrUpdate(List<LogisticsBillCostDTO.ImportDataDTO> importDataList,String processingType);
}
