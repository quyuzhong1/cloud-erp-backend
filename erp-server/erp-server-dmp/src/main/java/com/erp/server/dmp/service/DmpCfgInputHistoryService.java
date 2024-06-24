package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgInputHistoryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgInputHistoryDTO;

/**
 * <p>
 * 外部系统接口明细补偿 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpCfgInputHistoryService extends SuperService<DmpCfgInputHistoryEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgInputHistoryDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpCfgInputHistoryDTO.UpdateDTO dto);


}
