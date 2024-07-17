package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.PickingCartTypeDTO;
import com.erp.model.wms.entity.PickingCartTypeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 拣货车类型 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
@Mapper
public interface PickingCartTypeMapper extends BaseMapper<PickingCartTypeEntity> {
    /**
     * 拣货车类型查询
     * @author will
     * @date 2024/6/20 18:24
     * @param selectDTO
     * @return List<ListDTO>
     */
    List<PickingCartTypeDTO.ListDTO> select(@Param("selectDTO") PickingCartTypeDTO.SelectDTO selectDTO);
}
