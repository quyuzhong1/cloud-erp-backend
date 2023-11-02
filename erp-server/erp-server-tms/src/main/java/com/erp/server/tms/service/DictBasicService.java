package com.erp.server.tms.service;
import com.erp.model.tms.entity.DictBasicEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.DictBasicDTO;

import java.util.List;

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
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(DictBasicDTO.UpdateDTO dto);

    /**
     * 保存或者修改字典信息
     * @author yl
     * @date 2023-03-17 12:21
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean saveOrUpdateDict(List<DictBasicDTO.AddOrUpdateDTO> dto);
}
