package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.server.wms.mapper.WarehouseReceiveMapper;
import com.erp.server.wms.service.WarehouseReceiveService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-06
 */
@Service
public class WarehouseReceiveServiceImpl extends SuperServiceImpl<WarehouseReceiveMapper, WarehouseReceiveEntity> implements WarehouseReceiveService {



    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    public Boolean add(WarehouseReceiveDTO.AddDTO dto) {
        dto.getPurchaseOrderId();


        return true;
    }
}
