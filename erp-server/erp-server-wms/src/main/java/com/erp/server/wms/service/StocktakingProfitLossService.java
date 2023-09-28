package com.erp.server.wms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 盘盈盘亏单 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
public interface StocktakingProfitLossService extends SuperService<StocktakingProfitLossEntity> {

    

    /**
     * tab list
     * @param dto
     * @return
     */
    List<StocktakingProfitLossDTO.TabDTO> tabList(PermissionsDTO dto);

    /**
     * 分页列表
     * @author yl
     * @date 2023-08-11 9:05
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.StocktakingProfitLossDTO.PagingViewDTO>
     */
    PagingVO<StocktakingProfitLossDTO.PagingViewDTO> paging(PagingDTO<StocktakingProfitLossDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @param id
     * @return
     */
    StocktakingProfitLossDTO.ViewDTO view(String id);

    /**
     * 导出
     * @author yl
     * @date 2023-08-11 12:10
     * @param dto
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportExcel(StocktakingProfitLossDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 盘盈盘亏单提交
     * @author yl
     * @date 2023-08-14 11:45
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     */
    BatchResultDTO submit(String id);

    /**
     * 盘盈盘亏单审核
     * @author yl
     * @date 2023-08-14 14:02
     * @param id
     * @param approveOneDTO
     * @return com.common.business.dto.base.BatchResultDTO
     */
    BatchResultDTO approve(String id, ApproveOneDTO approveOneDTO);

    /**
     * 取消流程
     * @author yl
     * @date 2023-08-14 14:46
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     */
    BatchResultDTO cancelProcess(String id);

    /**
     * 更改金蝶同步状态
     * @author yl
     * @date 2023-08-14 17:47
     * @return void
     */
    Boolean updateSyncKingdeeStatus(PushSyncStatusDTO.KingdeeDTO kingdeeDTO);

    /**
     * 流程监听后
     * @param approveOne
     * @param entity
     * @return
     */
    Boolean approveEnd(ApproveOneDTO approveOne, StocktakingProfitLossEntity entity);

    /**
     * 添加
     * @param dto
     * @return
     */
    String add(StocktakingProfitLossDTO.AddDTO dto);

    /**
     * 新增 并提交
     * @author yl
     * @date 2023-08-23 10:25
     * @param dto
     * @return java.lang.Boolean
     */
    String addAndSubmit(StocktakingProfitLossDTO.AddDTO dto);

    /**
     * 批量保存提交
     * @author yl
     * @date 2023-08-23 11:37
     * @param list
     * @return java.util.List<java.lang.String>
     */
    List<StocktakingProfitLossEntity> batchSave(List<StocktakingProfitLossDTO.AddDTO> list);

    /**
     * 根据SourceId 获取盘盈盘亏列表
     * @author yl
     * @date 2023-08-23 20:27
     * @param sourceId
     * @return java.util.List<com.erp.model.wms.entity.StocktakingProfitLossEntity>
     */
    List<StocktakingProfitLossEntity> listBySourceId(String sourceId);
}
