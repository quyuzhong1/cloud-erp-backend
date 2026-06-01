package com.erp.server.tms.service;

import com.common.business.service.SuperService;
import com.erp.model.tms.dto.LogisticsReconRefLogisticsBillDTO;
import com.erp.model.tms.entity.LogisticsReconRefLogisticsBillEntity;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 物流商对账单 - 关联关系 服务类
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
public interface LogisticsReconRefLogisticsBillService extends SuperService<LogisticsReconRefLogisticsBillEntity> {

    /**
     * 物流商对账明细 - 关联关系查询（按 detail_id 集合，join 展示业务单号）
     * @author Will
     * @date: 2026/05/29
     * @param detailIds
     * @return List<LogisticsReconRefLogisticsBillDTO.ListDTO>
     */
    List<LogisticsReconRefLogisticsBillDTO.ListDTO> listByDetailIds(Collection<String> detailIds);

    /**
     * 物流商对账单 - 关联关系查询（按 main_id）
     * @author Will
     * @date: 2026/05/29
     * @param mainId
     * @return List<LogisticsReconRefLogisticsBillDTO.ListDTO>
     */
    List<LogisticsReconRefLogisticsBillDTO.ListDTO> listByMainId(String mainId);

    /**
     * 按 detail 维度整批写入关联（合并匹配 / 手动匹配 / 新增费用单 共用）
     * 写入前会先按 detailId 逻辑删旧记录，保证 uniq_detail_sub_active 唯一约束
     * @author Will
     * @date: 2026/05/29
     * @param refList
     * @return
     */
    void saveBatchByDetail(List<LogisticsReconRefLogisticsBillEntity> refList);

    /**
     * 按 detail 维度批量解绑（逻辑删除）
     * @author Will
     * @date: 2026/05/29
     * @param detailIds
     * @return
     */
    void removeByDetailIds(Collection<String> detailIds);

    /**
     * 按 detail_sub 维度批量解绑（逻辑删除）
     * @author Will
     * @date: 2026/06/01
     * @param detailSubIds
     * @return void
     */
    void removeByDetailSubIds(Collection<String> detailSubIds);

    /**
     * 按 main 维度级联逻辑删除（用于主表 batchDelete）
     * @author Will
     * @date: 2026/05/29
     * @param mainIds
     * @return
     */
    void removeByMainIds(Collection<String> mainIds);
}
