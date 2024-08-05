package com.erp.server.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.server.file.dto.FileTaskParamsDTO;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.vo.FileTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileTaskMapper extends BaseMapper<FileTask> {
    IPage<FileTaskVO> getFileTasks(@Param("page") Page<FileTaskVO> page, @Param("params") FileTaskParamsDTO params);
}