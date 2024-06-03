package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualTransFlowDTO;

/**
 * <p>
 * 虚拟库存交易流水表 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
public interface VirtualTransFlowService extends SuperService<VirtualTransFlowEntity> {
    /**
     * 虚拟库存流水分页查询
     * @author will
     * @date 2024/6/3 17:08
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<VirtualTransFlowDTO.ListDTO> paging(PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto);
}
