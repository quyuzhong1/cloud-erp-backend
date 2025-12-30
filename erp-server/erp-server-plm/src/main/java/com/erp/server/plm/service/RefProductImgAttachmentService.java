package com.erp.server.plm.service;
import com.erp.model.plm.entity.RefProductImgAttachmentEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.RefProductImgAttachmentDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 图片分类附件关联表 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-29
 */
public interface RefProductImgAttachmentService extends SuperService<RefProductImgAttachmentEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RefProductImgAttachmentDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @return
    */
    Boolean update(RefProductImgAttachmentDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author wuhaotian
    * @date: 2025-12-29
    * @param pagingParamDTO
    * @return PagingVO<RefProductImgAttachmentDTO.ListDTO>>
    */
    PagingVO<RefProductImgAttachmentDTO.ListDTO> paging(PagingDTO<RefProductImgAttachmentDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @return List<RefProductImgAttachmentDTO.TabListDTO>>
    */
    List<RefProductImgAttachmentDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuhaotian
    * @date: 2025-12-29
    * @param id
    * @return
    */
    RefProductImgAttachmentDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @param response
    * @return
    */
    void exportList(RefProductImgAttachmentDTO.ExportDTO dto, HttpServletResponse response);
}
