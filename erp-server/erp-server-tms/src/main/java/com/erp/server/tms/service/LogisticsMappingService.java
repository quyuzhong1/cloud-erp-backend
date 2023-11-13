package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsMappingDTO;

import java.util.List;

/**
 * <p>
 * 物流渠道映射表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsMappingService extends SuperService<LogisticsMappingEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dtoList
     *@param channelId
    * @return
    */
    Boolean add(String channelId, List<LogisticsMappingDTO.AddDTO> dtoList);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(LogisticsMappingDTO.UpdateDTO dto);


}
