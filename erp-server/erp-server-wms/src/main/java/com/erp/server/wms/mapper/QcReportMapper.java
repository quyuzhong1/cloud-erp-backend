package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.QcReportDTO;
import com.erp.model.wms.entity.QcReportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 质检报告 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
@Mapper
public interface QcReportMapper extends BaseMapper<QcReportEntity> {

    List<QcReportDTO.ListDTO> getByQcType(@Param("qcType") String qcType,@Param("approveStatus") String approveStatus);


}
