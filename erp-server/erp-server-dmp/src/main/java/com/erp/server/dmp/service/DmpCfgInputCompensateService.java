package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgInputCompensateEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgInputCompensateDTO;

/**
 * <p>
 * 外部系统接口明细补偿 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-27
 */
public interface DmpCfgInputCompensateService extends SuperService<DmpCfgInputCompensateEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgInputCompensateDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-27
    * @param dto
    * @return
    */
    Boolean update(DmpCfgInputCompensateDTO.UpdateDTO dto);


}
