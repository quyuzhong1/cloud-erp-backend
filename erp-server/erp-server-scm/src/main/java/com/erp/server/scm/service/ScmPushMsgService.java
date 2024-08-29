package com.erp.server.scm.service;
import com.erp.model.scm.entity.ScmPushMsgEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.ScmPushMsgDTO;

/**
 * <p>
 * 本地推送消息表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-29
 */
public interface ScmPushMsgService extends SuperService<ScmPushMsgEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ScmPushMsgDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-29
    * @param dto
    * @return
    */
    Boolean update(ScmPushMsgDTO.UpdateDTO dto);


}
