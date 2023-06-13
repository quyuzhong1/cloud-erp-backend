package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.LayoutRefModuleDTO;
import com.erp.model.bi.entity.BiLayoutRefModuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 布局与模块关系表(BiLayoutRefModule)表数据库访问层
 *
 * @author yl
 * @since 2022-12-08 14:29:39
 */
@Mapper
public interface BiLayoutRefModuleMapper extends BaseMapper<BiLayoutRefModuleEntity> {


    List<LayoutRefModuleDTO.LayoutRefModuleInfoDTO> listByLayoutIds(@Param("layoutIdList") List<String> layoutIdList);
}

