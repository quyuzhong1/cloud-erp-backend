package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.CfgAfterPlatformShopEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.CfgAfterPlatformShopDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2026-03-03
 */
public interface CfgAfterPlatformShopService extends SuperService<CfgAfterPlatformShopEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2026-03-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgAfterPlatformShopDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2026-03-03
    * @param dto
    * @return
    */
    Boolean update(CfgAfterPlatformShopDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author wtr
    * @date: 2026-03-03
    * @param pagingParamDTO
    * @return PagingVO<CfgAfterPlatformShopDTO.ListDTO>>
    */
    PagingVO<CfgAfterPlatformShopDTO.ListDTO> paging(PagingDTO<CfgAfterPlatformShopDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wtr
    * @date: 2026-03-03
    * @param dto
    * @return List<CfgAfterPlatformShopDTO.TabListDTO>>
    */
    List<CfgAfterPlatformShopDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wtr
    * @date: 2026-03-03
    * @param id
    * @return
    */
    CfgAfterPlatformShopDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author wtr
    * @date: 2026-03-03
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgAfterPlatformShopDTO.ExportDTO dto, HttpServletResponse response);
}
