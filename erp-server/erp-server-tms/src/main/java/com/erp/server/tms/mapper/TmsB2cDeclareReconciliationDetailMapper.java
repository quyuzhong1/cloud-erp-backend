package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * b2c报关对账单明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
@Mapper
public interface TmsB2cDeclareReconciliationDetailMapper extends BaseMapper<TmsB2cDeclareReconciliationDetailEntity> {


    /**
     * 导出Excel查询
     * @param params
     * @return
     */
    List<TmsB2cDeclareReconciliationDetailDTO.ListDTO> listExport(@Param("params") TmsB2cDeclareReconciliationDetailDTO.ExportDTO params);
}
