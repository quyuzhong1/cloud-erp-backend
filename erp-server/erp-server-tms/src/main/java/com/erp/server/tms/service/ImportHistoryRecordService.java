package com.erp.server.tms.service;

import cn.hutool.core.lang.Pair;
import cn.hutool.json.JSONObject;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.LogisticsReconMatchDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.ImportHistoryRecordEntity;

import java.time.LocalDateTime;
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
     * @return List<ImportHistoryRecordDTO.ImportConfirmDTO>
     */
    List<ImportHistoryRecordDTO.ImportConfirmDTO> handleImportSuccessList(ImportHistoryRecordDTO.ImportSyncDTO dto, CfgLogisticsCostImportEntity costImportEntity, List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList, List<JSONObject> successList, List<JSONObject> errorList2, List<String> headList, Map<Integer, String> headMap);
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
    List<ImportHistoryRecordDTO.ImportConfirmDTO> importBatchAddOrUpdate(List<LogisticsBillCostDTO.ImportDataDTO> importDataList,String processingType);

    /**
     * 物流商对账"合并 & 匹配"编排：把对账明细/费用项转为与导入一致的识别行，
     * 复用导入的匹配（识别单号 + 物流商）与落库逻辑，生成/更新物流单、物流费用单、费用项。
     * @author Will
     * @date 2026/6/11
     * @param ctx 匹配编排上下文
     * @return 逐行匹配结果（含命中的物流费用单关联，用于回写对账关联关系）
     */
    List<LogisticsReconMatchDTO.MatchResultDTO> reconMatchAndGenerate(LogisticsReconMatchDTO.MatchContextDTO ctx);

    /**
     * 加载对账整批匹配的整单级上下文（币别/汇率 Feign + 尾程费用配置），写入预加载对象。
     * 供整单匹配前调用一次，各分片复用，避免每分片重复远程/库表查询。
     * @param preload 预加载对象（方法内填充 currencyLookupMap / currencyRateMap / cfgCostList）
     */
    void fillReconMatchCurrencyContext(LogisticsReconMatchDTO.ReconMatchPreloadDTO preload);

    /**
     * 对账匹配落库（短事务，与 Feign 预查询分离）
     */
    void persistReconMatchImportData(List<LogisticsBillCostDTO.ImportDataDTO> importDataList, String processingType);
    /**
     * 更新对账状态
     * @author will
     * @date 2026/4/14 15:09
     * @param confirmPairList
     */
    void confirmImportData(ImportHistoryRecordDTO.ImportSyncDTO importDTO, List<ImportHistoryRecordDTO.ImportConfirmDTO> confirmPairList);
}
