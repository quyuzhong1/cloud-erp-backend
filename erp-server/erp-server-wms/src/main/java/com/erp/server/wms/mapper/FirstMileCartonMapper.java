package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.FirstMileCartonDTO;
import com.erp.model.wms.entity.FirstMileCartonEntity;
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
public interface FirstMileCartonMapper extends BaseMapper<FirstMileCartonEntity> {

    /**
     * 查询已装箱数量
     * @Author Luo_WG
     * @Date 2023/11/28 19:04
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.FirstMileCartonDTO.PackingQtyDTO>
     **/
    List<FirstMileCartonDTO.PackingQtyDTO> listPackingQtyByMainId(@Param("mainId") String mainId);
}
