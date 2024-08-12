package com.erp.server.dmp.service;
import cn.hutool.core.date.DateTime;
import com.erp.model.dmp.entity.DmpCfgInputConvertValueEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgInputConvertValueDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-08
 */
public interface DmpCfgInputConvertValueService extends SuperService<DmpCfgInputConvertValueEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-08-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgInputConvertValueDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-08-08
    * @param dto
    * @return
    */
    Boolean update(DmpCfgInputConvertValueDTO.UpdateDTO dto);

    /**
    * 查询映射key和值
    * @author Luo_WG
    * @date: 2024-08-08
    * @return
    */
    List<DmpCfgInputConvertValueDTO.MappingAndValueDTO> listMappingAndValue();

    /**
    * 根据更新时间查询数据用于缓存
    * @author Luo_WG
    * @date: 2024-08-08
    * @return
    */
    List<DmpCfgInputConvertValueDTO.MappingAndValueDTO> listMappingAndValueByFreshCacheTime(DateTime freshCacheTime);


}
