package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.model.wms.entity.WmsCartonDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 发货单箱子信息表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Mapper
public interface WmsCartonDetailMapper extends BaseMapper<WmsCartonDetailEntity> {

    List<WmsCartonDTO.PackingItemDTO> boxInfoBySourceId(@Param("sourceId") String sourceId);
}
