package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpPushMsgHisEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpPushMsgHisDTO;

/**
 * <p>
 * 本地消息表归档 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-12-16
 */
public interface DmpPushMsgHisService extends SuperService<DmpPushMsgHisEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-12-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpPushMsgHisDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-12-16
    * @param dto
    * @return
    */
    Boolean update(DmpPushMsgHisDTO.UpdateDTO dto);


}
