package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.entity.SoB2bProcessingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * B2B虚拟仓订单跟踪 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Mapper
public interface SoB2bProcessingMapper extends BaseMapper<SoB2bProcessingEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:25
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<SoB2bProcessingDTO.ListDTO> paging(Page<SoB2bProcessingDTO.PagingParamDTO> page,@Param("params") SoB2bProcessingDTO.PagingParamDTO params);
}
