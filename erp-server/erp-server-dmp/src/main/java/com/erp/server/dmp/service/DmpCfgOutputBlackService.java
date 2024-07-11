package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgOutputBlackEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgOutputBlackDTO;

/**
 * <p>
 * 输出黑名单 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-07-03
 */
public interface DmpCfgOutputBlackService extends SuperService<DmpCfgOutputBlackEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-07-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgOutputBlackDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-07-03
    * @param dto
    * @return
    */
    Boolean update(DmpCfgOutputBlackDTO.UpdateDTO dto);


}
