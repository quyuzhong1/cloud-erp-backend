package com.erp.server.oms.service;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.OmsPushMsgDTO;

/**
 * <p>
 * 本地推送消息表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-22
 */
public interface OmsPushMsgService extends SuperService<OmsPushMsgEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-22
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OmsPushMsgDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-22
    * @param dto
    * @return
    */
    Boolean update(OmsPushMsgDTO.UpdateDTO dto);


}
