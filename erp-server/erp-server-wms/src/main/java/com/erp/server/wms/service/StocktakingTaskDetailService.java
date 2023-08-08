package com.erp.server.wms.service;

import com.common.business.dto.base.BaseIdDTO;
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
     * @param dto
     * @param response
     * @return
     */
    Boolean exportExcel(BaseIdDTO dto, HttpServletResponse response);
    /**
     * 导入明细
     * @author yl
     * @date 2023-08-03 17:58
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 更新明细
     * @author yl
     * @date 2023-08-03 17:59
     * @param dto
     * @return 
     */
    Boolean updateDetail(StocktakingTaskDetailDTO.UpdateDTO dto);

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
}
