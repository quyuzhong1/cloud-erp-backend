package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseLocationSafetyInventoryDTO;
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
    PagingVO<WarehouseLocationSafetyInventoryDTO.ViewDTO> paging(PagingDTO<WarehouseLocationSafetyInventoryDTO.SearchParamDTO> pagingDTO);

    /**
     * 根据ID修改 安全库存 与 库存上限
     *
     * @param updateParamDto 更新参数
     * @return 执行成功返回null，否则返回BaseResultDTO.UpdateDTO
     * @date: 2024-06-21
     * @author: tanmujin
     */
    BaseResultDTO.UpdateDTO updateInventory(WarehouseLocationSafetyInventoryDTO.UpdateParamDTO updateParamDto);

    /**
     * 导入Excel
     *
     * @param file 文件
     * @return
     * @date: 2024-06-21
     * @author: tanmujin
     */
    boolean importExcel(MultipartFile file, HttpServletResponse response);

    /**
     * 导出Excel
     *
     * @param dto 导出参数
     * @return
     * @date: 2024-06-21
     * @author: tanmujin
     */
    boolean exportExcel(WarehouseLocationSafetyInventoryDTO.exportParamDTO dto);

    /**
     * 下载导入模板
     * @param response
     * @return
     * @date: 2024-06-26
     * @author: tanmujin
     */
    void downloadTemplate(HttpServletResponse response);

    PagingVO<WarehouseLocationSafetyInventoryDTO.ViewDTO> exportWarehouseLocationSafetyInventory(PagingDTO<WarehouseLocationSafetyInventoryDTO.exportParamDTO> dto);
}
