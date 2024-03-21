package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.model.wms.entity.WmsCartonEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 发货单箱规信息 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Mapper
public interface FirstMileCartonMapper extends BaseMapper<WmsCartonEntity> {

    /**
     * 查询已装箱数量
     * @Author Luo_WG
     * @Date 2023/11/28 19:04
     * @param sourceId
     * @return java.util.List<com.erp.model.wms.dto.FirstMileCartonDTO.PackingQtyDTO>
     **/
    List<WmsCartonDTO.PackingQtyDTO> listPackingQtyByMainId(@Param("sourceId") String sourceId, @Param("boxSpecNo") Integer boxSpecNo);


    /**
     * 根据来源Id查询包装信息
     * @Author Luo_WG
     * @Date 2024/3/21 14:24
     * @param sourceId
     * @return java.util.List<com.erp.model.wms.dto.WmsCartonDTO.PackingQtyDTO>
     **/
    List<WmsCartonDTO.PackDateDTO> listPackDateBySourceId(@Param("sourceId") String sourceId);

}
