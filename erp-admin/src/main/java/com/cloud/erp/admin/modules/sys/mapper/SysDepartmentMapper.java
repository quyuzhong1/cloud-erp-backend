package com.cloud.erp.admin.modules.sys.mapper;

import com.cloud.erp.admin.modules.sys.entity.SysDepartmentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.erp.admin.modules.sys.vo.SysDepartmentTreeVO;
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

    List<SysDepartmentTreeVO> findTree();
}
