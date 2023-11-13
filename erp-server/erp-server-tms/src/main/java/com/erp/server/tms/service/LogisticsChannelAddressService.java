package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsChannelAddressEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsChannelAddressDTO;

import java.util.List;

/**
 * <p>
 * 渠道地址表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsChannelAddressService extends SuperService<LogisticsChannelAddressEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param list
    * @return
    */
    Boolean add(String channelId, List<LogisticsChannelAddressDTO.AddDTO> list);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(LogisticsChannelAddressDTO.UpdateDTO dto);


}
