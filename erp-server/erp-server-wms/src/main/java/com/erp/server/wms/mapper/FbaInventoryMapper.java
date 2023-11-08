package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * FBI库存 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Mapper
public interface FbaInventoryMapper extends BaseMapper<FbaInventoryEntity> {

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/11/8 15:56
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.FbaDeliveryDTO.ListDTO>
     **/
    IPage<FbaInventoryDTO.ListDTO> paging(Page query, @Param("params") FbaInventoryDTO.PagingParamDTO params);
}
