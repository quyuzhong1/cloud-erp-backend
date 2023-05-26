package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.bi.dto.ModuleDTO;
import com.erp.model.bi.dto.ModulePagingDTO;
import com.erp.model.bi.entity.BiModuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模块表(BiModule)表数据库访问层
 *
 * @author yl
 * @since 2022-12-08 14:31:14
 */
@Mapper
public interface BiModuleMapper extends BaseMapper<BiModuleEntity> {


    IPage<ModulePagingDTO> paging(Page query, @Param("params") BaseSearchDTO params);

    List<String> getUserVisibleModuleIds(@Param("userId") String userId);

    List<ModuleDTO> getByIds(@Param("moduleIdList") List<String> moduleIdList,@Param("searchKeyword") String searchKeyword);
}

