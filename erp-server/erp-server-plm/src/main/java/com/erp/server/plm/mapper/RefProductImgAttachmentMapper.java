package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.RefProductImgAttachmentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.RefProductImgAttachmentDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 图片分类附件关联表 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-29
 */
@Mapper
public interface RefProductImgAttachmentMapper extends BaseMapper<RefProductImgAttachmentEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<RefProductImgAttachmentDTO.ListDTO> paging(Page query, @Param("params") RefProductImgAttachmentDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") RefProductImgAttachmentDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<RefProductImgAttachmentDTO.ListDTO> listExport(@Param("params") RefProductImgAttachmentDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<RefProductImgAttachmentDTO.TabListDTO> tabList(@Param("params") RefProductImgAttachmentDTO.PagingParamDTO searchParam);
}
