package com.erp.server.file.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.server.file.dto.FileTaskParamsDTO;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.enums.FileTaskStatusEnum;
import com.erp.server.file.vo.FileTaskVO;

import java.time.LocalDateTime;
import java.util.List;

public interface IFileTaskRepository extends IService<FileTask> {
    List<FileTask> getExpireByStatuses(LocalDateTime localDateTime, FileTaskStatusEnum... fileTaskStatusEnums);

    IPage<FileTaskVO> getFileTasks(Page<FileTaskVO> page, FileTaskParamsDTO dto);
}