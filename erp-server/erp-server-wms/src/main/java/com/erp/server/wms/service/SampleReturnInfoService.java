package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleReturnInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleReturnInfoDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 样品归还单主表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
public interface SampleReturnInfoService extends SuperService<SampleReturnInfoEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleReturnInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    Boolean update(SampleReturnInfoDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2025-08-20
    * @param pagingParamDTO
    * @return PagingVO<SampleReturnInfoDTO.ListDTO>>
    */
    PagingVO<SampleReturnInfoDTO.ListDTO> paging(PagingDTO<SampleReturnInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return List<SampleReturnInfoDTO.TabListDTO>>
    */
    List<SampleReturnInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    SampleReturnInfoDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SampleReturnInfoDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    void updateAndSubmit(SampleReturnInfoDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author jack
     * @date: 2025-08-20
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(SampleReturnInfoDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SampleReturnInfoEntity entity);

}
