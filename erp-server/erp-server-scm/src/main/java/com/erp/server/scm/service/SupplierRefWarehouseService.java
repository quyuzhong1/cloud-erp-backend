package com.erp.server.scm.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierRefWarehouseDTO;
import com.erp.model.scm.entity.SupplierRefWarehouseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 供应商关联仓库表 服务类
 * </p>
 *
 * @author will
 * @since 2025-06-18
 */
public interface SupplierRefWarehouseService extends SuperService<SupplierRefWarehouseEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-06-18
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SupplierRefWarehouseDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-06-18
    * @param dto
    * @return
    */
    Boolean update(SupplierRefWarehouseDTO.UpdateDTO dto);

    /**
     * 查询详情
     * @author will
     * @date 2025/6/18 16:51
     * @param id
     * @return ViewDTO
     */
    SupplierRefWarehouseDTO.ViewDTO view(String id);
    /**
     * 分页查询
     * @author will
     * @date 2025/6/18 17:21
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<SupplierRefWarehouseDTO.ListDTO> paging(PagingDTO<SupplierRefWarehouseDTO.PagingParamDTO> dto);
    /**
     * 列表数量查询
     * @author will
     * @date 2025/6/18 17:30
     * @param dto
     * @return List<TabListDTO>
     */
    List<SupplierRefWarehouseDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * 删除
     * @author will
     * @date 2025/6/18 17:42
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);
    /**
     * 更新禁用状态
     * @author will
     * @date 2025/6/18 17:53
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO updateDisabled(String id,Boolean disabled);
    /**
     * 下载模板
     * @author will
     * @date 2025/6/18 17:56
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);
    /**
     * 导入excel
     * @author will
     * @date 2025/6/18 17:56
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);
    /**
     * 导出
     * @author will
     * @date 2025/6/18 18:00
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(SupplierRefWarehouseDTO.PagingParamDTO dto);
}
