package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgOutputConvertValueEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgOutputConvertValueDTO;

/**
 * <p>
 * 推送字段映射值 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-20
 */
public interface DmpCfgOutputConvertValueService extends SuperService<DmpCfgOutputConvertValueEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgOutputConvertValueDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    Boolean update(DmpCfgOutputConvertValueDTO.UpdateDTO dto);


}
