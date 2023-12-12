package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.FirstMileCartonDTO;
import com.erp.model.wms.entity.FirstMileCartonDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

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
public interface FirstMileCartonDetailMapper extends BaseMapper<FirstMileCartonDetailEntity> {

    List<FirstMileCartonDTO.PackingItemDTO> boxInfoByMainId(String mainId);
}
