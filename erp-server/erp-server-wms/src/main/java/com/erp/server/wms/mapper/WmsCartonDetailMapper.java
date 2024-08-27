package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
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

    List<WmsCartonSpecDTO.PackingItemDTO> boxInfoBySourceId(@Param("sourceIdList") List<String> sourceId);

    /**
     * 根据箱子id获取装箱明细
     * @param mainIds
     * @return
     */
    List<WmsCartonDetailDTO.BoxDTO> listCartonDetailByMainIds(@Param("mainIds") List<String> mainIds);

    /**
     * 根据任务id获取箱子明细
     * @param taskIds
     * @return
     */

    List<WmsCartonDetailEntity> listByTaskIds(@Param("taskIds") List<String> taskIds);
}
