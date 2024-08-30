package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.ReplenishmentRefLabelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.ReplenishmentRefLabelDTO;

/**
 * <p>
 * 补货建议标签关系表 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
public interface ReplenishmentRefLabelService extends SuperService<ReplenishmentRefLabelEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ReplenishmentRefLabelDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-30
    * @param dto
    * @return
    */
    Boolean update(ReplenishmentRefLabelDTO.UpdateDTO dto);


}
