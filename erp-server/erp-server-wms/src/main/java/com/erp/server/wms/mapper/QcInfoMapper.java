package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.entity.QcInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Mapper
public interface QcInfoMapper extends BaseMapper<QcInfoEntity> {

    List<QcInfoDTO.QcQtyDTO> getByPurOrderIds(@Param("purOrderIds") List<String> purOrderIds);
}
