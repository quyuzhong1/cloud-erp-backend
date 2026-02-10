package com.erp.server.dmp.service;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO;
import com.erp.model.dmp.entity.doris.AdsErpDiffReturnInstockSyncEntity;

/**
 * <p>
 * ERP退货入库单差异表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-11-19
 */
public interface AdsErpDiffReturnInstockSyncService extends SuperService<AdsErpDiffReturnInstockSyncEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-11-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AdsErpDiffReturnInstockSyncDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-11-19
    * @param dto
    * @return
    */
    Boolean update(AdsErpDiffReturnInstockSyncDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author shukai
    * @date: 2025-11-19
    * @param pagingParamDTO
    * @return PagingVO<AdsErpDiffReturnInstockSyncDTO.ListDTO>>
    */
    PagingVO<AdsErpDiffReturnInstockSyncDTO.ListDTO> paging(PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author shukai
    * @date: 2025-11-19
    * @param dto
    * @return List<AdsErpDiffReturnInstockSyncDTO.TabListDTO>>
    */
    List<AdsErpDiffReturnInstockSyncDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author shukai
    * @date: 2025-11-19
    * @param id
    * @return
    */
    AdsErpDiffReturnInstockSyncDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author shukai
    * @date: 2025-11-19
    * @param dto
    * @param response
    * @return
    */
    void exportList(AdsErpDiffReturnInstockSyncDTO.ExportDTO dto, HttpServletResponse response);
    
    AdsErpDiffReturnInstockSyncDTO.TotalDTO total(PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto);
    
    Boolean reCreate(AdsErpDiffReturnInstockSyncDTO.ReCreateDTO dto);
    
    Boolean updateErp(AdsErpDiffReturnInstockSyncDTO.UpdateErpDTO dto);
    
    Boolean updateRemark(AdsErpDiffReturnInstockSyncDTO.UpdateRemarkDTO dto);
    
    Boolean exportExcel(AdsErpDiffReturnInstockSyncDTO.ExpotParamDTO dto);

    PagingVO<AdsErpDiffReturnInstockSyncDTO.SourcePlatformDTO> sourcePlatformPaging(PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto);
}
