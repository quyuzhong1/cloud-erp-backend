package com.erp.server.wms.service;
import com.erp.model.wms.entity.VirtualInventoryTransactionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualInventoryTransactionDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 虚拟仓库存事务表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-12-18
 */
public interface VirtualInventoryTransactionService extends SuperService<VirtualInventoryTransactionEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-12-18
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualInventoryTransactionDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-12-18
    * @param dto
    * @return
    */
    Boolean update(VirtualInventoryTransactionDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author shukai
    * @date: 2025-12-18
    * @param pagingParamDTO
    * @return PagingVO<VirtualInventoryTransactionDTO.ListDTO>>
    */
    PagingVO<VirtualInventoryTransactionDTO.ListDTO> paging(PagingDTO<VirtualInventoryTransactionDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author shukai
    * @date: 2025-12-18
    * @param dto
    * @return List<VirtualInventoryTransactionDTO.TabListDTO>>
    */
    List<VirtualInventoryTransactionDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author shukai
    * @date: 2025-12-18
    * @param id
    * @return
    */
    VirtualInventoryTransactionDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author shukai
    * @date: 2025-12-18
    * @param dto
    * @param response
    * @return
    */
    void exportList(VirtualInventoryTransactionDTO.ExportDTO dto, HttpServletResponse response);
}
