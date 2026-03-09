package com.erp.server.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.file.dto.FileDTO;
import com.erp.server.file.dto.FileTaskParamsDTO;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.vo.FileTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileTaskMapper extends BaseMapper<FileTask> {
    IPage<FileTaskVO> getFileTasks(@Param("page") Page<FileTaskVO> page, @Param("params") FileTaskParamsDTO params);
    /**
     * 查询最新的文件任务信息
     * @author will
     * @date 2026/1/26 11:37
     * @param fileUrlList
     * @return List<FileTaskDTO>
     */
    List<FileDTO.FileTaskDTO> listLatestFileTask(@Param("fileUrlList") List<String> fileUrlList);
}