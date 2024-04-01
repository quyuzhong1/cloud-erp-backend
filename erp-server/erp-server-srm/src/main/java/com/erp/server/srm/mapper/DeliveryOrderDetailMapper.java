package com.erp.server.srm.mapper;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 送货单明细 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
@Mapper
public interface DeliveryOrderDetailMapper extends BaseMapper<DeliveryOrderDetailEntity> {
    /**
     * 获取发货明细及状态
     * @param detailIds
     * @return
     */
    List<DeliveryOrderDetailDTO.ListDTO> listDetailDTOByDetailSourceIds(@Param("detailIds") List<String> detailIds);
}
