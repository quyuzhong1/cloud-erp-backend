package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.dto.BatchSysDepartUserDTO;
import com.cloud.erp.admin.modules.sys.dto.DepartmentSearchDTO;
import com.cloud.erp.admin.modules.sys.dto.UpdateUserStateDTO;
import com.cloud.erp.admin.modules.sys.entity.SysDepartmentUserEntity;
import com.cloud.erp.admin.modules.sys.vo.SysDepartmentUserNumber;
import com.erp.common.dto.PagingDTO;
import com.erp.common.vo.PagingVO;

import java.util.List;
import java.util.Set;

/**
 * @Classname SysDepartmentUserService
 * @Description TODO
 * @Date 2022-07-13 18:50
 * @Created by yl
 */
public interface SysDepartmentUserService  extends IService<SysDepartmentUserEntity> {

    /**
     * 获取部门员工列表
     * @author yl
     * @date 2022-07-13 18:59
     * @param dto
     * @return java.util.List<com.cloud.erp.admin.modules.sys.vo.SysUserVO>
     */

    PagingVO findDepartmentUser(PagingDTO<DepartmentSearchDTO> dto);


    void removeByDepartmentIds(List<String> ids);

    void setLead(UpdateUserStateDTO dto);

    List<SysDepartmentUserNumber>  findUserNumber();

    boolean saveBatchDepartmentUser(BatchSysDepartUserDTO list);
}
