package com.erp.server.dmp.service;
import java.util.List;
import java.util.Map;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.dmp.dto.DmpCfgInputConvertMappingDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertMappingEntity;

/**
 * <p>
 * 转换映射 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-20
 */
public interface DmpCfgInputConvertMappingService extends SuperService<DmpCfgInputConvertMappingEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgInputConvertMappingDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    Boolean update(DmpCfgInputConvertMappingDTO.UpdateDTO dto);

    Map<String , List<String>> getMapping(String mainId);
}
