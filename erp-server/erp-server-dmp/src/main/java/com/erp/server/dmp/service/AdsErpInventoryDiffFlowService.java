package com.erp.server.dmp.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDTO;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDetailDTO;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffFlowEntity;
import com.erp.server.dmp.enums.InventoryMonthCheckEnum;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 第三方仓流水差异表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-11-14
 */
public interface AdsErpInventoryDiffFlowService extends SuperService<AdsErpInventoryDiffFlowEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-11-14
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AdsErpInventoryDiffFlowDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-11-14
    * @param dto
    * @return
    */
    Boolean update(AdsErpInventoryDiffFlowDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author shukai
    * @date: 2025-11-14
    * @param pagingParamDTO
    * @return PagingVO<AdsErpInventoryDiffFlowDTO.ListDTO>>
    */
    PagingVO<AdsErpInventoryDiffFlowDTO.ListDTO> paging(PagingDTO<AdsErpInventoryDiffFlowDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author shukai
    * @date: 2025-11-14
    * @param dto
    * @return List<AdsErpInventoryDiffFlowDTO.TabListDTO>>
    */
    List<AdsErpInventoryDiffFlowDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author shukai
    * @date: 2025-11-14
    * @param id
    * @return
    */
    AdsErpInventoryDiffFlowDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author shukai
    * @date: 2025-11-14
    * @param dto
    * @param response
    * @return
    */
    void exportList(AdsErpInventoryDiffFlowDTO.ExportDTO dto, HttpServletResponse response);
    
    AdsErpInventoryDiffFlowDTO.TotalDTO total(PagingDTO<AdsErpInventoryDiffFlowDTO.PagingParamDTO> dto);
    
    Boolean updateRemark(AdsErpInventoryDiffFlowDTO.UpdateRemarkDTO dto);
    
    Boolean exportExcel(AdsErpInventoryDiffFlowDTO.ExpotParamDTO dto);
    
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) throws Exception;
    /**
     * 朔源信息-每日库存
     * @author will
     * @date 2026/2/5 09:28
     * @param dto
     * @return PagingVO<SourcePlatformDTO>
     */
    PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourcePlatformDTO> sourcePlatformPaging(PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto);
    /**
     * 导出朔源数据-每日库存
     * @author will
     * @date 2026/2/5 09:28
     * @param dto
     * @return Boolean
     */
    Boolean exportSourcePlatform(AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO dto);
    /**
     * 朔源信息 -按流水
     * @author will
     * @date 2026/2/5 09:29
     * @param dto
     * @return PagingVO<SourceSelfDTO>
     */
    PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO> sourceSelfPaging(PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto);
    /**
     * 导出- 按流水
     * @author will
     * @date 2026/2/5 09:29
     * @param dto
     * @return Boolean
     */
    Boolean exportSourceSelf(AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO dto);
    
    void updateReCreateInventoryMonthCheck(InventoryMonthCheckEnum inventoryMonthCheckEnum, String checkMonth,
			String sourceSystem);
}
