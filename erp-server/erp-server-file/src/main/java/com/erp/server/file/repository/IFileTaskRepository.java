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
import java.util.Set;

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
     * 查询待清理的文件任务（按 create_time、id 稳定排序，配合 offset 跳过本轮已失败、未删除的任务，
     * 避免使用持续膨胀的 NOT IN 列表）。
     *
     * @param expireTime 过期时间
     * @param limit 查询条数
     * @param offset 跳过本轮已失败、仍匹配查询条件的任务数
     * @return List<FileTask>
     */
    List<FileTask> listCleanFileTask(LocalDateTime expireTime, int limit, int offset);

    /**
     * 查询当前处于处理中（PROCESS）状态的任务ID集合，用于临时目录清理时 O(1) 判断是否处理中，
     * 避免逐文件查库（N+1）。
     *
     * @return 处理中任务ID集合
     */
    Set<String> listProcessingTaskIds();
}