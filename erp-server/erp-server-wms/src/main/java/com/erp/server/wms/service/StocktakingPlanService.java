package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.erp.model.wms.entity.StocktakingPlanEntity;
import com.erp.model.wms.enums.StocktakingStatusEnum;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 盘点计划表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
 */
public interface StocktakingPlanService extends SuperService<StocktakingPlanEntity> {

      /**
      * 分页列表查询
      * @author Cloud
      * @date: 2023-08-08
      * @param pagingParamDTO
      * @return PagingVO<StocktakingPlanDTO.ListDTO>>
      */
      PagingVO<StocktakingPlanDTO.ListDTO> paging(PagingDTO<StocktakingPlanDTO.PagingParamDTO> pagingParamDTO);

     /**
     * 状态统计
     * @author Cloud
     * @date: 2023-08-08
     * @param dto
     * @return List<StocktakingPlanDTO.TabListDTO>>
     */
     List<StocktakingPlanDTO.TabListDTO> tabList(PermissionsDTO dto);

     /**
     * 详情
     * @author Cloud
     * @date: 2023-08-08
     * @param id
     * @return
     */
     StocktakingPlanDTO.ViewDTO view(String id);

     /**
     * 新增
     * @author Cloud
     * @date: 2023-08-08
     * @param dto
     * @return
     */
     String add(StocktakingPlanDTO.AddDTO dto);

     /**
     * 修改
     * @author Cloud
     * @date: 2023-08-08
     * @param dto
     * @return
     */
     void update(StocktakingPlanDTO.UpdateDTO dto);

     /**
     * 新增并提交审核
     * @author Cloud
     * @date: 2023-08-08
     * @param dto
     * @return
     */
     void addAndSubmit(StocktakingPlanDTO.AddDTO dto);

     /**
     * 修改并提交审核
     * @author Cloud
     * @date: 2023-08-08
     * @param dto
     * @return
     */
     void updateAndSubmit(StocktakingPlanDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author Cloud
     * @date: 2023-08-08
     * @param id
     * @return
     */
     BatchResultDTO submit(String id);

    /**
    * 审核
    * @author Cloud
    * @date: 2023-08-08
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author Cloud
    * @date: 2023-08-08
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author Cloud
    * @date: 2023-08-08
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author Cloud
    * @date: 2023-08-08
    * @param dto
    * @return
    */
   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author Cloud
    * @date: 2023-08-08
    * @param dto
    * @param response
    * @return
    */
    void exportList(StocktakingPlanDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 审核通过
     * @param dto
     * @param entity
     * @return
     */
    Boolean approveEnd(ApproveOneDTO dto, StocktakingPlanEntity entity);

    /**
     * 盘点状态更新
     * @param sourceId
     * @param stocktakingStatusEnum
     */
    void updateForStocktakingStatus(String sourceId, StocktakingStatusEnum stocktakingStatusEnum);

}
