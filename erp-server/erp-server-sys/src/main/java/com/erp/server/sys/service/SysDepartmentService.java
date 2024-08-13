package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.sys.dto.DepartmentDTO;
import com.erp.model.sys.dto.DeptUserDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysDepartmentEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 部门表
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
public interface SysDepartmentService extends IService<SysDepartmentEntity> {

    /**
     * @param sysDepartment
     * @description: 新增或修改
     * @author Will
     * @date: 2023/5/4 10:01
     */
    void saveOrUpdateSysDept(SysDepartmentEntity sysDepartment);

    /**
     * 根据id 集合 删除部门信息
     *
     * @param ids
     */
    void removeByIdList(List<String> ids);


    /**
     * 获取部门树结构
     *
     * @return
     */
    List<DepartmentDTO> findDepartmentTree();

    /**
     * 树形入参
     * 批量保存部门树结构
     *
     * @param sysDepartmentTree
     */
    void saveBatchDepartment(List<SysDepartmentDTO> sysDepartmentTree);

    List<String> getDepartmentIds(String flagId);

    /**
     * 根据部门id查询
     */
    SysDepartmentDTO getDepartmentById(String deptId);

    /**
     * 根据部门code查询
     */
    SysDepartmentDTO getUserDeptByCode(String code);

    /**
     * 部门列表
     *
     * @return
     */
    List<SysDepartmentEntity> listDept();


    List<String> getDeptIds(String deptName);

    List<SysDepartmentDTO> getDeptList();

    List<DeptUserDTO> deptUserTree();

    /**
     * @param deptNames
     * @return List<SysUserDeptDTO>
     * @description: 根据部门名称查询上级负责人
     * @author Will
     * @date: 2023/1/17 10:07
     */
    List<SysUserDeptDTO> getByDeptNames(List<String> deptNames);

    SysDepartmentEntity getParentDepartmentById(String departmentId);


    /**
     * @param codeList
     * @return List<SysDepartmentDTO>
     * @description: 根据编码查询部门
     * @author Will
     * @date: 2023/7/5 18:17
     */
    List<SysDepartmentDTO> listDeptByCodeList(List<String> codeList);

    /**
     * 导入部门金蝶
     *
     * @param file
     * @throws IOException
     */
    void importDeptKingdee(MultipartFile file) throws IOException;

    /**
     * 根据id 集合获取到所有部门信息
     *
     * @param deptIdList
     * @return
     */
    List<SysDepartmentEntity> listByIdList(List<String> deptIdList);

    /**
     * 根据用户ｉｄ集合获取到负责人
     *
     * @param userIdList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-07-14 17:26
     */
    List<String> listLeadByUserIdList(List<String> userIdList);

    /**
     * @param deptNameList
     * @return List<SysDepartmentDTO>
     * @description: 根据部门名称查询最高级别部门及下级
     * @author Will
     * @date: 2023/9/20 18:55
     */
    List<SysDepartmentDTO> listSameLevelDeptIdList(List<String> deptNameList);

    /**
     * 获取部门下全量子集
     *
     * @param deptId
     * @return
     */
    List<SysDepartmentTreeDTO> getDeptByParentId(String deptId);

    List<SysDepartmentEntity> getDeptByNames(List<String> deptNameList);
}

