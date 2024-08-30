package com.erp.server.wms.service;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WmsPushMsgDTO;

/**
 * <p>
 * 本地推送消息表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-22
 */
public interface WmsPushMsgService extends SuperService<WmsPushMsgEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-22
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(WmsPushMsgDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-22
    * @param dto
    * @return
    */
    Boolean update(WmsPushMsgDTO.UpdateDTO dto);


}
