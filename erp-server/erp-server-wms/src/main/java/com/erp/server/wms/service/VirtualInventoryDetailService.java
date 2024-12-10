package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;

import java.util.List;

/**
 * <p>
 * 虚拟仓库明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
public interface VirtualInventoryDetailService extends SuperService<VirtualInventoryDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-03
    * @param addOrUpdateDTO
    * @return
    */
    VirtualInventoryDetailEntity addOrUpdate(VirtualInventoryDetailDTO.UpdateDTO addOrUpdateDTO);

    /**
     * 分页列表
     * @author will
     * @date 2024/12/3 17:34
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<VirtualInventoryAgeDTO.ListDTO> paging(PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto);
    /**
     * 导出excel
     * @author will
     * @date 2024/12/3 18:03
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(VirtualInventoryAgeDTO.SearchParamDTO dto);
    /**
     * 查看详情
     * @author will
     * @date 2024/12/4 16:25
     * @param dto
     * @return ViewDTO
     */
    VirtualInventoryAgeDTO.ViewDTO view(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto);
    /**
     * 导出历史库龄
     * @author will
     * @date 2024/12/4 16:40
     * @param dto 
     * @return Boolean
     */
    Boolean exportHisInventoryAge(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto);
    /**
     * 查询历史库龄
     * @author will
     * @date 2024/12/5 9:43
     * @param dto
     * @return PagingVO<HisInventoryAgeDTO>
     */
    PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDTO> hisInventoryAgePaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeParamDTO> dto);

    /**
     * 查询历史库龄
     * @author will
     * @date 2024/12/5 9:43
     * @param dto
     * @return PagingVO<HisInventoryAgeDetailDTO>
     */
    PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> hisInventoryAgeDetailPaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO> dto);

    /**
     * 导出历史库龄明细
     * @author will
     * @date 2024/12/4 16:40
     * @param dto
     * @return Boolean
     */
    Boolean exportHisInventoryAgeDetail(VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto);
    /**
     * 导出历史库龄明细数据查询
     * @author will
     * @date 2024/12/9 10:57
     * @param dto
     * @return PagingVO<HisInventoryAgeDetailDTO>
     */
    PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> exportHisInventoryAgeDetailPaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO> dto);
    /**
     * 查询配置表头信息
     * @author will
     * @date 2024/12/9 9:20
     * @return List<String>
     */
    List<String> getCfgHead();
    /**
     * 详情库龄图
     * @author will
     * @date 2024/12/9 9:48
     * @param dto
     * @return HisInventoryAgeChartDTO
     */
    VirtualInventoryAgeDTO.HisInventoryAgeChartDTO getHisInventoryAgeChart(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto);
    /**
     * 根据出库参数查询
     * @author will
     * @date 2024/12/10 16:24
     * @param skuId
     * @param warehouseId
     * @param virtualWarehouseId
     * @return List<VirtualInventoryDetailEntity>
     */
    List<VirtualInventoryDetailEntity> getByOutParam(String skuId, String warehouseId, String virtualWarehouseId);
}
