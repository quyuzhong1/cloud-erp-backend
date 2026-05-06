package com.erp.server.dmp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDetailDTO;
import com.erp.model.dmp.dto.DmpRestCloudDTO;
import com.erp.model.dmp.dto.*;
import org.springframework.validation.annotation.Validated;

/**
 * restCloud服务调用接口
 *
 * @author Jim
 * @date 2025/10/30 17:11
 * @Return
 */
public interface DmpRestCloudService {

    /**
     * 流程信息分页接口
     * @param dto 请求参数
     * @return 响应json
     */
    PagingVO<DmpRestCloudDTO.ListDTO> flowPaging(@Validated PagingDTO<DmpRestCloudDTO.PagingParamDTO> dto);

    /**
     * 平台单据差异溯源分页接口（库存流水）
     * @author will
     * @date 2026/2/5 14:41
     * @param dto
     * @return PagingVO<SourceSelfDTO>
     */
    PagingVO<AdsErpOutstockDiffFlowDetailDTO.SourceSelfDTO> outstockSourceSelfPaging(@Validated PagingDTO<AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> dto);
    /**
     * 平台单据差异溯源分页接口（平台出库）
     * @author will
     * @date 2026/2/5 14:41
     * @param dto
     * @return PagingVO<SourcePlatformDTO>
     */
    PagingVO<AdsErpOutstockDiffFlowDetailDTO.SourcePlatformDTO> outstockSourcePlatformPaging(@Validated PagingDTO<AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> dto);
    /**
     * 流水差异溯源分页接口（库存流水）
     * @author will
     * @date 2026/2/5 14:45
     * @param dto
     * @return PagingVO<SourcePlatformDTO>
     */
    PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO> inventorySourceSelfPaging(@Validated PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto);

    /**
     * 流水差异溯源分页接口（每日库存）
     * @author will
     * @date 2026/2/5 14:45
     * @param dto
     * @return PagingVO<SourcePlatformDTO>
     */
    PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourcePlatformDTO> inventorySourcePlatformPaging(@Validated PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto);

    PagingVO<AdsErpDiffOutstockSyncDTO.SourcePlatformDTO> diffOutstockSyncSourcePlatformPaging(PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto);

    PagingVO<AdsErpDiffReturnInstockSyncDTO.SourcePlatformDTO> diffReturnInstockSourcePlatformPaging(PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto);
    /**
     * 签收流水差异溯源分页接口（直接调拨单）
     * @param dto 请求参数
     * @return 响应json
     */
    PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourceTransferInfoDTO> transferInfoPaging(PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto);
    /**
     * 签收流水差异溯源分页接口（库存流水）
     * @param dto 请求参数
     * @return 响应json
     */
    PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourcePlatformFlowDTO> sourcePlatformFlowPaging(PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto);
}
