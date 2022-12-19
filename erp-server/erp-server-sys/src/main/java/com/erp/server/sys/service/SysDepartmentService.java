package com.erp.server.sys.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.modules.sys.vo.SysDeptDropDownVO;
import com.erp.model.sys.dto.DepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;

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
     * 根据id 集合 删除部门信息
     * @param ids
     */
    void removeByIdList(List<String> ids);


    /**
     * 获取部门树结构
     * @return
     */
    List<DepartmentDTO> findDepartmentTree();

    /**树形入参
     * 批量保存部门树结构
     * @param sysDepartmentTree
     */
    void saveBatchDepartment(List<SysDepartmentDTO> sysDepartmentTree);

    List<String> getDepartmentIds(String flagId);
    /**
     * 根据部门id查询
     */
    SysDepartmentDTO getDepartmentById(String deptId);

    /**
     * 部门列表
     * @return
     */
    List<SysDepartmentEntity> listDept();

}

