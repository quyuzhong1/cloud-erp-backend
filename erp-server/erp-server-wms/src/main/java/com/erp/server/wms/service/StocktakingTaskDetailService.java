package com.erp.server.wms.service;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.common.business.service.SuperService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 盘点任务明细表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
public interface StocktakingTaskDetailService extends SuperService<StocktakingTaskDetailEntity> {

    /**
     * 导出明细
     *
     * @param dto
     * @return
     */
    Boolean exportExcel(BaseIdDTO dto);
    /**
     * 导入明细
     * @author yl
     * @date 2023-08-03 17:58
     * @param excelFile
     * @param response
     * @param mainId
     * @return java.lang.Boolean
     */
    Boolean importFile(String mainId,MultipartFile excelFile, HttpServletResponse response);

    /**
     * 更新明细
     * @author yl
     * @date 2023-08-03 17:59
     * @param dto
     * @return 
     */
    Boolean updateBatchDetail(List<StocktakingTaskDetailDTO.UpdateDTO> dto);

    /**
     * 根据仓库id 集合 获取到任务明细
     * @param warehouseIdList
     * @return
     */
    List<StocktakingTaskDetailEntity> listByWarehouseIds(List<String> warehouseIdList);

    /**
     * 根据主表id 获取到详情
     * @author yl
     * @date 2023-08-08 12:08
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.entity.StocktakingTaskDetailEntity>
     */
    List<StocktakingTaskDetailEntity> listBaseByMainIds(List<String> mainIdList);

    /**
     * 获取到对应的详情
     * @author yl
     * @date 2023-08-08 16:48
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.StocktakingTaskDetailDTO.ViewDTO>
     */
    List<StocktakingTaskDetailDTO.ViewDTO> listByMainId(String mainId);

    /**
     * 下载模板
     * @author yl
     * @date 2023-08-09 14:04
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 批量删除详情数据
     * @param mainIds
     * @return
     */
    Boolean removeByMainId(List<String> mainIds);

    /**
     * 根据一些信息 获取到明细信息
     * @author yl
     * @date 2023-08-21 17:51
     * @param mainId
     * @param skuNo
     * @param warehouseId
     * @param warehouseLocation
     * @return com.erp.model.wms.entity.StocktakingTaskDetailEntity
     */
    StocktakingTaskDetailEntity getTaskDetail(String mainId, String skuNo, String warehouseId, String warehouseLocation);

    /**
     * 更改差异数量
     * @author yl
     * @date 2023-08-22 11:57
     * @param taskDetailList
     * @return void
     */
    
    void updateQty(List<StocktakingTaskDetailEntity> taskDetailList);

    PagingVO<StocktakingTaskDetailDTO.ExportDTO> exportStocktakingTaskDetail(PagingDTO<BaseIdDTO> dto);
}
