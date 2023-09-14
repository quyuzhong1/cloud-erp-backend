package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSplitErrorLogEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSplitErrorLogDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-09-14
 */
public interface DmpSplitErrorLogService extends SuperService<DmpSplitErrorLogEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-09-14
    * @param dto
    * @return
    */
    String add(DmpSplitErrorLogDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-09-14
    * @param dto
    * @return
    */
    Boolean update(DmpSplitErrorLogDTO.UpdateDTO dto);


}
