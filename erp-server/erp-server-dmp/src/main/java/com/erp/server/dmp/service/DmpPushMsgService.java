package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpPushMsgDTO;

/**
 * <p>
 * 本地消息表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-22
 */
public interface DmpPushMsgService extends SuperService<DmpPushMsgEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-22
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpPushMsgDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-22
    * @param dto
    * @return
    */
    Boolean update(DmpPushMsgDTO.UpdateDTO dto);


}
