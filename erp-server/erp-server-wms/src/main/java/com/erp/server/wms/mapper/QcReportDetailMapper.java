package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.QcReportDetailDTO;
import com.erp.model.wms.entity.QcReportDetailEntity;
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
public interface QcReportDetailMapper extends BaseMapper<QcReportDetailEntity> {

    List<QcReportDetailDTO.ListDTO> getByMainId(@Param("mainId") String mainId);
}
