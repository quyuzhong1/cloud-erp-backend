package com.erp.server.tms.service;
import com.erp.model.tms.entity.CfgLogisticsAuthFieldEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.CfgLogisticsAuthFieldDTO;

/**
 * <p>
 * 物流商授权字段配置表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
public interface CfgLogisticsAuthFieldService extends SuperService<CfgLogisticsAuthFieldEntity> {

    /**
    * 新增
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgLogisticsAuthFieldDTO.AddDTO dto);

    /**
    * 修改
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    Boolean update(CfgLogisticsAuthFieldDTO.UpdateDTO dto);


}
