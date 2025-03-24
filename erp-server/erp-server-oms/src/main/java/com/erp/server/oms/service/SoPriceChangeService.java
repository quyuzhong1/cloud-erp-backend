package com.erp.server.oms.service;
import com.erp.model.oms.entity.SoPriceChangeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoPriceChangeDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售价变更表 服务类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
public interface SoPriceChangeService extends SuperService<SoPriceChangeEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-03-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoPriceChangeDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-03-24
    * @param dto
    * @return
    */
    Boolean update(SoPriceChangeDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author will
    * @date: 2025-03-24
    * @param pagingParamDTO
    * @return PagingVO<SoPriceChangeDTO.ListDTO>>
    */
    PagingVO<SoPriceChangeDTO.PagingViewDTO> paging(PagingDTO<SoPriceChangeDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author will
    * @date: 2025-03-24
    * @param dto
    * @return List<SoPriceChangeDTO.TabListDTO>>
    */
    List<SoPriceChangeDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author will
    * @date: 2025-03-24
    * @param id
    * @return
    */
    SoPriceChangeDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author will
    * @date: 2025-03-24
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SoPriceChangeDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author will
    * @date: 2025-03-24
    * @param dto
    * @return
    */
    void updateAndSubmit(SoPriceChangeDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author will
     * @date: 2025-03-24
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author will
    * @date: 2025-03-24
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author will
    * @date: 2025-03-24
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author will
    * @date: 2025-03-24
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author will
    * @date: 2025-03-24
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author will
    * @date: 2025-03-24
    * @param dto
    * @param response
    * @return
    */
    void exportList(SoPriceChangeDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SoPriceChangeEntity entity);

}
