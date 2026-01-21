package com.erp.server.tms.service;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 费用项配置字段配置 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
public interface CfgLogisticsCostImportDetailService extends SuperService<CfgLogisticsCostImportDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgLogisticsCostImportDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return
    */
    Boolean update(CfgLogisticsCostImportDetailDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-01-20
    * @param pagingParamDTO
    * @return PagingVO<CfgLogisticsCostImportDetailDTO.ListDTO>>
    */
    PagingVO<CfgLogisticsCostImportDetailDTO.ListDTO> paging(PagingDTO<CfgLogisticsCostImportDetailDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return List<CfgLogisticsCostImportDetailDTO.TabListDTO>>
    */
    List<CfgLogisticsCostImportDetailDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-01-20
    * @param id
    * @return
    */
    CfgLogisticsCostImportDetailDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgLogisticsCostImportDetailDTO.ExportDTO dto, HttpServletResponse response);
    /**
     * 根据主表id列表查询明细
     * @author will
     * @date 2026/1/21 15:03
     * @param mainIdList
     * @return List<CfgLogisticsCostImportDetailEntity>
     */
    List<CfgLogisticsCostImportDetailEntity> listByMainIdList(List<String> mainIdList);
}
