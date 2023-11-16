package com.erp.server.wms.service;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasProviderDTO;

/**
 * <p>
 * 海外物流商 服务类
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
public interface OverseasProviderService extends SuperService<OverseasProviderEntity> {

    /**
    * 新增
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasProviderDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasProviderDTO.UpdateDTO dto);


}
