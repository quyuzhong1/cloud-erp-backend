package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpInputTaskStatusRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpInputTaskStatusRecordDTO;

/**
 * <p>
 * 拉取任务状态记录 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpInputTaskStatusRecordService extends SuperService<DmpInputTaskStatusRecordEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpInputTaskStatusRecordDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpInputTaskStatusRecordDTO.UpdateDTO dto);


}
