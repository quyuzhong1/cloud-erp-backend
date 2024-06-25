package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpBasicSystemDTO;

/**
 * <p>
 * 外部系统 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpBasicSystemService extends SuperService<DmpBasicSystemEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpBasicSystemDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpBasicSystemDTO.UpdateDTO dto);


}
