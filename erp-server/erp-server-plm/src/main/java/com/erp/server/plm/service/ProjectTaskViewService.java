package com.erp.server.plm.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;

import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/11/23 18:57
 */
public interface ProjectTaskViewService {

    /**
     * @description: 项目视图按人员查询
     * @author Will
     * @date: 2022/11/23 11:50
     * @param dto
     * @return List<ProductTaskPersonnelChildDTO>
     */
    List<ProductTaskPersonnelChildDTO> getPersonnelView(ProductTaskViewSearchDTO dto);
    /**
     * @description: 项目视图按产品查询
     * @author Will
     * @date: 2022/11/23 11:51
     * @param dto
     * @return List<ProductTaskProductChildDTO>
     */
    List<ProductTaskProductChildDTO> getProductView(ProductTaskViewSearchDTO dto);
    /**
     * @description: 项目视图按阶段查询
     * @author Will
     * @date: 2022/11/23 11:51
     * @param dto
     * @return List<ProductTaskPhaseChildDTO>
     */
    List<ProductTaskPhaseChildDTO> getPhaseView(ProductTaskViewSearchDTO dto);
    /**
     * @description: 项目视图按量产入库时间查询
     * @author Will
     * @date: 2022/11/23 11:51
     * @param dto
     * @return List<ProductTaskInWarehouseTimeChildDTO>
     */
    List<ProductTaskInWarehouseTimeChildDTO> getInWarehouseTimeView(ProductTaskViewSearchDTO dto);
    /**
     * @param dto
     * @description: 项目视图导出
     * @author Will
     * @date: 2022/11/23 16:16
     */
    void exportExcel(ProductTaskViewSearchDTO dto);

    PagingVO<ProductTaskViewDTO> exportProductTaskView(PagingDTO<ProductTaskViewSearchDTO> dto);
}
