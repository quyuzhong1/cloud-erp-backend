package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpInputTaskSubstatusEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpInputTaskSubstatusDTO;

/**
 * <p>
 * 拉取任务子状态 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpInputTaskSubstatusService extends SuperService<DmpInputTaskSubstatusEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpInputTaskSubstatusDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpInputTaskSubstatusDTO.UpdateDTO dto);


}
