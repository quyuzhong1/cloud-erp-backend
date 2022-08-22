package com.erp.server.sys.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.sys.dto.SysDepartmentTreeDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import org.apache.ibatis.annotations.Mapper;

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
}
