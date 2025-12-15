package com.erp.server.dmp.service;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO;
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
}
