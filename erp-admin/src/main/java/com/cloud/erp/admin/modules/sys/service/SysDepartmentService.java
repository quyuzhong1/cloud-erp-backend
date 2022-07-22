package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.dto.SysDepartmentDTO;
import com.cloud.erp.admin.modules.sys.entity.SysDepartmentEntity;
import com.cloud.erp.admin.modules.sys.vo.SysDepartmentVO;

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
    List<SysDepartmentVO> findDepartmentTree();

    /**树形入参
     * 批量保存部门树结构
     * @param sysDepartmentTree
     */
    void saveBatchDepartment(List<SysDepartmentDTO> sysDepartmentTree);

    List<String> getDepartmentIds(String flagId);
}

