package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpThirdWarehouseInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpThirdWarehouseInfoDTO;

/**
 * <p>
 * 第三方仓库 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-06
 */
public interface DmpThirdWarehouseInfoService extends SuperService<DmpThirdWarehouseInfoEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-06
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpThirdWarehouseInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-06
    * @param dto
    * @return
    */
    Boolean update(DmpThirdWarehouseInfoDTO.UpdateDTO dto);


}
