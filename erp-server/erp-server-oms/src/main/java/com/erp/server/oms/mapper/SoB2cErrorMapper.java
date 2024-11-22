package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


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

    /**
     * 删除异常订单
     * @description
     * @author Lambda
     * @return
     * @create 2023-12-21 15:00
     */
    void deleteByCodeAndType(@Param("code") String soCode,@Param("type") String type);

    /**
     * 批量删除异常订单
     * @param batchDeleteDTO
     * @return
     */
    Boolean batchDeleteB2cError(@Param("params")SoB2cErrorDTO.BatchDeleteDTO batchDeleteDTO);

    /**
     * 删除异常订单明细
     */
    Boolean deleteB2cErrorByDetailId(@Param("params") SoB2cErrorDTO.DeleteDetailDTO dto);
    /**
     * @description: 根据明细id删除
     * @author Will
     * @date: 2024/4/30 17:13
     * @param mainIds
     * @return Boolean
     */
    Boolean deleteByMainIds(@Param("mainIds") List<String> mainIds);

    List<SoB2cErrorDTO.TypeCountDTO> getTypeCountDTO();
}
