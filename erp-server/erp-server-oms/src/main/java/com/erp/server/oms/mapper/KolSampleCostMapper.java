package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.KolSampleCostDTO;
import com.erp.model.oms.entity.KolSampleCostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 寄样费用表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
@Mapper
public interface KolSampleCostMapper extends BaseMapper<KolSampleCostEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2025/12/8 10:20
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<KolSampleCostDTO.ListDTO> paging(Page<Object> query,@Param("params") KolSampleCostDTO.PagingParamDTO params);
    /**
     * 查询月份内存在的寄样费用
     * @author will
     * @date 2025/12/9 14:54
     * @param startTime
     * @param endTIme
     * @return List<KolSampleCostEntity>
     */
    List<KolSampleCostEntity> listByTime(@Param("startTime") LocalDateTime startTime,@Param("endTIme") LocalDateTime endTIme);
    /**
     * 根据销售订单明细id集合查询寄样费用
     * @author will
     * @date 2025/12/9 16:06
     * @param soDetailIdList
     * @return List<KolSampleCostEntity>
     */
    List<KolSampleCostEntity> listKolSampleCostBySoDetailIdList(@Param("soDetailIdList") List<String> soDetailIdList);
}
