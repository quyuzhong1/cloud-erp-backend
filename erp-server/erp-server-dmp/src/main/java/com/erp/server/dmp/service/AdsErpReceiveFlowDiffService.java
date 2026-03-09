package com.erp.server.dmp.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDTO;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDetailDTO;
import com.erp.model.dmp.entity.doris.AdsErpReceiveFlowDiffEntity;

import java.util.List;

/**
 * <p>
 * ERP出库单差异表 服务类
 * </p>
 *
 * @author will
 * @since 2026-03-09
 */
public interface AdsErpReceiveFlowDiffService extends SuperService<AdsErpReceiveFlowDiffEntity> {

    /**
    * 新增
    * @author will
    * @date: 2026-03-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AdsErpReceiveFlowDiffDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2026-03-09
    * @param dto
    * @return
    */
    Boolean update(AdsErpReceiveFlowDiffDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author will
    * @date: 2026-03-09
    * @param pagingParamDTO
    * @return PagingVO<AdsErpReceiveFlowDiffDTO.ListDTO>>
    */
    PagingVO<AdsErpReceiveFlowDiffDTO.ListDTO> paging(PagingDTO<AdsErpReceiveFlowDiffDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author will
    * @date: 2026-03-09
    * @param dto
    * @return List<AdsErpReceiveFlowDiffDTO.TabListDTO>>
    */
    List<AdsErpReceiveFlowDiffDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author will
    * @date: 2026-03-09
    * @param id
    * @return
    */
    AdsErpReceiveFlowDiffDTO.ViewDTO view(String id);

    /**
     * 获取统计数据
     * @author will
     * @date 2026/3/10 17:25
     * @param dto
     * @return TotalDTO
     */
    AdsErpReceiveFlowDiffDTO.TotalDTO total(PagingDTO<AdsErpReceiveFlowDiffDTO.PagingParamDTO> dto);
    /**
     * 重新生成差异数据
     * @author will
     * @date 2026/3/11 10:00
     * @param dto
     * @return Boolean
     */
    Boolean reCreate(AdsErpReceiveFlowDiffDTO.ReCreateDTO dto);
    /**
     * 更新ERP单号
     * @author will
     * @date 2026/3/11 10:30
     * @param dto
     * @return Boolean
     */
    Boolean updateErp(AdsErpReceiveFlowDiffDTO.UpdateErpDTO dto);
    /**
     * 更新备注
     * @author will
     * @date 2026/3/11 10:45
     * @param dto
     * @return Boolean
     */
    Boolean updateRemark(AdsErpReceiveFlowDiffDTO.UpdateRemarkDTO dto);
    /**
     * 导出差异数据
     * @author will
     * @date 2026/3/11 11:00
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(AdsErpReceiveFlowDiffDTO.ExportParamDTO dto);
    /**
     * 获取来源单据信息
     * @author will
     * @date 2026/3/11 14:00
     * @param dto
     * @return PagingVO<SourceTransferInfoDTO>
     */
    PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourceTransferInfoDTO> transferInfoPaging(PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto);
    /**
     * 导出来源单据信息
     * @author will
     * @date 2026/3/11 14:30
     * @param dto
     * @return Boolean
     */
    Boolean exportTransferInfo(AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO dto);
    /**
     * 获取来源平台流水信息
     * @author will
     * @date 2026/3/11 15:00
     * @param dto
     * @return PagingVO<SourcePlatformFlowDTO>
     */
    PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourcePlatformFlowDTO> sourcePlatformFlowPaging(PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto);
    /**
     * 导出来源平台流水信息
     * @author will
     * @date 2026/3/11 15:30
     * @param dto
     * @return Boolean
     */
    Boolean exportPlatformFlow(AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO dto);
}
