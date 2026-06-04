package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cRefDTO;
import com.erp.model.oms.entity.SoB2cRefEntity;
import com.erp.model.oms.enums.SoB2cOptionTypeEnum;

import java.util.List;


/**
 * <p>
 * B2C销售订单合并拆分关联表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cRefService extends SuperService<SoB2cRefEntity> {

    /**
     * @description: 新增关联关系
     * @author Will
     * @date: 2023/8/23 12:12
     * @param refList
     * @return Boolean
     */
    Boolean add(List<SoB2cRefDTO.AddDTO> refList);
    /**
     * @description: 新增关联关系
     * @author Will
     * @date: 2023/8/23 16:36
     * @param type
     * @param sourceId
     * @param targetId
     * @return Boolean
     */
    Boolean add(String type,String sourceId,String targetId);
    /**
     * @description: 根据目标单据id和操作类型查询
     * @author Will
     * @date: 2023/8/23 12:19
     * @param id
     * @param typeEnum
     * @return List<SoB2cRefEntity>
     */
    List<SoB2cRefEntity> listByTargetId(String id, SoB2cOptionTypeEnum typeEnum);
    /**
     * @description: 根据目标单据ids和操作类型查询
     * @author Will
     * @date: 2023/8/24 11:35
     * @param targetIdList
     * @param typeEnum
     * @return List<SoB2cRefEntity>
     */
    List<SoB2cRefEntity> listByTargetIds(List<String> targetIdList, SoB2cOptionTypeEnum typeEnum);

    /**
     * @description: 根据目标单据ids和操作类型查询来源下说有的关联关系
     * @author Will
     * @date: 2023/8/24 11:35
     * @param targetIdList
     * @param typeEnum
     * @return List<SoB2cRefEntity>
     */
    List<SoB2cRefEntity> listSourceByTargetIds(List<String> targetIdList, String typeEnum);

    /**
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/8/23 12:29
     * @param mainIds
     * @return Boolean
     */
    Boolean deleteByTargetIds(List<String> mainIds);
    /**
     * @description: 根据来源单据ids和操作类型查询
     * @author Will
     * @date: 2023/8/24 11:35
     * @param sourceIdList
     * @param typeEnum
     * @return List<SoB2cRefEntity>
     */
    List<SoB2cRefEntity> listBySourceIds(List<String> sourceIdList, SoB2cOptionTypeEnum typeEnum);
    /**
     * @description: 根据ids查询
     * @author Will
     * @date: 2023/8/25 10:53
     * @param ids
     * @return List<SoB2cRefEntity>
     */
    List<SoB2cRefEntity> listBySourceIdOrTargetId(List<String> ids);

    SoB2cRefDTO.SplitCombinationDTO getSplitCombination(String soId);

    List<String> listDeepestTargetIdsBySourceId(String id);
}
