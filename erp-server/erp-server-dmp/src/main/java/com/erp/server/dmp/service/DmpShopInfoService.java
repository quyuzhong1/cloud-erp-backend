package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpShopInfoDTO;

/**
 * <p>
 * 中台店铺表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-07
 */
public interface DmpShopInfoService extends SuperService<DmpShopInfoEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-08-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpShopInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-08-07
    * @param dto
    * @return
    */
    Boolean update(DmpShopInfoDTO.UpdateDTO dto);


}
