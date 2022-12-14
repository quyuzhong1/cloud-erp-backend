package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.LayoutDetailsDTO;
import com.erp.model.bi.entity.BiLayoutEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 布局表(BiLayout)表数据库访问层
 *
 * @author yl
 * @since 2022-12-08 14:28:26
 */
@Mapper
public interface BiLayoutMapper extends BaseMapper<BiLayoutEntity> {


    List<LayoutDetailsDTO> getLayoutBySubjectId(@Param("subjectId") String subjectId);
}

