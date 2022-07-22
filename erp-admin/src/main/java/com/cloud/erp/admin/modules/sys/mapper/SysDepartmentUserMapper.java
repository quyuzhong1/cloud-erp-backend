package com.cloud.erp.admin.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.erp.admin.modules.sys.dto.DepartmentSearchDTO;
import com.cloud.erp.admin.modules.sys.entity.SysDepartmentUserEntity;
import com.cloud.erp.admin.modules.sys.vo.SysDepartmentUserNumber;
import com.cloud.erp.admin.modules.sys.vo.SysDepartmentUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Classname SysDepartmentUserMapper
 * @Description TODO
 * @Date 2022-07-13 18:54
 * @Created by yl
 */
@Mapper
public interface SysDepartmentUserMapper  extends BaseMapper<SysDepartmentUserEntity> {

    IPage<SysDepartmentUserVO>  findDepartmentUser(Page query ,@Param("params") DepartmentSearchDTO params, @Param("departmentIds") List<String> departmentIds);

    List<SysDepartmentUserNumber> findUserNumber();
}
