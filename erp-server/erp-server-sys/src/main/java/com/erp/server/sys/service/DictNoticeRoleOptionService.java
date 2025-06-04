package com.erp.server.sys.service;
import com.erp.model.sys.entity.DictNoticeRoleOptionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.DictNoticeRoleOptionDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jack
 * @since 2025-06-04
 */
public interface DictNoticeRoleOptionService extends SuperService<DictNoticeRoleOptionEntity> {

    List<DictNoticeRoleOptionDTO.DropDownDTO> dropDownList(String businessType);
}
