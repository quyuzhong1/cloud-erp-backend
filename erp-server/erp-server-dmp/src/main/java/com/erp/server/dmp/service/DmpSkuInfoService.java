package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSkuInfoDTO;

/**
 * <p>
 * 中台产品表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-07-22
 */
public interface DmpSkuInfoService extends SuperService<DmpSkuInfoEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-07-22
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSkuInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-07-22
    * @param dto
    * @return
    */
    Boolean update(DmpSkuInfoDTO.UpdateDTO dto);


}
