package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.PickingListsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 拣货单 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
@Mapper
public interface PickingListsMapper extends BaseMapper<PickingListsEntity> {

    IPage<PickingListsDTO.PagingView> paging(@Param("page") Page<PickingListsDTO.PagingView> page, @Param("params") PickingListsDTO.PagingParam params);

    List<PickingListsDTO.ExportInfoDTO> exportInfo(@Param("params") PickingListsDTO.ExportDTO params);
    Page<PickingListsDTO.ExportInfoDTO> exportInfo(@Param("page") Page<PickingListsDTO.ExportInfoDTO> page, @Param("params") PickingListsDTO.ExportDTO params);

    List<PickingListsDTO.SourceView> listBySourceIds(@Param("sourceIds") List<String> sourceIds);

    /**
     * 根据来源单汇总sku拣货数量 b2b
     * @param sourceIds
     * @return
     */
    List<PickingListsDTO.DetailPickDTO> listB2BDetailBySourceIds(@Param("sourceIds") List<String> sourceIds);

    /**
     * 头程拣货数量
     * @param sourceIds
     * @return
     */
    List<PickingListsDTO.DetailPickDTO> listRequitDetailBySourceIds(@Param("sourceIds") List<String> sourceIds);
}
