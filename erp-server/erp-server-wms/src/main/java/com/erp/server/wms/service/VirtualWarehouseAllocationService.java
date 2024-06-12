package com.erp.server.wms.service;

import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 虚拟仓分货单 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
public interface VirtualWarehouseAllocationService extends SuperService<VirtualWarehouseAllocationEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-05
     */
    BaseResultDTO.AddDTO add(VirtualWarehouseAllocationDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-05
     */
    Boolean update(VirtualWarehouseAllocationDTO.UpdateDTO dto);

    /**
     * 列表查询
     *
     * @param dto
     * @return PagingVO
     * @author hyj
     * @date: 2024-06-05
     */
    PagingVO<VirtualWarehouseAllocationDTO.ListDTO> paging(PagingDTO<VirtualWarehouseAllocationDTO.PagingParamDTO> dto);

    /**
     * 预览
     *
     * @param id
     * @return
     */
    VirtualWarehouseAllocationDTO.ViewDTO view(String id);

    /**
     * 提交
     *
     * @param ids
     * @return
     */
    BatchResultDTO submit(VirtualWarehouseAllocationEntity ids);

    /**
     * 作废
     *
     * @param allocationEntity
     * @param status
     * @param invalidDescription
     * @return
     */
    BatchResultDTO invalid(VirtualWarehouseAllocationEntity allocationEntity, String status, String invalidDescription);

    void export(VirtualWarehouseAllocationDTO.ExportDTO dto, HttpServletResponse response);

    VirtualWarehouseAllocationDTO.DetailDto importFile(String type, MultipartFile excelFile, HttpServletResponse response);

    List<VirtualWarehouseAllocationDTO.TabListDTO> tabList(PermissionsDTO dto);

    BatchResultDTO saveAndSubmit(VirtualWarehouseAllocationDTO.UpdateDTO dto);
}
