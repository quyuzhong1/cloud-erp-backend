package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.PickingCartDTO;
import com.erp.model.wms.entity.PickingCartEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 拣货车管理 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
@Mapper
public interface PickingCartMapper extends BaseMapper<PickingCartEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/6/24 9:23
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PickingCartDTO.ListDTO> paging(Page query,@Param("params") PickingCartDTO.PagingParamDTO params);
}
