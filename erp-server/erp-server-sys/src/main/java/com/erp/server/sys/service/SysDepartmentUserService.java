package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysDepartmentUserEntity;

import java.util.List;

/**
 * @Classname SysDepartmentUserService

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
     * @return List<SysDepartmentUserEntity>
     */
    List<SysDepartmentUserEntity> listByDepartmentIds(List<String> departmentIdList);
    /**
     * @description: 查询部门上级
     * @author Will
     * @date: 2023/1/14 11:10
     * @param id
     * @return List<SysDepartmentUserEntity>
     */
    List<SysDepartmentUserEntity> listSuperiorById(String id);
    /**
     * 根据人员id查询部门id
     */
    SysDepartmentUserNumberDTO getDeptByUserId(String userId);

    /**
     * 根据用户id查询部门信息（过滤已禁用部门，取最近绑定的部门）
     * 自动带出用户绑定部门，如用户绑定多个部门，则取最近绑定的部门；若部门已禁用，则不带出
     * @param userId 用户id
     * @return 部门信息
     */
    SysDepartmentUserNumberDTO getDeptByUserIdWithDisabledFilter(String userId);

    /**
     * 根据人员id查询所有上级
     *
     * @param userId 人员id
     * @return
     */
    List<UserSuperiorDTO> listSuperiorByUserId(String userId);

    /**
     * 根据人员id查询所有部门
     * @param userId
     * @return
     */
    List<UserSuperiorDTO> listDeptByUserId(String userId);

    
    /**
     * 根据部门id 获取部门员工
     * @author yl
     * @date 2023-06-05 12:07
     * @param deptId
     * @return java.util.List<com.common.business.dto.FindUserDTO>
     */
    List<FindUserDTO> listDeptUserByDeptId(String deptId);

    /**
     * 根据用户ids 获取部门 用户信息
     * @author yl
     * @date 2023-06-15 16:56
     * @param userIdList
     * @return java.util.List<com.erp.model.sys.dto.SysDepartmentUserNumberDTO>
     */
    List<SysDepartmentUserNumberDTO> listDeptUserByUserIdList(List<String> userIdList);
    /**
     * @description: 
     * @author Will
     * @date: 2024/1/31 17:34
     * @param deptIdList 
     * @return List<SysDepartmentUserNumberDTO> 
     */
    List<SysDepartmentUserNumberDTO> listDeptUserByDeptIdList(List<String> deptIdList);

    void batchSaveOrUpdate(String uid, List<String> departmentIdList, boolean ifAdd);
    /**
     * 删除用户部门关联关系
     * @author will
     * @date 2026/3/9 16:32
     * @param uids
     * @return  void
     */
    void deleteByUserIds(List<String> uids);

    List<SysDepartmentUserNumberDTO> listDeptByUserIdWithDisabledFilter(String userId);
}
