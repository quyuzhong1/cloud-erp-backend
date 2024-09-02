package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsWarehouseMappingDTO;
import com.erp.model.tms.entity.TmsWarehouseMappingEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
public interface TmsWarehouseMappingService extends SuperService<TmsWarehouseMappingEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsWarehouseMappingDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    Boolean update(TmsWarehouseMappingDTO.UpdateDTO dto);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/3/19 11:39
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<TmsWarehouseMappingDTO.ListDTO> paging(PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2024/3/19 11:56
     * @param id
     * @return ViewDTO
     */
    TmsWarehouseMappingDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2024/3/19 11:58
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);
    /**
     * @description: 下载模板
     * @author Will
     * @date: 2024/3/19 12:02
     * @param response

     */
    void downloadTemplate(HttpServletResponse response);
    /**
     * @description: 导入
     * @author Will
     * @date: 2024/3/19 12:04
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2024/3/19 12:06
     */
    Boolean exportExcel(TmsWarehouseMappingDTO.PagingParamDTO dto);

    PagingVO<TmsWarehouseMappingDTO.ListDTO> exportWarehouseMapping(PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto);
}
