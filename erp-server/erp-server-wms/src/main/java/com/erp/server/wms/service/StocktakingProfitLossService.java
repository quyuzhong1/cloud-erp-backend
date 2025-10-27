package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;

import java.time.LocalDate;
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
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-11 12:10
     */
    Boolean exportExcel(StocktakingProfitLossDTO.ExportDTO dto);

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
     * @param dto
     * @return com.common.business.dto.base.BatchResultDTO
     */
   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
     * 更改金蝶同步状态
     * @author yl
     * @date 2023-08-14 17:47
     * @param id
     * @param syncKingdeeId
     * @return void
     */
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

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
     * 修改盘盈盘亏单
     * @param dto
     * @return
     */
    String update(StocktakingProfitLossDTO.UpdateDTO dto);

    /**
     * 新增 并提交
     * @author yl
     * @date 2023-08-23 10:25
     * @param dto
     * @return java.lang.Boolean
     */
    void addAndSubmit(StocktakingProfitLossDTO.AddDTO dto);

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

    /**
     * 修改并提交
     * @author yl
     * @date 2023-10-20 14:11
     * @param dto
     * @return void
     */
    void updateAndSubmit(StocktakingProfitLossDTO.UpdateDTO dto);

    /**
     * 删除盘盈盘亏单
     * @author yl
     * @date 2023-10-20 14:19
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     */
    BatchResultDTO delete(String id);

    /**
     * 库存组织ID和SKuId最新单据时间
     *
     * @author Jim
     * @date 2024-03-05
     */
    List<StocktakingProfitLossDetailDTO.LastDTO> maxDateByParams(List<String> warehouseIds, List<String> orgIds, List<String> skuIds);


    /**
     * 仓库ID和SkuId,单据时间查询最新的单号
     *
     * @author Jim
     * @date 2024-03-05
     */
    String findLastOneCode(String warehouseId, String skuId, LocalDate billDate);

    /**
     * 根据组织IDS和SkuIds，比较单据日期是否是已审核的盘盈盘亏单据日期之前
     *
     * @author Jim
     * @date 2024-03-07
     */
    boolean checkClosed(List<String> warehouseIds,List<String> warehourseLocationList, List<String> orgIds, List<String> skuIds, LocalDate billDate);

    PagingVO<StocktakingProfitLossDTO.ExportViewDTO> exportStocktakingProfitLoss(PagingDTO<StocktakingProfitLossDTO.ExportDTO> dto);
}
