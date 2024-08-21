package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpPushMessageEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpPushMessageDTO;

/**
 * <p>
 * 本地消息表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-21
 */
public interface DmpPushMessageService extends SuperService<DmpPushMessageEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpPushMessageDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-21
    * @param dto
    * @return
    */
    Boolean update(DmpPushMessageDTO.UpdateDTO dto);


}
