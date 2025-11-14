package com.erp.server.dmp.service;
import com.erp.model.dmp.dto.AdsErpInventoryDiffDTO;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffKingdeeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.AdsErpInventoryDiffKingdeeDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * <p>
 * 金蝶库存差异 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
 */
public interface AdsErpInventoryDiffKingdeeService extends SuperService<AdsErpInventoryDiffKingdeeEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AdsErpInventoryDiffKingdeeDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return
    */
    Boolean update(AdsErpInventoryDiffKingdeeDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author Jim
    * @date: 2025-11-13
    * @param pagingParamDTO
    * @return PagingVO<AdsErpInventoryDiffKingdeeDTO.ListDTO>>
    */
    PagingVO<AdsErpInventoryDiffKingdeeDTO.ListDTO> paging(PagingDTO<AdsErpInventoryDiffKingdeeDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return List<AdsErpInventoryDiffKingdeeDTO.TabListDTO>>
    */
    AdsErpInventoryDiffKingdeeDTO.StatisticsDTO statistics(PagingDTO<AdsErpInventoryDiffKingdeeDTO.PagingParamDTO> dto);

    /**
    * 详情
    * @author Jim
    * @date: 2025-11-13
    * @param id
    * @return
    */
    AdsErpInventoryDiffKingdeeDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @param response
    * @return
    */
    void exportList(AdsErpInventoryDiffKingdeeDTO.ExportDTO dto, HttpServletResponse response);


    /**
     * 更新备注
     * @author Jim
     * @date: 2025-11-13
     * @return
     */
    BatchResultDTO updateRemark(String id,String remark);
}
