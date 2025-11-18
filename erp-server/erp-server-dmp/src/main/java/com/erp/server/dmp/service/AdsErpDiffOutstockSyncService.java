package com.erp.server.dmp.service;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO;
import com.erp.model.dmp.entity.doris.AdsErpDiffOutstockSyncEntity;

/**
 * <p>
 * ERP出库单差异表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-11-18
 */
public interface AdsErpDiffOutstockSyncService extends SuperService<AdsErpDiffOutstockSyncEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-11-18
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AdsErpDiffOutstockSyncDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-11-18
    * @param dto
    * @return
    */
    Boolean update(AdsErpDiffOutstockSyncDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author shukai
    * @date: 2025-11-18
    * @param pagingParamDTO
    * @return PagingVO<AdsErpDiffOutstockSyncDTO.ListDTO>>
    */
    PagingVO<AdsErpDiffOutstockSyncDTO.ListDTO> paging(PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author shukai
    * @date: 2025-11-18
    * @param dto
    * @return List<AdsErpDiffOutstockSyncDTO.TabListDTO>>
    */
    List<AdsErpDiffOutstockSyncDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author shukai
    * @date: 2025-11-18
    * @param id
    * @return
    */
    AdsErpDiffOutstockSyncDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author shukai
    * @date: 2025-11-18
    * @param dto
    * @param response
    * @return
    */
    void exportList(AdsErpDiffOutstockSyncDTO.ExportDTO dto, HttpServletResponse response);
    
    AdsErpDiffOutstockSyncDTO.TotalDTO total(PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto);
    
    Boolean reCreate(AdsErpDiffOutstockSyncDTO.ReCreateDTO dto);
    
    Boolean updateErp(AdsErpDiffOutstockSyncDTO.UpdateErpDTO dto);
    
    Boolean updateRemark(AdsErpDiffOutstockSyncDTO.UpdateRemarkDTO dto);
    
    Boolean exportExcel(AdsErpDiffOutstockSyncDTO.ExpotParamDTO dto);
}
