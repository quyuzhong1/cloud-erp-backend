package com.erp.server.wms.service;
import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.erp.model.wms.entity.RequisitionApplicationChangeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.RequisitionApplicationChangeDetailDTO;

import java.util.List;

/**
 * <p>
 * 要货申请变更明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
 */
public interface RequisitionApplicationChangeDetailService extends SuperService<RequisitionApplicationChangeDetailEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-11-18
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RequisitionApplicationChangeDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-11-18
    * @param dto
    * @return
    */
    Boolean update(RequisitionApplicationChangeDetailDTO.UpdateDTO dto);


    List<RequisitionApplicationChangeDTO.ExistDTO> checkExist(List<String> detailIds);

    List<RequisitionApplicationChangeDetailEntity> listByMains(List<String> mainIds);
}
