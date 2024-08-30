package com.erp.server.sys.service;
import com.erp.model.sys.entity.SysPushMsgEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysPushMsgDTO;

/**
 * <p>
 * 本地推送消息表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-27
 */
public interface SysPushMsgService extends SuperService<SysPushMsgEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SysPushMsgDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    Boolean update(SysPushMsgDTO.UpdateDTO dto);


}
