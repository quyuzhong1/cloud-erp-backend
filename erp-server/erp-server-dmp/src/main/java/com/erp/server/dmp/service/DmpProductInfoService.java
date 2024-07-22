package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpProductInfoDTO;

/**
 * <p>
 * 产品spu信息 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-07-22
 */
public interface DmpProductInfoService extends SuperService<DmpProductInfoEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-07-22
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpProductInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-07-22
    * @param dto
    * @return
    */
    Boolean update(DmpProductInfoDTO.UpdateDTO dto);


}
