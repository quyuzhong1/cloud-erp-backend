package com.erp.server.sys.service;
import com.erp.model.sys.entity.DictNoticeRoleOptionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.DictNoticeRoleOptionDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jack
 * @since 2025-06-04
 */
public interface DictNoticeRoleOptionService extends SuperService<DictNoticeRoleOptionEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-06-04
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DictNoticeRoleOptionDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-06-04
    * @param dto
    * @return
    */
    Boolean update(DictNoticeRoleOptionDTO.UpdateDTO dto);


}
