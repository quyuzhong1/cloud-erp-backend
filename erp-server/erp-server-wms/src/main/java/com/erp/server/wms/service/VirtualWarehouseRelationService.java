package com.erp.server.wms.service;

import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;

import java.util.List;

/**
 * <p>
 * 虚拟仓实体仓关联关系 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
public interface VirtualWarehouseRelationService extends SuperService<VirtualWarehouseRelationEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-02
     */
    BaseResultDTO.AddDTO add(VirtualWarehouseRelationDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-02
     */
    Boolean update(VirtualWarehouseRelationDTO.UpdateDTO dto);

    /**
     * 根据仓库id获取关联关系
     *
     * @param warehouseIdList
     * @return
     */
    List<VirtualWarehouseRelationEntity> getByWarehouseId(List<String> warehouseIdList);
    /**
     * 根据虚拟仓库id获取关联关系
     *
     * @param virtualWarehouseId
     * @return
     */
    List<VirtualWarehouseRelationEntity> getByVirtualWarehouseId(String virtualWarehouseId);

    /**
     * 批量新增
     *
     * @param batchAddDTO
     * @return
     */
    BaseResultDTO.AddDTO batchAdd(VirtualWarehouseRelationDTO.BatchAddDTO batchAddDTO);

    PagingVO<VirtualWarehouseRelationDTO.SelectResultDTO> warehousePagingSelect(PagingDTO<VirtualWarehouseRelationDTO.SelectDTO> dto);
    PagingVO<VirtualWarehouseRelationDTO.SelectResultDTO> vmPagingSelect(PagingDTO<VirtualWarehouseRelationDTO.SelectDTO> dto);
    /**
     * 根据实体仓库id查询
     * @author will
     * @date 2024/6/12 14:10
     * @param warehouseIdList
     * @param virtualWarehouseIdList
     * @return List<VirtualWarehouseRelationEntity>
     */
    List<VirtualWarehouseRelationEntity> listByWarehouseIdList(List<String> warehouseIdList,List<String> virtualWarehouseIdList);
    /**
     * 根据虚拟仓id集合查询
     * @author will
     * @date 2024/9/3 18:20
     * @param virtualWarehouseIdList
     * @return List<VirtualWarehouseRelationEntity>
     */
    List<VirtualWarehouseRelationEntity> listByVirtualWarehouseIdList(List<String> virtualWarehouseIdList);
}
