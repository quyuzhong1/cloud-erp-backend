package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.FirstMileProcessingDTO;
import com.erp.model.wms.entity.FirstMileProcessingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 头程虚拟仓订单跟踪 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Mapper
public interface FirstMileProcessingMapper extends BaseMapper<FirstMileProcessingEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:48
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<FirstMileProcessingDTO.ListDTO> paging(Page<FirstMileProcessingDTO.PagingParamDTO> page,@Param("params") FirstMileProcessingDTO.PagingParamDTO params);
}
