package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolSampleCostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolSampleCostDTO;

/**
 * <p>
 * 寄样费用表 服务类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
public interface KolSampleCostService extends SuperService<KolSampleCostEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolSampleCostDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    Boolean update(KolSampleCostDTO.UpdateDTO dto);


}
