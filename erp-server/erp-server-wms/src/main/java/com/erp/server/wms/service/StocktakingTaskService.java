package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.erp.model.wms.entity.StocktakingPlanEntity;
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
    Boolean createTaskList(StocktakingPlanEntity entity,List<StocktakingPlanDetailEntity> detailEntityList);

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
}
