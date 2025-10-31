package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpFeishuInstanceIdsEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpFeishuInstanceIdsDTO;

/**
 * <p>
 * DMP飞书变更实例IDS记录 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
 */
public interface DmpFeishuInstanceIdsService extends SuperService<DmpFeishuInstanceIdsEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2025-10-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpFeishuInstanceIdsDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2025-10-30
    * @param dto
    * @return
    */
    Boolean update(DmpFeishuInstanceIdsDTO.UpdateDTO dto);


}
