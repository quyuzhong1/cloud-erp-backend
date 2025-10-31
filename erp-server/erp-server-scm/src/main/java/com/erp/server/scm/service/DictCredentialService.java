package com.erp.server.scm.service;
import com.erp.model.scm.entity.DictCredentialEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.DictCredentialDTO;

import java.util.List;

/**
 * <p>
 * 供应商资质字典表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-10-15
 */
public interface DictCredentialService extends SuperService<DictCredentialEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-10-15
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DictCredentialDTO.AddDTO dto);


    List<DictCredentialDTO.ListDTO> listAll();
}
