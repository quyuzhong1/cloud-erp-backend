package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.WmsCartonSpecEntity;
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
public interface WmsCartonSpecMapper extends BaseMapper<WmsCartonSpecEntity> {

    /**
     * 查询已装箱数量
     * @Author zdy
     * @Date 2023/11/28 19:04
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.FirstMileCartonDTO.PackingQtyDTO>
     **/
    List<WmsCartonSpecDTO.PackingQtyDTO> listPackingQtyByMainId(@Param("mainId") String mainId);
    List<WmsCartonSpecDTO.PackingQtyDTO> listPackingQtyByMainIds(@Param("mainIds") List<String> mainIds);


    /**
     * 根据来源Id查询包装信息
     * @Author Luo_WG
     * @Date 2024/3/21 14:24
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.WmsCartonSpecDTO.PackingQtyDTO>
     **/
    List<WmsCartonSpecDTO.PackDateDTO> listPackDateByMainId(@Param("mainId") String mainId);

    Integer selectBoxSpecNo(@Param("taskId") String taskId);
}
