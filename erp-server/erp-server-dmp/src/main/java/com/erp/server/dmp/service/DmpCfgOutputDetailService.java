package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgOutputDetailDTO;

/**
 * <p>
 * 推送数据配置明细 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpCfgOutputDetailService extends SuperService<DmpCfgOutputDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgOutputDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpCfgOutputDetailDTO.UpdateDTO dto);
}
