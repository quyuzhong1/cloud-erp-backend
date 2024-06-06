package com.erp.server.dmp.service;

import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.wms.dto.WarehouseDTO;

import java.util.List;

/**
 * <p>
 * 第三方系统映射关系表 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
public interface ThirdMappingService extends SuperService<ThirdMappingEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-05-17
     */
    BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO dto);

    /**
     * 预览
     *
     * @param viewParamDTO
     * @return
     * @author hyj
     * @date: 2024-05-17
     */
    ThirdMappingDTO.MappingViewDTO view(ThirdMappingDTO.ViewParamDTO viewParamDTO);

    /**
     * 根据类型和系统id获取数据
     *
     * @param type  类型
     * @param sysId 系统id
     * @return
     */
    List<ThirdMappingEntity> getList(String type, String sysId);

    void addOrUpdate(ThirdMappingEntity thirdMappingEntity, ThirdMappingEntity existMapping, WarehouseDTO.ListDTO warehouse);

    /**
     * 查找系统绑定的第三方信息
     *
     * @param thirdMappingEntity
     * @return
     */
    ThirdMappingEntity getByTypeAndSysIdAndSysType(ThirdMappingEntity thirdMappingEntity);

    /**
     * 查找第三方系统绑定的信息
     *
     * @param thirdMappingEntity
     * @return
     */
    ThirdMappingEntity getByTypeAndThirdId(ThirdMappingEntity thirdMappingEntity);

    /**
     * 根据erp内部仓库/店铺的id查询映射关系
     *
     * @param sysId erp内部仓库/店铺ID
     * @return ThirdMappingEntity 三方映射实体
     * @date: 2024-05-27
     * @author: tanmujin
     */
    ThirdWarehouseEntity getBySysId(String sysId);

    Boolean getWhetherBind(ThirdMappingDTO.ViewParamDTO viewParamDTO);

    List<ThirdMappingEntity> getByThirdId(ThirdMappingDTO.ViewParamDTO viewParamDTO);

    BaseResultDTO.AddDTO batchAdd(ThirdMappingDTO.FeignMappingDTO feignMappingDTO);
}
