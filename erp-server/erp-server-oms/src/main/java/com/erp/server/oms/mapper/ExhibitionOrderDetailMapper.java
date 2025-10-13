package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.ExhibitionOrderDetailDTO;
import com.erp.model.oms.entity.ExhibitionOrderDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 展会订单详情 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-08-29
 */
@Mapper
public interface ExhibitionOrderDetailMapper extends BaseMapper<ExhibitionOrderDetailEntity> {

    List<ExhibitionOrderDetailDTO.SkuQtyDetailDTO> listBySourceDetailIds(@Param("sourceDetailIdList") List<String> sourceDetailIdList);
}
