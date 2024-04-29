package com.cloud.erp.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.dto.FileTaskParamsDTO;
import com.cloud.erp.entity.FileTask;
import com.cloud.erp.enums.FileTaskStatusEnum;
import com.cloud.erp.vo.FileTaskVO;

import java.time.LocalDateTime;
import java.util.List;

public interface IFileTaskRepository extends IService<FileTask> {
    List<FileTask> getExpireByStatuses(LocalDateTime localDateTime, FileTaskStatusEnum... fileTaskStatusEnums);

    IPage<FileTaskVO> getFileTasks(Page<FileTaskVO> page, FileTaskParamsDTO dto);
}