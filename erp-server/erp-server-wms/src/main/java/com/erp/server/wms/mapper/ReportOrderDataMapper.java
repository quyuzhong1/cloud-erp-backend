package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.ReportOrderDataEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 订单报表信息 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-09-24
 */
@Mapper
public interface ReportOrderDataMapper extends BaseMapper<ReportOrderDataEntity> {
    /**
     * 查询报表数据
     * @author will
     * @date 2024/9/27 9:17
     * @return List<ReportOrderDataEntity>
     */
    List<ReportOrderDataEntity> listReportOrderData();
}
