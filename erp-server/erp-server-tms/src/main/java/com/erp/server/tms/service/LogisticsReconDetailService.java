package com.erp.server.tms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsReconDetailDTO;
import com.erp.model.tms.dto.excel.LogisticsReconMatchImportExcelDTO;
import com.erp.model.tms.entity.LogisticsReconDetailEntity;
import com.erp.server.tms.service.support.LogisticsReconImportMatchContext;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 物流商对账明细（行级） 服务类
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
public interface LogisticsReconDetailService extends SuperService<LogisticsReconDetailEntity> {

    /**
     * 物流商对账明细分页列表查询
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return PagingVO<LogisticsReconDetailDTO.ListDTO>
     */
    PagingVO<LogisticsReconDetailDTO.ListDTO> paging(PagingDTO<LogisticsReconDetailDTO.PagingParamDTO> dto);

    /**
     * 物流商对账明细列表数量合计（按 match_status 分组 TAB）
     * @author Will
     * @date: 2026/05/29
     * @param param
     * @return List<LogisticsReconDetailDTO.TabListDTO>
     */
    List<LogisticsReconDetailDTO.TabListDTO> tabList(LogisticsReconDetailDTO.TabListParamDTO param);

    /**
     * 物流商对账明细导出（异步：提交文件中心下载任务）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return void
     */
    void exportList(LogisticsReconDetailDTO.PagingParamDTO dto);

    /**
     * 物流商对账导入匹配（标准异步导入：上传 Excel，控制层逐文件提交导入任务）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> importMatch(LogisticsReconDetailDTO.ImportMatchDTO dto);

    /**
     * 导入匹配异步任务执行：解析 Excel（与手动匹配字段一致），逐费用项按 ERP 单号匹配
     * @author Will
     * @date: 2026/06/11
     * @param dto 单文件异步参数
     * @return void
     */
    void executeImportMatchTask(LogisticsReconDetailDTO.ImportMatchSyncDTO dto);

    /**
     * 导入匹配分批处理：按模板识别号分组合并费用项后认领并异步提交匹配。
     *
     * @author Will
     * @date 2026/6/12
     * @param context            导入匹配上下文（识别号分组后的账单费用项）
     * @param excelBatch         本批 Excel 行
     * @param groupErrorMap      识别号分组键 → 错误文案（可累积）
     * @param matchedGroupKeySet 本文件已命中账单识别组的键（可累积）
     * @param handledGroupKeySet 本文件已处理过的识别组（跨分批去重，可累积）
     */
    void processImportMatchBatch(LogisticsReconImportMatchContext context,
                                 List<LogisticsReconMatchImportExcelDTO> excelBatch,
                                 Map<String, List<String>> groupErrorMap,
                                 Set<String> matchedGroupKeySet,
                                 Set<String> handledGroupKeySet);

    /**
     * 物流商对账明细手动匹配（按 detailId：明细下全部未匹配费用项共用同一组 ERP 业务单号）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> manualMatch(LogisticsReconDetailDTO.ManualMatchDTO dto);

    /**
     * 按主表 id 级联逻辑删除（用于主表 batchDelete）
     * @author Will
     * @date: 2026/05/29
     * @param mainIds
     * @return
     */
    void removeByMainIds(Collection<String> mainIds);

    /**
     * 下载物流商对账明细导入模板
     * @author Will
     * @date: 2026/06/08
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);
}
