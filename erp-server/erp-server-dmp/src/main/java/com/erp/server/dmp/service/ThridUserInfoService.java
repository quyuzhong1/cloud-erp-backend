package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.ThridUserInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.ThridUserInfoDTO;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-04-03
 */
public interface ThridUserInfoService extends SuperService<ThridUserInfoEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-04-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ThridUserInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-04-03
    * @param dto
    * @return
    */
    Boolean update(ThridUserInfoDTO.UpdateDTO dto);


}
