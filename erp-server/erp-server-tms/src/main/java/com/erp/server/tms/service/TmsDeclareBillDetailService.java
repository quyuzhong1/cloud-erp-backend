package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsDeclareBillDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsDeclareBillDetailDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;

import java.util.List;

/**
 * <p>
 * 报关单明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
public interface TmsDeclareBillDetailService extends SuperService<TmsDeclareBillDetailEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-03-27
    * @return
    */
    Boolean add(TmsDeclareBillEntity tmsDeclareBillEntity, List<TmsDeclareBillDetailEntity> detailEntityList);

    /**
    * 修改
    * @author lrp
    * @date: 2024-03-27
    * @param dto
    * @return
    */
    Boolean update(TmsDeclareBillDetailDTO.UpdateDTO dto);


}
