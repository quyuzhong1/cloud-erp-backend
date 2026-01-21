package com.erp.server.tms.service;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.CfgLogisticsCostImportFieldDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 费用项配置字段基础表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
public interface CfgLogisticsCostImportFieldService extends SuperService<CfgLogisticsCostImportFieldEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgLogisticsCostImportFieldDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return
    */
    Boolean update(CfgLogisticsCostImportFieldDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-01-20
    * @param pagingParamDTO
    * @return PagingVO<CfgLogisticsCostImportFieldDTO.ListDTO>>
    */
    PagingVO<CfgLogisticsCostImportFieldDTO.ListDTO> paging(PagingDTO<CfgLogisticsCostImportFieldDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return List<CfgLogisticsCostImportFieldDTO.TabListDTO>>
    */
    List<CfgLogisticsCostImportFieldDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-01-20
    * @param id
    * @return
    */
    CfgLogisticsCostImportFieldDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgLogisticsCostImportFieldDTO.ExportDTO dto, HttpServletResponse response);
}
