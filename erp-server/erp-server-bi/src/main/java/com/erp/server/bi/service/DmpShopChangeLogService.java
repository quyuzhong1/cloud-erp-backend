package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.DmpShopChangeLogDTO;
import com.erp.model.dmp.entity.DmpShopChangeLogEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 14:30
 */
public interface DmpShopChangeLogService extends IService<DmpShopChangeLogEntity> {
    /**
     * @description: 根据店铺id查询变更记录
     * @author Will
     * @date: 2022/12/15 16:43
     * @param shopId
     * @return List<DmpShopChangeLogDTO>
     */
    List<DmpShopChangeLogDTO> listByShopId(String shopId);
}
