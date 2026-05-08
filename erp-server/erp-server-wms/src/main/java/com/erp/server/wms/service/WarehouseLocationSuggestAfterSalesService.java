package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseLocationSuggestAfterSalesDto;
import com.erp.model.wms.entity.WarehouseLocationSuggestAfterSalesEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 仓位售后推荐 服务类
 * </p>
 *
 * @author liuchao
 * @since 2026-04-30
 */
public interface WarehouseLocationSuggestAfterSalesService extends SuperService<WarehouseLocationSuggestAfterSalesEntity> {

    /**
     * 仓位售后推荐分页查询
     * @date 2026-04-30
     * @author liuchao
     * @param pagingDTO 分页参数
     * @return 仓位售后推荐分页结果
     */
    PagingVO<WarehouseLocationSuggestAfterSalesDto.ListDTO> paging(PagingDTO<WarehouseLocationSuggestAfterSalesDto.SearchParamDTO> pagingDTO);

    /**
     * 新增仓位售后推荐
     * @date 2026-04-30
     * @author liuchao
     * @param dto 新增参数
     * @return 新增结果
     */
    Boolean addOrEdit(WarehouseLocationSuggestAfterSalesDto.AddOrEditDTO dto);

    /**
     * 删除仓位售后推荐
     * @date 2026-04-30
     * @author liuchao
     * @param entity 删除参数
     * @return 删除结果
     */
    BatchResultDTO delete(WarehouseLocationSuggestAfterSalesEntity entity);

    /**
     * 导出仓位售后推荐Excel
     * @date 2026-04-30
     * @author liuchao
     * @param dto 导出参数
     */
    void exportExcel(WarehouseLocationSuggestAfterSalesDto.ExportParamDTO dto);

    /**
     * 导入仓位售后推荐Excel
     * @date 2026-04-30
     * @author liuchao
     * @param file 文件
     * @param response 响应
     */
    void importExcel(MultipartFile file, HttpServletResponse response);

    /**
     * 下载导入模板
     * @date 2026-04-30
     * @author liuchao
     */
    void downloadTemplate(HttpServletResponse response);
}
