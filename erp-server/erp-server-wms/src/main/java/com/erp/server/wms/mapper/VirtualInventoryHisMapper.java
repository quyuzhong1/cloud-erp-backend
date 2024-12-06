package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.VirtualInventoryHisDTO;
import com.erp.model.wms.entity.VirtualInventoryHisEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 虚拟仓库存历史信息 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Mapper
public interface VirtualInventoryHisMapper extends BaseMapper<VirtualInventoryHisEntity> {
    /**
     * 根据paramDTO参数查询
     * @author will
     * @date 2024/12/6 11:52
     * @param paramDTO
     * @return List<VirtualInventoryHisEntity>
     */
    List<VirtualInventoryHisEntity> listByParam(@Param("params") VirtualInventoryHisDTO.ParamDTO paramDTO);
}
