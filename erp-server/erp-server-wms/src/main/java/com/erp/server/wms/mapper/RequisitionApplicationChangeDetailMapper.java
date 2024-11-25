package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.erp.model.wms.entity.RequisitionApplicationChangeDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 要货申请变更明细 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
 */
@Mapper
public interface RequisitionApplicationChangeDetailMapper extends BaseMapper<RequisitionApplicationChangeDetailEntity> {

    List<RequisitionApplicationChangeDTO.ExistDTO> checkExist(@Param("sourceDetailIds") List<String> sourceDetailIds,@Param("businessDetailIds") List<String> businessDetailIds);
}
