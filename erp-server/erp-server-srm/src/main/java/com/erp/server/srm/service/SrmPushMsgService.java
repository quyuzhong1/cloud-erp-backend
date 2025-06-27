package com.erp.server.srm.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.srm.dto.SrmPushMsgDTO;
import com.erp.model.srm.entity.SrmPushMsgEntity;

/**
 * <p>
 * 本地推送消息表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-22
 */
public interface SrmPushMsgService extends SuperService<SrmPushMsgEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-22
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SrmPushMsgDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-22
    * @param dto
    * @return
    */
    Boolean update(SrmPushMsgDTO.UpdateDTO dto);


}
