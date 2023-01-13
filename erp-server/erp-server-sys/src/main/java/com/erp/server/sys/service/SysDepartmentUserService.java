package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysDepartmentUserEntity;

import java.util.List;

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

    List<SysDepartmentUserNumberDTO> findUserNumber();

    boolean saveBatchDepartmentUser(BatchSysDepartUserDTO list);

    SysDepartmentUserNumberDTO getByUserId(String id);
    /**
     * @description: 根据部门ids查询
     * @author Will
     * @date: 2023/1/9 10:03
     * @param departmentIdList
     * @return List<SysDepartmentUserNumberDTO>
     */
    List<SysDepartmentUserNumberDTO> listByDepartmentIds(List<String> departmentIdList);
}
