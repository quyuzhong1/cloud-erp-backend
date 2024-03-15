package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsCfgCostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsCfgCostDTO;

/**
 * <p>
 * 费用管理配置表 服务类
 * </p>
 *
 * @author will
 * @since 2024-03-15
 */
public interface TmsCfgCostService extends SuperService<TmsCfgCostEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-15
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsCfgCostDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-03-15
    * @param dto
    * @return
    */
    Boolean update(TmsCfgCostDTO.UpdateDTO dto);


}
