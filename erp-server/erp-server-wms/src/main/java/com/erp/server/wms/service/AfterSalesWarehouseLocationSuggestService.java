package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.entity.AfterSalesWarehouseLocationSuggestEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 仓位售后推荐 服务类
 * </p>
 *
 * @author liuchao
 * @since 2026-04-30
 */
public interface AfterSalesWarehouseLocationSuggestService extends SuperService<AfterSalesWarehouseLocationSuggestEntity> {

    /**
     * 仓位售后推荐分页查询
     *
     * @param pagingDTO 分页参数
     * @return 仓位售后推荐分页结果
     * @date 2026-04-30
     * @author liuchao
     */
    PagingVO<AfterSalesWarehouseLocationSuggestDto.ListDTO> paging(PagingDTO<AfterSalesWarehouseLocationSuggestDto.SearchParamDTO> pagingDTO);

    /**
     * 新增仓位售后推荐
     *
     * @param dto 新增参数
     * @return 新增结果
     * @date 2026-04-30
     * @author liuchao
     */
    Boolean addOrEdit(AfterSalesWarehouseLocationSuggestDto.AddOrEditDTO dto);

    /**
     * 删除仓位售后推荐
     *
     * @param entity 删除参数
     * @return 删除结果
     * @date 2026-04-30
     * @author liuchao
     */
    BatchResultDTO delete(AfterSalesWarehouseLocationSuggestEntity entity);

    /**
     * 导出仓位售后推荐Excel
     *
     * @param dto 导出参数
     * @date 2026-04-30
     * @author liuchao
     */
    void exportExcel(AfterSalesWarehouseLocationSuggestDto.ExportParamDTO dto);

    /**
     * 导入仓位售后推荐Excel
     *
     * @param file     文件
     * @param response 响应
     * @date 2026-04-30
     * @author liuchao
     * @date 2026-04-30
     * @author liuchao
     */
    void importExcel(MultipartFile file, HttpServletResponse response);

    /**
     * 下载导入模板
     *
     * @date 2026-04-30
     * @author liuchao
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 批量更新状态
     *
     * @param dto 更新参数
     * @date 2026-04-30
     * @author liuchao
     */
    void updateDisabled(AfterSalesWarehouseLocationSuggestDto.UpdateStatusDto dto);

    /**
     * 批量更新状态
     *
     * @param dto 批量更新参数
     * @return 更新结果
     * @date 2026-04-30
     * @author liuchao
     */
    List<BatchResultDTO> updateStatusBatch(AfterSalesWarehouseLocationSuggestDto.UpdateStatusDto dto);


    /**
     * PDA获取仓位售后推荐列表
     *
     * @param dto 搜索参数
     * @return 仓位售后推荐列表
     * @date 2026-05-09
     * @author liuchao
     */
    List<AfterSalesWarehouseLocationSuggestDto.PdaListDto> getSuggestWarehouseLocationList(AfterSalesWarehouseLocationSuggestDto.PdaSearchDto dto);
}
