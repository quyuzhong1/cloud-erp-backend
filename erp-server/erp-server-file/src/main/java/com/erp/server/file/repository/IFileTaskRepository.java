package com.erp.server.file.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseDTO;
import com.erp.model.file.dto.FileDTO;
import com.erp.server.file.dto.FileTaskParamsDTO;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.server.file.vo.FileTaskVO;

import java.time.LocalDateTime;
import java.util.List;

public interface IFileTaskRepository extends IService<FileTask> {
    List<FileTask> getExpireByStatuses(LocalDateTime localDateTime, FileTaskStatusEnum... fileTaskStatusEnums);

    IPage<FileTaskVO> getFileTasks(Page<FileTaskVO> page, FileTaskParamsDTO dto);

    void updateTask(BaseDTO.ImportResultDTO importResultDTO);
    /**
     * 查询最新的文件任务信息
     * @author will
     * @date 2026/1/26 11:31
     * @param fileUrlList
     * @return List<FileTaskDTO>
     */
    List<FileDTO.FileTaskDTO> listLatestFileTask(List<String> fileUrlList);
    /**
     * 根据类型查询超时的导入任务
     * @author will
     * @date 2026/1/26 11:31
     * @param code
     * @return List<FileTask>
     */
    List<FileTask> listTimeOutImportTask(String code,Integer hours);
    /**
     * 更新任务状态
     * @author will
     * @date 2026/1/26 11:31
     * @param taskIdList
     * @return void
     */
    void updateTaskStatus(List<String> taskIdList);

    /**
     * 查询待清理的文件任务
     *
     * @param expireTime 过期时间
     * @param limit 查询条数
     * @param excludeIds 本轮已失败需跳过的任务ID
     * @return List<FileTask>
     */
    List<FileTask> listCleanFileTask(LocalDateTime expireTime, int limit, List<String> excludeIds);
}