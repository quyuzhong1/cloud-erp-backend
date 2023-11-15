package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsSaleChannelDTO;

/**
 * <p>
 * 销售平台物流渠道表 服务类
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
 */
public interface LogisticsSaleChannelService extends SuperService<LogisticsSaleChannelEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2023-11-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsSaleChannelDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2023-11-08
    * @param dto
    * @return
    */
    Boolean update(LogisticsSaleChannelDTO.UpdateDTO dto);


    Boolean saveOrUpdateSaleChannel(LogisticsSaleChannelEntity logisticsSaleChannelEntity);
}
