package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * B2C销售订单异常表 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-12-20
 */
@Mapper
public interface SoB2cErrorMapper extends BaseMapper<SoB2cErrorEntity> {

    
    /**
     * 删除异常订单
     * @description
     * @param dto
     * @author Lambda
     * @return 
     * @create 2023-12-21 15:00
     */
    Boolean deleteB2cError(@Param("params") SoB2cErrorDTO.DeleteDTO dto);
}
