package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.entity.PickingDetailEntity;

import java.util.List;

/**
 * <p>
 * 拣货明细 服务类
 * </p>
 *
 * @author will
 * @since 2023-05-11
 */
public interface PickingDetailService extends SuperService<PickingDetailEntity> {
     /**
      * 新增拣货明细
      * @author Will
      * @date: 2023/5/12 11:23
      * @param detailList
      */
     void add(List<PickingDetailDTO.CommonDTO> detailList);
}
