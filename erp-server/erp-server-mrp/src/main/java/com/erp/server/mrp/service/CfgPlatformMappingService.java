package com.erp.server.mrp.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.erp.model.mrp.dto.CfgPlatformMappingDTO;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;

import java.util.List;

/**
 * <p>
 * 平台映射表 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
public interface CfgPlatformMappingService extends SuperService<CfgPlatformMappingEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-08-29
    * @param updateList
    * @return
    */
    Boolean update(ValidList<CfgPlatformMappingDTO.UpdateDTO> updateList);

    /**
     * 远程下拉
     * @author will
     * @date 2024/8/29 17:17
     * @param dto
     * @return List<ListDTO>
     */
    List<CfgPlatformMappingDTO.ListDTO> selectPlatformMapping(CfgPlatformMappingDTO.SelectDTO dto);

    /**
     * 获取启用得平台数据
     */
    List<CfgPlatformMappingEntity> listByEffective();
    /**
     * 根据平台类型查询有效数据
     * @author will
     * @date 2024/9/3 17:56
     * @param platformType 
     * @return List<CfgPlatformMappingEntity>
     */
    List<CfgPlatformMappingEntity> listByPlatformType(String platformType);
    /**
     * 根据平台类型查询所有数据
     * @author will
     * @date 2024/10/16 17:13
     * @param platformType
     * @return List<CfgPlatformMappingEntity>
     */
    List<CfgPlatformMappingEntity> listAllByPlatformType(String platformType);
    /**
     * 查询详情
     * @author will
     * @date 2024/10/15 10:01
     * @return MainViewDTO
     */
    CfgPlatformMappingDTO.MainViewDTO view();
    /**
     * 根据平台类型查询
     * @author will
     * @date 2024/10/16 17:11
     * @param platformType
     * @return ViewDTO
     */
    CfgPlatformMappingDTO.ViewDTO getByPlatformType(String platformType);

    /**
     * 根据平台映射获取具体平台
     * @param code
     */
    List<String> listEffectiveByPlatform(String code);
}
