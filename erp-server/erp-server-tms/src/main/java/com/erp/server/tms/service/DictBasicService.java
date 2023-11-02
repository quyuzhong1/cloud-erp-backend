package com.erp.server.tms.service;
import com.erp.model.tms.entity.DictBasicEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.DictBasicDTO;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface DictBasicService extends SuperService<DictBasicEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DictBasicDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(DictBasicDTO.UpdateDTO dto);


}
