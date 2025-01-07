package com.erp.server.sys.service;
import com.erp.model.sys.entity.CfgCountryPartitionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.CfgCountryPartitionDTO;

/**
 * <p>
 * 分区国家关联表 服务类
 * </p>
 *
 * @author lrp
 * @since 2025-01-03
 */
public interface CfgCountryPartitionService extends SuperService<CfgCountryPartitionEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2025-01-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgCountryPartitionDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2025-01-03
    * @param dto
    * @return
    */
    Boolean update(CfgCountryPartitionDTO.UpdateDTO dto);


}
