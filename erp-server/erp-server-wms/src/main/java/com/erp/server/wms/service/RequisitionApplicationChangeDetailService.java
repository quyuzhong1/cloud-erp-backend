package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.RequisitionApplicationChangeDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationChangeEntity;

import java.util.List;

/**
 * <p>
 * 要货申请变更明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
 */
public interface RequisitionApplicationChangeDetailService extends SuperService<RequisitionApplicationChangeDetailEntity> {

    /**
     * 新增
     *
     * @param entity
     * @param addDTO
     * @author lrp
     * @date: 2024-11-18
     */
    void add(RequisitionApplicationChangeEntity entity, RequisitionApplicationChangeDTO.ViewDTO addDTO);

    /**
     * 修改
     *
     * @param dto
     * @author lrp
     * @date: 2024-11-18
     */
    void update(RequisitionApplicationChangeDTO.ViewDTO dto, RequisitionApplicationChangeEntity entity);


    List<RequisitionApplicationChangeDTO.ExistDTO> checkExist(List<String> sourceDetailIds, List<String> businessDetailIds);

    List<RequisitionApplicationChangeDetailEntity> listByMains(List<String> mainIds);

    void addByPicking(RequisitionApplicationChangeEntity requisitionApplicationChangeEntity, PickingListsDTO.AddChangeDTO addChangeDTO);

    void updateVirtualWarehouse(List<RequisitionApplicationChangeDTO.ApproveView> approveViewList);
}
