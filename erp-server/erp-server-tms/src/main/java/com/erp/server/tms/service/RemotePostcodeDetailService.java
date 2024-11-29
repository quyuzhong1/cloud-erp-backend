package com.erp.server.tms.service;
import com.erp.model.tms.entity.RemotePostcodeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.RemotePostcodeDetailDTO;

/**
 * <p>
 * 偏远邮编明细表 服务类
 * </p>
 *
 * @author jack
 * @since 2024-11-29
 */
public interface RemotePostcodeDetailService extends SuperService<RemotePostcodeDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RemotePostcodeDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    Boolean update(RemotePostcodeDetailDTO.UpdateDTO dto);


}
