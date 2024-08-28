package com.erp.server.plm.service;
import com.erp.model.plm.entity.PlmPushMsgEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.PlmPushMsgDTO;

/**
 * <p>
 * 本地推送消息表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-27
 */
public interface PlmPushMsgService extends SuperService<PlmPushMsgEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PlmPushMsgDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    Boolean update(PlmPushMsgDTO.UpdateDTO dto);


}
