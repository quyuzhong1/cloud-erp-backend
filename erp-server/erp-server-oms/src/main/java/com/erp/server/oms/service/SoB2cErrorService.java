package com.erp.server.oms.service;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoB2cErrorDTO;

/**
 * <p>
 * B2C销售订单异常表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-12-20
 */
public interface SoB2cErrorService extends SuperService<SoB2cErrorEntity> {

    /**
    * 新增
    * @author lambda
    * @date: 2023-12-20
    * @param dto
    * @return
    */
    Boolean add(SoB2cErrorDTO.AddDTO dto);

    /**
    * 修改
    * @author lambda
    * @date: 2023-12-20
    * @param dto
    * @return
    */
    Boolean update(SoB2cErrorDTO.UpdateDTO dto);


}
