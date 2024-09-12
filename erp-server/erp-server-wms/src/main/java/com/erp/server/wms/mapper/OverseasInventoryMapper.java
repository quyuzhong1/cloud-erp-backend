package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.OverseasInventoryDTO;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 海外仓库存 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Mapper
public interface OverseasInventoryMapper extends BaseMapper<OverseasInventoryEntity> {

    /**
     * 分页列表
     * @author Jim
     * @date: 2023-11-20
     */
    IPage<OverseasInventoryDTO.ListDTO> paging(Page<?> query, @Param("params") OverseasInventoryDTO.PagingParamDTO params);

    /**
     * 列表合计
     * @author Jim
     * @date: 2023-11-20
     */
    OverseasInventoryDTO.ListTotalDTO queryParamsTotal(@Param("params") OverseasInventoryDTO.PagingParamDTO params);

    /**
     * 根据筛选条件导出列表
     * @author Jim
     * @date: 2023-11-21
     */
    List<OverseasInventoryDTO.ListDTO> listByParams(@Param("params") OverseasInventoryDTO.PagingParamDTO params);
    Page<OverseasInventoryDTO.ListDTO> listByParams(@Param("page") Page<OverseasInventoryDTO.ListDTO> page, @Param("params") OverseasInventoryDTO.PagingParamDTO params);
}
