package com.erp.server.wms.service;
import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.erp.model.wms.dto.StocktakingPlanDetailDTO;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.enums.StocktakingTypeEnum;

import java.util.List;


/**
 * <p>
 * 盘点计划明细表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
 */
public interface StocktakingPlanDetailService extends SuperService<StocktakingPlanDetailEntity> {

    /**
     * 保存盘点计划明细
     * @param detailList
     * @param mainId
     */
    void saveList(List<StocktakingPlanDTO.DetailDTO> detailList, String mainId);

    /**
     * 更新盘点计划明细
     * @param detailList
     * @param mainId
     */
    void updateList(List<StocktakingPlanDTO.DetailDTO> detailList, String mainId);

    /**
     * 根据主表id查询明细
     * @param mainId
     * @return
     */
    List<StocktakingPlanDetailEntity> listByMainId(String mainId);

    /**
     * 根据主表id删除明细
     * @param mainId
     * @return
     */
    Boolean removeByMainId(String mainId);

    /**
     * 根据主表id和盘点类型查询明细
     * @param mainId
     * @param type
     * @return
     */
    List<StocktakingPlanDetailDTO.ViewDTO> listByMainIdAndType(String mainId, StocktakingTypeEnum type);
}
