package com.erp.server.tms.service;
import com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.DeliveryDeclareDetailMidDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 报关明细中间表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-04-27
 */
public interface DeliveryDeclareDetailMidService extends SuperService<DeliveryDeclareDetailMidEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DeliveryDeclareDetailMidDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @return
    */
    Boolean update(DeliveryDeclareDetailMidDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-04-27
    * @param pagingParamDTO
    * @return PagingVO<DeliveryDeclareDetailMidDTO.ListDTO>>
    */
    PagingVO<DeliveryDeclareDetailMidDTO.ListDTO> paging(PagingDTO<DeliveryDeclareDetailMidDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @return List<DeliveryDeclareDetailMidDTO.TabListDTO>>
    */
    List<DeliveryDeclareDetailMidDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-04-27
    * @param id
    * @return
    */
    DeliveryDeclareDetailMidDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @param response
    * @return
    */
    void exportList(DeliveryDeclareDetailMidDTO.ExportDTO dto, HttpServletResponse response);
}
