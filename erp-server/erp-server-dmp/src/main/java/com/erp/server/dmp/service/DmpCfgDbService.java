package com.erp.server.dmp.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.dmp.dto.DmpCfgDbDTO;
import com.erp.model.dmp.entity.DmpCfgDbEntity;

/**
 * <p>
 * 输入输出db信息 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpCfgDbService extends SuperService<DmpCfgDbEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgDbDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpCfgDbDTO.UpdateDTO dto);


    /**
     * 详情
     *
     * @param id
     * @return
     * @author shukai
     * @date: 2026-03-17
     */
    DmpCfgDbDTO.ViewDTO view(String id);

}
