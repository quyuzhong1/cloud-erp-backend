package com.erp.server.sys.service;
import com.erp.model.sys.entity.SysDepartmentThirdEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysDepartmentThirdDTO;
import com.erp.model.sys.entity.SysUserThirdEntity;

import java.util.List;

/**
 * <p>
 * 第三方 部门信息 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-15
 */
public interface SysDepartmentThirdService extends SuperService<SysDepartmentThirdEntity> {

    List<SysDepartmentThirdDTO.ThirdDeptDropDownDTO> listThirdDeptDropDown(SysDepartmentThirdDTO.ThirdDeptParamDTO dto);

    void syncFsDept();

    SysDepartmentThirdEntity findByDepartmentId(String platform, String departmentId);
}
