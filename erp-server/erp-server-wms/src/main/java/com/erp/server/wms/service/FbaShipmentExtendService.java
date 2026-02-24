package com.erp.server.wms.service;
import com.erp.model.wms.entity.FbaShipmentExtendEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaShipmentExtendDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * FBA拣货扩展表 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-12-24
 */
public interface FbaShipmentExtendService extends SuperService<FbaShipmentExtendEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-12-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FbaShipmentExtendDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-12-24
    * @param dto
    * @return
    */
    Boolean update(FbaShipmentExtendDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author zdy
    * @date: 2025-12-24
    * @param pagingParamDTO
    * @return PagingVO<FbaShipmentExtendDTO.ListDTO>>
    */
    PagingVO<FbaShipmentExtendDTO.ListDTO> paging(PagingDTO<FbaShipmentExtendDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author zdy
    * @date: 2025-12-24
    * @param dto
    * @return List<FbaShipmentExtendDTO.TabListDTO>>
    */
    List<FbaShipmentExtendDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author zdy
    * @date: 2025-12-24
    * @param id
    * @return
    */
    FbaShipmentExtendDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author zdy
    * @date: 2025-12-24
    * @param dto
    * @param response
    * @return
    */
    void exportList(FbaShipmentExtendDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 根据主表获取扩展信息
     * @param mainIds
     * @return
     */
    List<FbaShipmentExtendEntity> listByMainIds(List<String> mainIds);
}
