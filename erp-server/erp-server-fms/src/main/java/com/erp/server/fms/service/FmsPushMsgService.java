package com.erp.server.fms.service;
import com.erp.model.fms.entity.FmsPushMsgEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.FmsPushMsgDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 本地推送消息表 服务类
 * </p>
 *
 * @author will
 * @since 2025-12-30
 */
public interface FmsPushMsgService extends SuperService<FmsPushMsgEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-12-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FmsPushMsgDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-12-30
    * @param dto
    * @return
    */
    Boolean update(FmsPushMsgDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author will
    * @date: 2025-12-30
    * @param pagingParamDTO
    * @return PagingVO<FmsPushMsgDTO.ListDTO>>
    */
    PagingVO<FmsPushMsgDTO.ListDTO> paging(PagingDTO<FmsPushMsgDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author will
    * @date: 2025-12-30
    * @param dto
    * @return List<FmsPushMsgDTO.TabListDTO>>
    */
    List<FmsPushMsgDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author will
    * @date: 2025-12-30
    * @param id
    * @return
    */
    FmsPushMsgDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author will
    * @date: 2025-12-30
    * @param dto
    * @param response
    * @return
    */
    void exportList(FmsPushMsgDTO.ExportDTO dto, HttpServletResponse response);
}
