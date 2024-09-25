package com.erp.server.wms.service;

import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseDTO;

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
//    /**
//     * 新增
//     *
//     * @param dto
//     * @return
//     * @author hyj
//     * @date: 2024-06-02
//     */
//    BaseResultDTO.AddDTO addAndBind(VirtualWarehouseDTO.AddDTO dto);
    //绑定信息
//    void bindInfo(List<String> warehouseIdList, List<VirtualWarehouseChannelDTO.ChannelAddDTO> channelList,
//                          List<ThirdMappingDTO.AddDTO> thirdMappingList, String virtualWarehouseEntityId);
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

}
