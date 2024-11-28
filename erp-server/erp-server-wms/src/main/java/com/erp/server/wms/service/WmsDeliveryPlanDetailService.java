package com.erp.server.wms.service;
import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import com.erp.model.wms.entity.WmsDeliveryPlanDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 发货计划详情表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface WmsDeliveryPlanDetailService extends SuperService<WmsDeliveryPlanDetailEntity> {

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/11/17 12:23
     * @param addDTO
     * @param mainId
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    void add(WmsDeliveryPlanDTO.AddDTO addDTO, String mainId);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    void update(WmsDeliveryPlanDTO.UpdateDTO dto, String mainId);


    /**
     * 根据主表id查询明细
     * @Author Luo_WG
     * @Date 2023/11/17 14:03
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.OverseasDeliveryPlanDetailEntity>
     **/
    List<WmsDeliveryPlanDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据主表id删除明细
     * @Author Luo_WG
     * @Date 2023/11/17 15:26
     * @param mainIds
     * @return void
     **/
    Boolean removeByMainIds(List<String> mainIds);
    /**
     * 根据发货计划明细查询
     * @author will
     * @date 2024/10/23 10:01
     * @param idList
     * @return List<WmsDeliveryPlanDetailEntity>
     */
    List<WmsDeliveryPlanDetailEntity> listBySourceIdList(List<String> idList);
}
