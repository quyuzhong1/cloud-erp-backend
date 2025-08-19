package com.erp.server.wms.service;

import cn.hutool.core.lang.Pair;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.entity.VirtualInventoryAgeEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 库龄分析表 服务类
 * </p>
 *
 * @author will
 * @since 2025-08-19
 */
public interface VirtualInventoryAgeService extends SuperService<VirtualInventoryAgeEntity> {

    /**
     * 虚拟仓库龄分析生成任我
     * @author will
     * @date 2025/8/19 14:44
     * @param date
     * @return void
     */
    void generateVirtualInventoryAge(LocalDate date);

    /**
     * 查看详情
     * @author will
     * @date 2024/12/4 16:25
     * @param dto
     * @return ViewDTO
     */
    VirtualInventoryAgeDTO.ViewDTO view(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto);
    /**
    /**
     * 分页查询
     * @author will
     * @date 2025/8/19 16:23
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<VirtualInventoryAgeDTO.ListDTO> paging(PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto);
    /**
     * 导出excel
     * @author will
     * @date 2025/8/19 16:23
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(VirtualInventoryAgeDTO.SearchParamDTO dto);

    /**
     * 库龄分析导出
     * @author will
     * @date 2024/12/23 15:28
     * @param dto
     * @return PagingVO<DynamicExcelDTO>
     */
    PagingVO<DynamicExcelDTO> exportWmsVirtualInventoryAge(PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto);

    /**
     * 查询配置导出表头信息
     * @author will
     * @date 2024/12/9 9:20
     * @return List<Pair<String,String>>
     */
    List<Pair<String,String>> getCfgHeadExport();

    /**
     * 查询配置表头信息
     * @author will
     * @date 2024/12/9 9:20
     * @return List<String>
     */
    List<String> getCfgHead();

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
     * 详情库龄图
     * @author will
     * @date 2024/12/9 9:48
     * @param dto
     * @return HisInventoryAgeChartDTO
     */
    VirtualInventoryAgeDTO.HisInventoryAgeChartDTO getHisInventoryAgeChart(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto);

    /**
     * 详情历史库龄明细
     * @author will
     * @date 2024/12/10 17:31
     * @param dto
     * @return viewHisInventoryAgeDetailDTO
     */
    VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO viewHisInventoryAgeDetail(VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto);
    /**
     * 列表弹框分页查询
     * @author will
     * @date 2024/12/10 18:21
     * @param dto
     * @return PagingVO<HisInventoryAgeDetailDTO>
     */
    PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> framePaging(PagingDTO<VirtualInventoryAgeDTO.FrameParamDTO> dto);
    /**
     * 列表弹框导出
     * @author will
     * @date 2024/12/10 18:22
     * @param dto
     * @return Boolean
     */
    Boolean frameExportExcel(VirtualInventoryAgeDTO.FrameParamDTO dto);

    /**
     * 差异导出
     * @param dto
     * @return
     */
    Boolean diffExportExcel(VirtualInventoryAgeDTO.SearchParamDTO dto);
}
