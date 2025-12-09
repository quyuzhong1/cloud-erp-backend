package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.KolSubB2cApplicationDTO;
import com.erp.model.oms.entity.KolSubB2cApplicationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * B2C寄样申请单拆分单 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
@Mapper
public interface KolSubB2cApplicationMapper extends BaseMapper<KolSubB2cApplicationEntity> {

    List<KolSubB2cApplicationDTO.ListDTO> listSubBySourceId(@Param("sourceId") String sourceId);
}
