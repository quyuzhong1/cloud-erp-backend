package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.AdsErpInventoryDiffDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * <p>
 * 平台库存差异 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
 */
public interface AdsErpInventoryDiffService extends SuperService<AdsErpInventoryDiffEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AdsErpInventoryDiffDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return
    */
    Boolean update(AdsErpInventoryDiffDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author Jim
    * @date: 2025-11-13
    * @param pagingParamDTO
    * @return PagingVO<AdsErpInventoryDiffDTO.ListDTO>>
    */
    PagingVO<AdsErpInventoryDiffDTO.ListDTO> paging(PagingDTO<AdsErpInventoryDiffDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return AdsErpInventoryDiffDTO.StatisticsDTO
    */
    AdsErpInventoryDiffDTO.StatisticsDTO statistics(PagingDTO<AdsErpInventoryDiffDTO.PagingParamDTO> dto);

    /**
    * 详情
    * @author Jim
    * @date: 2025-11-13
    * @param id
    * @return
    */
    AdsErpInventoryDiffDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @param response
    * @return
    */
    Boolean exportList(AdsErpInventoryDiffDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 更新备注
     * @param id
     * @param remark
     * @return
     */
    BatchResultDTO updateRemark(String id, String remark);
}
