package com.erp.server.file.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseDTO;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.file.dto.FileTaskParamsDTO;
import com.erp.model.file.entity.FileTask;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.model.file.vo.FileTaskVO;

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
     * 查询待清理的文件任务，按 {@code (create_time, id)} 稳定升序、游标分页：
     * 取严格大于上一批已处理到的 {@code (lastCreateTime, lastId)} 的下一批。
     * 包含 fileUrl 为 null、空串或仅空白字符的过期记录，由 Job 侧逻辑软删，避免脏数据永久残留。
     * 相比 offset，软删成功记录移除后游标仍单调推进，不回扫已处理记录，失败记录顺延到下次调度重试。
     *
     * @param expireTime     过期时间
     * @param limit          查询条数
     * @param lastCreateTime 上一批最后一条的 create_time，首批传 null
     * @param lastId         上一批最后一条的 id，首批传 null
     * @return List<FileTask>
     */
    List<FileTask> listCleanFileTask(LocalDateTime expireTime, int limit, LocalDateTime lastCreateTime, String lastId);

    /**
     * 查询当前处于处理中（PROCESS）状态的任务 ID 集合，用于临时目录清理时 O(1) 判断是否处理中，
     * 避免逐文件查库（N+1）。PROCESS 规模受 {@link com.erp.server.file.context.FileTaskContext} 执行线程池并发约束（集群约两百量级）。
     *
     * @return 处理中任务ID集合
     */
    Set<String> listProcessingTaskIds();
}