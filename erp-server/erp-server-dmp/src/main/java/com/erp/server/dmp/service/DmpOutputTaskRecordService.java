package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;

/**
 * <p>
 * 推送任务记录 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpOutputTaskRecordService extends SuperService<DmpOutputTaskRecordEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpOutputTaskRecordDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpOutputTaskRecordDTO.UpdateDTO dto);


}
