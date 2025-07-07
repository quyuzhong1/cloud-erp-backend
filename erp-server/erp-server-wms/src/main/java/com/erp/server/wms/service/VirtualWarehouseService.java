package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;

import java.util.List;

/**
 * <p>
 * 虚拟仓 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
public interface VirtualWarehouseService extends SuperService<VirtualWarehouseEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-02
     */
    BaseResultDTO.AddDTO add(VirtualWarehouseDTO.AddDTO dto);
    /**
     * 修改
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-02
     */
    Boolean update(VirtualWarehouseDTO.UpdateDTO dto);


    PagingVO<VirtualWarehouseDTO.ListDTO> paging(PagingDTO<VirtualWarehouseDTO.PagingParamDTO> dto);

    /**
     * 修改状态
     */
    Boolean updateState(VirtualWarehouseDTO.UpdateStateDTO updateStateDTO);

    /**
     * 预览
     *
     * @param id
     * @return
     */
    VirtualWarehouseDTO.ViewDTO view(String id);

    List<VirtualWarehouseDTO.Tree> tree(String key, String id);

    PagingVO<ShopDTO.ListDTO> pagingSelect(PagingDTO<VirtualWarehouseDTO.ShopSelectDTO> dto);

    List<VirtualWarehouseDTO.VwDTO> getByNames(List<String> list);

    /**
     * 虚拟仓高级搜索
     * @param dto
     * @return
     */
    PagingVO<VirtualWarehouseDTO.SelectDTO> warehousePagingSelect(PagingDTO<VirtualWarehouseDTO.WarehouseSelectDTO> dto);

    /**
     * 获取虚拟仓库列表
     * @param dto
     * @return
     */
    List<VirtualWarehouseDTO.SelectDTO> warehouseSelectList(PagingDTO<VirtualWarehouseDTO.WarehouseSelectDTO> dto);

    /**
     * 通过仓库，平台，关联id 获取虚拟仓库列表
     * @return
     */
    List<VirtualWarehouseDTO.SelectDTO> listByParam(VirtualWarehouseDTO.SearchDTO searchDTO);

    /**
     * 根据高级查询查找虚拟仓
     * @param compareCodeSplicingValueSql 高级查询
     */
    List<String> listWarehouseBySql(String compareCodeSplicingValueSql);

    /**
     * 导出
     * @param dto
     * @return
     */
    Boolean exportExcel(VirtualWarehouseDTO.PagingParamDTO dto);

    /**
     * 导出虚拟仓设置
     * @param dto
     * @return
     */
    PagingVO<VirtualWarehouseDTO.ExportDTO> exportVirtualWarehouse(PagingDTO<VirtualWarehouseDTO.PagingParamDTO> dto);

    List<VirtualWarehouseDTO.ViewWarehouseDTO> listWarehouseInfoByIds(List<String> virtualWarehouseIdList);

    List<VirtualWarehouseEntity> listByNameList(List<String> virtualWarehouseNameList);
}
