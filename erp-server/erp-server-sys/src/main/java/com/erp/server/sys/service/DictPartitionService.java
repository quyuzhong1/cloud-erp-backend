package com.erp.server.sys.service;
import com.erp.model.sys.entity.DictPartitionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.DictPartitionDTO;

/**
 * <p>
 * 分区表 服务类
 * </p>
 *
 * @author lrp
 * @since 2025-01-03
 */
public interface DictPartitionService extends SuperService<DictPartitionEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2025-01-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DictPartitionDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2025-01-03
    * @param dto
    * @return
    */
    Boolean update(DictPartitionDTO.UpdateDTO dto);


}
