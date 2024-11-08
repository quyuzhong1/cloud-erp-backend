package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 要货申请单明细表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Mapper
public interface RequisitionApplicationDetailMapper extends BaseMapper<RequisitionApplicationDetailEntity> {
    /**
     * 查询要货申请数据
     * @author will
     * @date 2024/9/26 17:41
     * @return List<ViewDTO>
     */
    List<ReportOrderDataDTO.ViewDTO> listAllVirtualRequisitionApplicationDetail();
}
