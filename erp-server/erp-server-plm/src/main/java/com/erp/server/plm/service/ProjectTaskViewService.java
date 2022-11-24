package com.erp.server.plm.service;

import com.erp.model.plm.dto.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/23 18:57
 */
public interface ProjectTaskViewService {

    /**
     * @description: 项目视图按人员查询
     * @author Will
     * @date: 2022/11/23 11:50
     * @param dto
     * @return List<ProductTaskPersonnelViewDTO>
     */
    List<ProductTaskPersonnelChildDTO> getPersonnelView(ProductTaskViewSearchDTO dto);
    /**
     * @description: 项目视图按产品查询
     * @author Will
     * @date: 2022/11/23 11:51
     * @param dto
     * @return List<ProductTaskProductViewDTO>
     */
    List<ProductTaskProductViewDTO> getProductView(ProductTaskViewSearchDTO dto);
    /**
     * @description: 项目视图按阶段查询
     * @author Will
     * @date: 2022/11/23 11:51
     * @param dto
     * @return List<ProductTaskPhaseViewDTO>
     */
    List<ProductTaskPhaseViewDTO> getPhaseView(ProductTaskViewSearchDTO dto);
    /**
     * @description: 项目视图按量产入库时间查询
     * @author Will
     * @date: 2022/11/23 11:51
     * @param dto
     * @return List<ProductTaskInWarehouseTimeViewDTO>
     */
    List<ProductTaskInWarehouseTimeViewDTO> getInWarehouseTimeView(ProductTaskViewSearchDTO dto);
    /**
     * @description: 项目视图导出
     * @author Will
     * @date: 2022/11/23 16:16
     * @param dto
     * @param response

     */
    void exportExcel(ProductTaskViewSearchDTO dto, HttpServletResponse response);
}
