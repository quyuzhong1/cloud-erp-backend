package com.erp.server.wms.mapper;

import com.erp.model.wms.dto.PackingTaskDTO;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.model.wms.entity.WmsCartonEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 发货单箱子信息明细表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Mapper
public interface WmsCartonMapper extends BaseMapper<WmsCartonEntity> {
    /**
     * 获取任务中最大装箱号
     * @param packingTaskId
     * @return
     */
    Integer getBoxNoByTaskId(@Param("packingTaskId") String packingTaskId);

    /**
     * 根据任务id和权限获取装箱列表
     * @param packedDetailDTO
     * @return
     */
    List<WmsCartonEntity> listByTaskIdsAndPermission(@Param("params") PackingTaskDTO.PackedDetailDTO packedDetailDTO);

    List<WmsCartonDTO.DetailDTO> listByPackingTaskId(@Param("packingTaskId") String packingTaskId);
}
