package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.erp.model.wms.dto.FileManagementDTO;
import com.erp.model.wms.entity.FileManagementEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 文件管理 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Mapper
public interface FileManagementMapper extends BaseMapper<FileManagementEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<FileManagementDTO.ListDTO> paging(Page query, @Param("params") FileManagementDTO.PagingParamDTO params);

    /**
     *  根据sku和类型统计数量
     * @param skuIds
     * @param fileType
     * @param id
     * @return
     */
    Integer countBySkuAndFileType(@Param("skuIds") List<String> skuIds, @Param("fileType")String fileType, @Param("id") String id);


    FileManagementDTO.AttachDTO getCategoryGeneralStandardFileUrl(@Param("skuId") String skuId);
}
