package com.erp.server.tms.service;

import com.common.business.service.SuperService;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.entity.TmsCostDetailEntity;
import com.erp.model.tms.enums.DictCostAttributionEnum;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 自发货费用明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-03-20
 */
public interface TmsCostDetailService extends SuperService<TmsCostDetailEntity> {

    /**
     * @description: 批量新增
     * @author Will
     * @date: 2024/3/22 14:41
     * @param costDetailList
     * @param mainId
     * @return Boolean
     */
    Boolean batchAdd(List<TmsCostDetailDTO.AddDTO> costDetailList, String mainId, DictCostAttributionEnum dictCostAttributionEnum);


    /**
     * @description: 批量更新
     * @author Will
     * @date: 2024/3/22 14:41
     * @param costDetailList
     * @param mainId
     * @return Boolean
     */
    Boolean batchUpdate(List<TmsCostDetailDTO.UpdateDTO> costDetailList, String mainId, DictCostAttributionEnum dictCostAttributionEnum,Boolean isImport);


    /**
     * 查询预估与实际比对列表

     * @return
     */
    List<TmsCostDetailDTO.CostCompareDTO> getCostCompareListByIds(List<String> ids);

    /**
     * @description: 根据主表id集合查询
     * @author Will
     * @date: 2024/3/25 11:49
     * @param mainIdList
     * @return List<TmsLogisticsBillCostDetailEntity>
     */
    List<TmsCostDetailEntity> listByMainIdList(List<String> mainIdList);
    /**
     * @description: 查询费用
     * @author Will
     * @date: 2024/3/25 9:17
     * @param mainIdList
     * @return List<CostViewDTO>
     */
    List<TmsCostDetailDTO.CostViewDTO> listCostByMainIdList(List<String> mainIdList);
    /**
     * @description: 根据主表id集合查询
     * @author Will
     * @date: 2024/3/25 14:16
     * @param mainIdList
     */
    void deleteByMainIdList(List<String> mainIdList);

    /**
     * 根据主表id和配置id统计每个配置id的费用
     */
    List<TmsCostDetailEntity> sumCostByMainIdAndCostId(String logisticsBillCostType, Collection<String> logisticsBillIds, String sourceType);
    /**
     * @description: 根据费用配置id集合查询
     * @author Will
     * @date: 2024/4/2 16:31
     * @param cfgCostIdList 
     * @return List<TmsCostDetailEntity> 
     */
    List<TmsCostDetailEntity> listByCfgCostIdList(List<String> cfgCostIdList);

    boolean updateActual0ByMainId(List<String> delActualCostIds);

    /**
     * 根据主键删除费用记录明细
     * @param costIds
     */
    void removeByMainIds(List<String> costIds);
}
