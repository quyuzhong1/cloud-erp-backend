package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.VirtualTransFlowDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;

/**
 * <p>
 * 虚拟仓库存流水明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
public interface VirtualTransFlowDetailService extends SuperService<VirtualTransFlowDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualTransFlowDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    Boolean update(VirtualTransFlowDetailDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author will
     * @date 2024/12/5 10:54
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<VirtualTransFlowDetailDTO.ListDTO> paging(PagingDTO<VirtualTransFlowDetailDTO.SearchParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/12/5 11:00
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(VirtualTransFlowDetailDTO.SearchParamDTO dto);
}
