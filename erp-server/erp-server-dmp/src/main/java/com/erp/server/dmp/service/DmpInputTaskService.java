package com.erp.server.dmp.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.dto.DmpInputTaskDTO;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 拉取任务 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpInputTaskService extends SuperService<DmpInputTaskEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpInputTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpInputTaskDTO.UpdateDTO dto);

    boolean updateErrorStatus(String id , boolean errorFlag , Integer errorCount , Exception e);
    
    boolean updateNextExecTime(String id , LocalDateTime nextExecTime);

    /**
     * 根据系统代号和业务代号查询最新任务记录
     * @param systemCodeList 系统代号列表
     * @param billTypeList 业务代号列表
     * @param nextLevelIdList 下一级ID列表
     * @return 最新任务信息
     */
    List<DmpInoutDTO.LastOneDTO> lastBySystemCodeAndBillType(List<String> systemCodeList, List<String> billTypeList, List<String> nextLevelIdList);

    DmpInputTaskEntity getByInputIdAndExtendJson(String inputId, String key, String value);
    
    void createNewTask(DmpInputTaskEntity dmpInputTaskEntity);

    /**
     * 分页列表查询
     * @author Jim
     * @date: 2025-10-23
     * @param pagingParamDTO
     * @return PagingVO<DmpInputTaskDTO.ListDTO>>
     */
    PagingVO<DmpInputTaskDTO.ListDTO> paging(PagingDTO<DmpInputTaskDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 状态统计
     * @author Jim
     * @date: 2025-10-23
     * @param dto
     * @return List<DmpInputTaskDTO.TabListDTO>>
     */
    List<DmpInputTaskDTO.TabListDTO> tabList(PermissionsDTO dto);

    @Transactional(rollbackFor = Exception.class)
    BatchResultDTO delete(String id);

    /**
     * 详情
     * @author Jim
     * @date: 2025-10-23
     * @param id
     * @return
     */
    DmpInputTaskDTO.ViewDTO view(String id);

    /**
     * 导出Excel
     * @author Jim
     * @date: 2025-10-23
     * @param dto
     * @param response
     * @return
     */
    void exportList(DmpInputTaskDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 重试
     * @author Jim
     * @date: 2025-10-23
     * @param entity
     * @return
     */
    BatchResultDTO retry(DmpInputTaskEntity entity);

    /**
     * 沿父任务链回溯，返回链路最顶端的根任务。
     * <p>用于子任务回溯获取根任务上下文（如店铺ID、扩展字段等），统一的入口便于后续优化为
     * 递归 CTE 或批次缓存，避免在多个 Handler 中重复维护循环 getById 的回溯逻辑。</p>
     *
     * @param startTask 起始任务（不为 null 时直接复用，避免额外查库）
     * @return 链路根任务；若 startTask 为 null 返回 null；若 parentTaskId 中途断链，返回最后一个有效任务
     */
    DmpInputTaskEntity findRootTaskInChain(DmpInputTaskEntity startTask);

    /**
     * 批量解析任务链根节点的 shopId（根任务 nextLevelId）。
     *
     * @param inputTaskIds 起始任务 ID 集合
     * @return key=inputTaskId, value=根任务 shopId；未解析到则 value 为空字符串
     */
    Map<String, String> batchResolveRootTaskShopId(Collection<String> inputTaskIds);
}
