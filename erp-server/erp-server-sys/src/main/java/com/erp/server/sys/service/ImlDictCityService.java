package com.erp.server.sys.service;
import com.erp.model.sys.entity.ImlDictCityEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.ImlDictCityDTO;

/**
 * <p>
 * 城市字典表 服务类
 * </p>
 *
 * @author lrp
 * @since 2023-11-22
 */
public interface ImlDictCityService extends SuperService<ImlDictCityEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2023-11-22
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ImlDictCityDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2023-11-22
    * @param dto
    * @return
    */
    Boolean update(ImlDictCityDTO.UpdateDTO dto);


}
