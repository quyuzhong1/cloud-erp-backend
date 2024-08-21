package com.erp.server.wms.service;
import com.erp.model.wms.entity.WmsLocalPushMessageEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WmsLocalPushMessageDTO;

/**
 * <p>
 * 本地推送消息表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-21
 */
public interface WmsLocalPushMessageService extends SuperService<WmsLocalPushMessageEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(WmsLocalPushMessageDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-21
    * @param dto
    * @return
    */
    Boolean update(WmsLocalPushMessageDTO.UpdateDTO dto);


}
