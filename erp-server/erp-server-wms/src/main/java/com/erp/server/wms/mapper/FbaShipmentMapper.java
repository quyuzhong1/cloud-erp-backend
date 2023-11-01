package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * FBI货件表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Mapper
public interface FbaShipmentMapper extends BaseMapper<FbaShipmentEntity> {

    /**
     * 列表查詢
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.FbaShipmentDTO.ListDTO>
     **/
    IPage<FbaShipmentDTO.ListDTO> paging(Page query, @Param("params") FbaShipmentDTO.PagingParamDTO params);

    /**
     * 下推发货单列表查询
     * @Author Luo_WG
     * @Date 2023/11/1 9:24
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.GenerateDeliverView>
     **/
    List<FbaShipmentDTO.GenerateDeliverView> generateDeliverView(@Param("params") BaseIdsDTO.IdsDTO ids);
}
