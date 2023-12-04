package com.erp.server.wms.service;

import com.erp.model.wms.dto.OverseasWarehouseInboundAddressDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundAddressEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 海外入库单常用揽收地址 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-12-04
 */
public interface OverseasWarehouseInboundAddressService extends SuperService<OverseasWarehouseInboundAddressEntity> {

    /**
     * 常用地址列表
     */
    List<OverseasWarehouseInboundAddressDTO.ListDTO> addressList();

    /**
     * 常用地址新增
     */
    void add(OverseasWarehouseInboundAddressDTO.AddDTO dto);

    /**
     * 删除-常用地址
     */
    Boolean deleteById(String id);
}
