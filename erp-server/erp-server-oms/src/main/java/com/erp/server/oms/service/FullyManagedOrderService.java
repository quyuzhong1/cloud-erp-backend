package com.erp.server.oms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.FullyManagedDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;

import java.util.List;

/**
 * @author zdy
 * @ClassName FullyManagedOrderService
 * @description: 全托管订单服务
 * @date 2025年03月25日
 * @version: 1.0
 */
public interface FullyManagedOrderService extends SuperService<SoB2cEntity> {
    /**
     * 全托管订单tab列表
     * @param dto
     * @return
     */
    List<SoB2cDTO.TabListDTO> fullyManagedTabList(PermissionsDTO dto);
}
