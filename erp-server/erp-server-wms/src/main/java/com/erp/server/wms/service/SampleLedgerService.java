package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleLedgerDTO;

/**
 * <p>
 * 样品库存统计 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleLedgerService extends SuperService<SampleLedgerEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleLedgerDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean update(SampleLedgerDTO.UpdateDTO dto);


}
