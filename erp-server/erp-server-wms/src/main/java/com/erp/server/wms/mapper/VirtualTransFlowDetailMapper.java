package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.model.wms.entity.VirtualTransFlowDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 虚拟仓库存流水明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Mapper
public interface VirtualTransFlowDetailMapper extends BaseMapper<VirtualTransFlowDetailEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/12/5 10:57
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<VirtualTransFlowDetailDTO.ListDTO> paging(Page<VirtualTransFlowDetailDTO.SearchParamDTO> page,@Param("params") VirtualTransFlowDetailDTO.SearchParamDTO params);
}
