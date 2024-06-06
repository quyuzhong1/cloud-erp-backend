package com.erp.server.wms.service;

import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.transaction.annotation.Transactional;

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
     * 提交
     *
     * @param ids
     * @return
     */
    List<BatchResultDTO> submit(List<String> ids);

    /**
     * 作废
     *
     * @param dto
     * @return
     */
    List<BatchResultDTO> invalid(BaseIdsDTO.RemarkDTO dto);

    /**
     * 变更状态
     *
     * @param allocationEntity
     * @param status
     * @param invalidDescription
     * @return
     */
    BatchResultDTO updateStatus(VirtualWarehouseAllocationEntity allocationEntity, String status, String invalidDescription);
    /**
     * 手动完结
     *
     * @param dto
     * @return
     */
    BatchResultDTO manualFinish(VirtualWarehouseAllocationDTO.ManualFinishDto dto);
}
