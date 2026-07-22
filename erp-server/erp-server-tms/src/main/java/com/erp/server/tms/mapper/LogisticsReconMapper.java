package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.entity.LogisticsReconEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 物流商对账单（主表） Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Mapper
public interface LogisticsReconMapper extends BaseMapper<LogisticsReconEntity> {

    /**
     * 物流商对账单分页列表查询
     * @author Will
     * @date: 2026/05/29
     * @param query
     * @param params
     * @return IPage<LogisticsReconDTO.ListDTO>
     */
    IPage<LogisticsReconDTO.ListDTO> paging(Page<LogisticsReconDTO.ListDTO> query,
                                            @Param("params") LogisticsReconDTO.PagingParamDTO params);

    /**
     * 物流商对账单 TAB 状态统计（按 check_status[importing/pending/confirmed] 分组聚合）
     * @author Will
     * @date: 2026/05/29
     * @param params
     * @return List<LogisticsReconDTO.TabListDTO>
     */
    List<LogisticsReconDTO.TabListDTO> tabList(@Param("params") LogisticsReconDTO.PagingParamDTO params);

    /**
     * 单条 SQL 聚合并回刷主表分页冗余，避免先读聚合结果再覆盖主表产生旧快照覆盖。
     */
    int refreshPagingStats(@Param("mainId") String mainId,
                           @Param("updateUserId") String updateUserId,
                           @Param("updateUserName") String updateUserName);

    /**
     * 原子维护主表匹配冗余统计（匹配数/成功金额增量，失败金额按当前状态重算）
     */
    int applyMatchStatsDelta(@Param("mainId") String mainId,
                             @Param("matchCountDelta") int matchCountDelta,
                             @Param("matchSuccessAmountDelta") BigDecimal matchSuccessAmountDelta,
                             @Param("updateUserId") String updateUserId,
                             @Param("updateUserName") String updateUserName);

}
