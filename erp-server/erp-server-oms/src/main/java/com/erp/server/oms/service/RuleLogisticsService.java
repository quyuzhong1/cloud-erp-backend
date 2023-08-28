package com.erp.server.oms.service;
import com.erp.model.oms.entity.RuleLogisticsEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.RuleLogisticsDTO;

/**
 * <p>
 * 物流规则表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
public interface RuleLogisticsService extends SuperService<RuleLogisticsEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    String add(RuleLogisticsDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    Boolean update(RuleLogisticsDTO.UpdateDTO dto);


}
