package com.erp.server.tms.service;
import com.erp.model.tms.entity.RemotePostcodeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.RemotePostcodeDTO;

/**
 * <p>
 * 偏远邮编组 服务类
 * </p>
 *
 * @author jack
 * @since 2024-11-29
 */
public interface RemotePostcodeService extends SuperService<RemotePostcodeEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RemotePostcodeDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    Boolean update(RemotePostcodeDTO.UpdateDTO dto);


}
