package com.cloud.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.erp.dto.FileTaskParamsDTO;
import com.cloud.erp.entity.FileTask;
import com.cloud.erp.vo.FileTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileTaskMapper extends BaseMapper<FileTask> {
    IPage<FileTaskVO> getFileTasks(@Param("page") Page<FileTaskVO> page, @Param("dto") FileTaskParamsDTO dto);
}