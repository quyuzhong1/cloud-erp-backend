package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsPushMsgEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsPushMsgDTO;

/**
 * <p>
 * 本地推送消息表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-11-18
 */
public interface TmsPushMsgService extends SuperService<TmsPushMsgEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-11-18
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsPushMsgDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-11-18
    * @param dto
    * @return
    */
    Boolean update(TmsPushMsgDTO.UpdateDTO dto);


}
