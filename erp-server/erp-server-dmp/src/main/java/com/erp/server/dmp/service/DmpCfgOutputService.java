package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgOutputDTO;

/**
 * <p>
 * 推送数据配置 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpCfgOutputService extends SuperService<DmpCfgOutputEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgOutputDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpCfgOutputDTO.UpdateDTO dto);


}
