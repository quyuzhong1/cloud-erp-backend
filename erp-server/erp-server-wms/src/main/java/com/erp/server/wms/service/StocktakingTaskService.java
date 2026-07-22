package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.erp.model.wms.entity.StocktakingPlanEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 盘点任务表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
public interface StocktakingTaskService extends SuperService<StocktakingTaskEntity> {

    /**
     * tab list
     * @param dto
     * @return
     */
    List<StocktakingTaskDTO.TabDTO> tabList(PermissionsDTO dto);

    /**
     * 分页
     * @author yl
     * @date 2023-08-03 17:26
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.StocktakingTaskDTO.PagingViewDTO>
     */
    PagingVO<StocktakingTaskDTO.PagingViewDTO> paging(PagingDTO<StocktakingTaskDTO.PagingParamDTO> dto);

    /**
     * 提交审核
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
     * 详情
     * @author yl
     * @date 2023-08-03 17:35
     * @param id
     * @return com.erp.model.wms.dto.StocktakingTaskDTO.ViewDTO
     */
    StocktakingTaskDTO.ViewDTO view(String id);

    /**
     * 审核
     * @param id
     * @param  approveOneDTO
     * @return
     */
    BatchResultDTO approve(String id, ApproveOneDTO approveOneDTO);

    /**
     * 撤销流程
     * @param dto
     * @return
     */
   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
     * 分配用户
     * @author yl
     * @date 2023-08-03 17:36
     * @param
     * @return java.lang.Boolean
     */
    BatchResultDTO assignUser(String id,  List<String> userIdList);

    /**
     * 导出excel
     * @author yl
     * @date 2023-08-03 17:37
     * @param dto
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportExcel(StocktakingTaskDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 导入盘点任务
     * @param excelFile
     * @param response
     * @return
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    
    /**
     * 下载模板
     * @author yl
     * @date 2023-08-03 17:38
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);


    /**
     * 根据来源ID 获取到任务列表
     * @param sourceId
     * @return
     */
    List<StocktakingTaskEntity> listBySourceId(String sourceId);

    /**
     * 批量按盘点计划 sourceId 查询任务
     */
    List<StocktakingTaskEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 根据来源ID 删除任务
     * @param id
     * @return
     */
    Boolean removeBySourceId(String id);

    /**
     * 创建盘点任务
     * @param entity
     * @param detailEntityList
     * @return
     */
    Boolean createTaskList(StocktakingPlanEntity entity,List<StocktakingPlanDetailEntity> detailEntityList,Boolean isNowExecute);

    /**
     * 创建盘点任务
     * @param entity
     * @param detailEntityList
     * @return
     */
    Boolean createTaskListByJob(StocktakingPlanEntity entity,List<StocktakingPlanDetailEntity> detailEntityList);

    /**
     * 流程监听结束
     * @author yl
     * @date 2023-08-18 9:02
     * @param approveOne
     * @param entity
     * @return java.lang.Boolean
     */
    Boolean approveEnd(ApproveOneDTO approveOne, StocktakingTaskEntity entity);

    /**
     * 盘点任务审核通过后释放 Redis 盘点库存锁（工作流/Feign 统一入口；有事务时 afterCommit 执行并重试）。
     * <p>
     * 按 task-keys 索引精确删除本任务占用的 lock；无索引时按任务明细推导 lockKey 兜底。
     * 若同计划其它未完成任务仍引用同一 lockKey 则保留。
     * 计划下全部任务均为「已完成」时，再触发 plan 级兜底释锁。
     */
    void releaseInventoryLockByTaskId(String taskId);

    /**
     * 按计划单号释放该计划全部 Redis 库存锁（反审核、下推失败回滚等调用）。
     * 优先读取 plan-keys 索引精确删除；无索引时回退 SCAN。
     */
    void releaseInventoryLockByPlanCode(String planCode);

    /**
     * 判断指定库存维度是否已被其它盘点计划占用（同 planCode 的锁不计入冲突）。
     *
     * @param planCode             当前计划单号；为空时任意已存在锁均视为占用
     * @param orgId                组织 ID
     * @param warehouseId          仓库 ID
     * @param warehouseLocation    库位
     * @param skuId                SKU ID
     * @param dictInventoryStatus  库存状态
     * @return 存在其它计划占用锁时返回 true
     */
    boolean isInventoryLockedForStocktaking(String planCode, String orgId, String warehouseId, String warehouseLocation, String skuId, String dictInventoryStatus);

    /**
     * 在库存维度分布式锁内执行盘点 Redis 库存锁预检与写入（须通过 Spring 代理调用以触发 {@code @DistributeLocker}）。
     *
     * @param planCode      计划单号
     * @param inventoryList 待加锁库存行
     */
    void acquireStocktakingInventoryLocks(String planCode, List<InventoryEntity> inventoryList);

    /**
     * 在库存维度分布式锁内执行预检与写入（调用方须已完成库存行校验，不再重复校验）。
     * <p>
     * 大计划分批加锁时由 {@link #acquireStocktakingInventoryLocksByPlan(String, List)} 逐批调用。
     *
     * @param planCode      计划单号
     * @param inventoryList 待加锁库存行（已通过校验）
     */
    void acquireStocktakingInventoryLocksAfterValidated(String planCode, List<InventoryEntity> inventoryList);

    /**
     * 大计划分批加锁：planCode 分布式锁覆盖全部分批过程，各批内再调用 {@link #acquireStocktakingInventoryLocksAfterValidated(String, List)} 获取维度 MultiLock。
     * <p>
     * 维度数超过实现类阈值时由 {@code lockInventoryForStocktaking} 经 Spring 代理调用。
     *
     * @param planCode      计划单号
     * @param inventoryList 待加锁库存行（库位已规范化）
     */
    void acquireStocktakingInventoryLocksByPlan(String planCode, List<InventoryEntity> inventoryList);

    /**
     * 根据code 获取任务信息
     * @param taskCode
     * @return
     */
    StocktakingTaskEntity getByCode(String taskCode);

    /**
     * 获取到盘点任务 盘点数量为0 的
     * @author yl
     * @date 2023-08-22 10:36
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.StocktakingTaskDTO.CheckResultDTO>
     */
    List<StocktakingTaskDTO.CheckResultDTO> checkQty(List<String> ids);

    Boolean pushStocktakingProfitLoss(BaseIdsDTO.IdsDTO dto);

}
