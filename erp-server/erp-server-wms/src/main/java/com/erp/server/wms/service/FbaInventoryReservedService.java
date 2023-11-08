package com.erp.server.wms.service;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.model.wms.entity.FbaInventoryReservedEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaInventoryReservedDTO;

import java.util.List;

/**
 * <p>
 * FBA库存预留信息 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
 */
public interface FbaInventoryReservedService extends SuperService<FbaInventoryReservedEntity> {

    /**
     * 根据主键id查询预留明细
     * @Author Luo_WG
     * @Date 2023/11/8 18:46
     * @param main
     * @return java.util.List<com.erp.model.wms.entity.FbaInventoryReservedEntity>
     **/
    List<FbaInventoryReservedEntity> listByMainId(String main);
}
