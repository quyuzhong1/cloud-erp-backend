package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleLedgerFlowEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;

/**
 * <p>
 * 样品库存 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleLedgerFlowService extends SuperService<SampleLedgerFlowEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleLedgerFlowDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean update(SampleLedgerFlowDTO.UpdateDTO dto);


}
