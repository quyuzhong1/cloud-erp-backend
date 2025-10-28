package com.erp.server.dmp.service;
import com.common.business.service.SuperService;

import java.util.List;

import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.AdsPushTaskDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.entity.doris.AdsPushTaskEntity;

/**
 * <p>
 * ads推送任务 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-10-28
 */
public interface AdsPushTaskService extends SuperService<AdsPushTaskEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-10-28
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AdsPushTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-10-28
    * @param dto
    * @return
    */
    Boolean update(AdsPushTaskDTO.UpdateDTO dto);

    /**
     * 获取 tab列表
     * @Author Luo_WG
     * @Date 2024/9/3 14:53
     * @param dto
     * @return java.util.List<com.erp.model.dmp.dto.DmpOutputTaskDTO.TabListDTO>
     **/
    List<DmpOutputTaskRecordDTO.TabListDTO> tabList(PermissionsDTO dto);
}
