package com.erp.server.oms.service;
import com.erp.model.oms.entity.SoB2cFinanceEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoB2cFinanceDTO;

/**
 * <p>
 * B2C销售订单财务信息表 服务类
 * </p>
 *
 * @author will
 * @since 2023-09-08
 */
public interface SoB2cFinanceService extends SuperService<SoB2cFinanceEntity> {

    /**
    * 新增
    * @author will
    * @date: 2023-09-08
    * @param dto
    * @return
    */
    String add(SoB2cFinanceDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2023-09-08
    * @param dto
    * @return
    */
    Boolean update(SoB2cFinanceDTO.UpdateDTO dto);


}
