package com.erp.server.wms.service;
import com.erp.model.wms.entity.VirtualAdjustDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualAdjustDetailDTO;
import com.erp.model.wms.entity.VirtualAdjustEntity;

import java.util.List;

/**
 * <p>
 * 虚拟仓调整单明细表 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-06-09
 */
public interface VirtualAdjustDetailService extends SuperService<VirtualAdjustDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-06-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualAdjustDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-06-09
    * @param dto
    * @return
    */
    Boolean update(VirtualAdjustDetailDTO.UpdateDTO dto);

    void updateDetail(String mainId, List<VirtualAdjustDetailEntity> detailEntityList);

    List<VirtualAdjustDetailEntity> listByMainIdList(List<String> mainIdList);

    void removeByMainId(String id);
}
