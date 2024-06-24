package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.wms.dto.WarehouseLocationSafetyInventoryDto;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.erp.model.wms.entity.WarehouseLocationSafetyInventoryEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 仓位安全库存
 * @date 2024-06-21
 * @author tanmujin
 */
public interface WarehouseLocationSafetyInventoryService extends SuperService<WarehouseLocationSafetyInventoryEntity>{
    /**
     * 分页查询
     * @param pagingDTO 分页查询参数
     * @return 仓位安全库存 列表
     * @date: 2024-06-21
     * @author: tanmujin
     */
    PagingVO<WarehouseLocationSafetyInventoryDto.ViewDto> paging(PagingDTO<WarehouseLocationSafetyInventoryDto.SearchParamDto> pagingDTO);

    /**
     * 根据ID修改 安全库存 与 库存上限
     * @param updateParamDto 更新参数
     * @date: 2024-06-21
     * @author: tanmujin
     */
    int updateInventory(WarehouseLocationSafetyInventoryDto.UpdateParamDto updateParamDto);

    /**
     * 导入Excel
     * @param file 文件
     * @date: 2024-06-21
     * @author: tanmujin
     */
    void importExcel(MultipartFile file, HttpServletResponse response);

    /**
     * 导出Excel
     * @param dto 导出参数
     * @date: 2024-06-21
     * @author: tanmujin
     */
    void exportExcel(WarehouseLocationSafetyInventoryDto.exportParamDto dto, HttpServletResponse response);
}
