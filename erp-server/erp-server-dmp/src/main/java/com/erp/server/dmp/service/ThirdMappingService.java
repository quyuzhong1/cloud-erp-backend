package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.ThirdMappingDTO;

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
     * @author hyj
     * @date: 2024-05-17
     * @param dto
     * @return
     */
    BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO dto);

    /**
     * 预览
     * @author hyj
     * @date: 2024-05-17
     * @param viewParamDTO
     * @return
     */
    ThirdMappingDTO.MappingViewDTO view(ThirdMappingDTO.ViewParamDTO viewParamDTO);
}
