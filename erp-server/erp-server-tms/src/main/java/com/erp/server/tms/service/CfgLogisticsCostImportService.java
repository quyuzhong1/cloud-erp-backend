package com.erp.server.tms.service;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.CfgLogisticsCostImportDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 费用项配置 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
public interface CfgLogisticsCostImportService extends SuperService<CfgLogisticsCostImportEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgLogisticsCostImportDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return
    */
    Boolean update(CfgLogisticsCostImportDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-01-20
    * @param pagingParamDTO
    * @return PagingVO<CfgLogisticsCostImportDTO.ListDTO>>
    */
    PagingVO<CfgLogisticsCostImportDTO.ListDTO> paging(PagingDTO<CfgLogisticsCostImportDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return List<CfgLogisticsCostImportDTO.TabListDTO>>
    */
    List<CfgLogisticsCostImportDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-01-20
    * @param id
    * @return
    */
    CfgLogisticsCostImportDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgLogisticsCostImportDTO.ExportDTO dto, HttpServletResponse response);
}
