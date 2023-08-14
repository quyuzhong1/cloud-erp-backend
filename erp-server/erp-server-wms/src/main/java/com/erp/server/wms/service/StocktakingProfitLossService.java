package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.StocktakingTaskEntity;

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
     * 盘点任务单审核通过生成盘盈盘亏单
     * @author yl
     * @date 2023-08-10 11:52
     * @param taskEntity
     * @return void
     */
    void autoCreateBill(StocktakingTaskEntity taskEntity);

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
}
