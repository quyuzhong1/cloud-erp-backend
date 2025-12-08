package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolB2cApplicationAddressEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolB2cApplicationAddressDTO;

/**
 * <p>
 * B2C寄样申请单地址信息 服务类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
public interface KolB2cApplicationAddressService extends SuperService<KolB2cApplicationAddressEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolB2cApplicationAddressDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    Boolean update(KolB2cApplicationAddressDTO.UpdateDTO dto);


}
