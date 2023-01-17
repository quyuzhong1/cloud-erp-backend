package com.erp.server.sys.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentTreeDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门表
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@Mapper
public interface SysDepartmentMapper extends BaseMapper<SysDepartmentEntity> {

    List<SysDepartmentTreeDTO> findTree();

    List<SysDepartmentDTO> getDeptList();
    /**
     * @description: 根据部门名称查询上级领导
     * @author Will
     * @date: 2023/1/17 10:09
     * @param deptNames
     * @return List<SysUserDeptDTO>
     */
    List<SysUserDeptDTO> getByDeptNames(@Param("deptNames") List<String> deptNames);
}
