package com.erp.server.dmp.service;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDetailDTO;
import com.erp.model.dmp.entity.doris.AdsErpOutstockDiffFlowEntity;

/**
 * <p>
 * 第三方仓出库单据差异表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-11-12
 */
public interface AdsErpOutstockDiffFlowService extends SuperService<AdsErpOutstockDiffFlowEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-11-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AdsErpOutstockDiffFlowDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-11-12
    * @param dto
    * @return
    */
    Boolean update(AdsErpOutstockDiffFlowDTO.UpdateDTO dto);

    PagingVO<AdsErpOutstockDiffFlowDTO.PagingDTO> paging(PagingDTO<AdsErpOutstockDiffFlowDTO.PagingParamDTO> dto);
    
    AdsErpOutstockDiffFlowDTO.TotalDTO total(PagingDTO<AdsErpOutstockDiffFlowDTO.PagingParamDTO> dto);
    
    Boolean reCreate(AdsErpOutstockDiffFlowDTO.ReCreateDTO dto);
    
    Boolean updateRemark(AdsErpOutstockDiffFlowDTO.UpdateRemarkDTO dto);
    
    Boolean exportExcel(AdsErpOutstockDiffFlowDTO.ExpotParamDTO dto);
    /**
     * 朔源数据分页
     * @author will
     * @date 2026/2/4 14:32
     * @param dto
     * @return PagingVO<SourceSelfDTO>
     */
    PagingVO<AdsErpOutstockDiffFlowDetailDTO.SourceSelfDTO> sourceSelfPaging(PagingDTO<AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> dto);
    /**
     * 朔源数据分页
     * @author will
     * @date 2026/2/4 15:59
     * @param dto
     * @return PagingVO<SourcePlatformDTO>
     */
    PagingVO<AdsErpOutstockDiffFlowDetailDTO.SourcePlatformDTO> sourcePlatformPaging(PagingDTO<AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> dto);
    /**
     * 导出朔源数据-平台
     * @author will
     * @date 2026/2/5 09:21
     * @param dto
     * @return Boolean
     */
    Boolean exportSourcePlatform(AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO dto);
    /**
     * 导出朔源数据-自有
     * @author will
     * @date 2026/2/5 09:22
     * @param dto
     * @return Boolean
     */
    Boolean exportSourceSelf(AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO dto);
}
