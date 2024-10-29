package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 物流单明细表 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Mapper
public interface LogisticsBillDetailMapper extends BaseMapper<LogisticsBillDetailEntity> {

    IPage<LogisticsBillDetailEntity> getTrackPage(@Param("page") Page<LogisticsBillDetailEntity> page, @Param("query") LogisticsBillDetailQueryDTO query);
    IPage<LogisticsTrackDTO.UpdateTrackDTO> getTrackDtoPage(@Param("page") Page<LogisticsTrackDTO.UpdateTrackDTO> page, @Param("query") LogisticsBillDetailQueryDTO query);
    List<LogisticsTrackDTO.UpdateTrackDTO> listTrackDto(@Param("query") LogisticsBillDetailQueryDTO query);
    /**
     * @description: 根据平台订单号和物流跟踪单号查询
     * @author Will
     * @date: 2024/5/11 11:51
     * @param platformCodeList
     * @param trackNoList
     * @return List<LogisticsBillDetailEntity>
     */
    List<LogisticsBillDetailEntity> listByPlatformCodeAndTrackNo(@Param("platformCodeList") List<String> platformCodeList,@Param("trackNoList") List<String> trackNoList);

    /**
     * 用于更新使用运单号做物流轨迹查询数据更新
     * @param trackNoList
     * @param aTrue
     * @param trackStatus
     * @param signTime
     */
    void updateTransportNo(@Param("trackNoList") List<String> trackNoList, @Param("aTrue") Boolean aTrue, @Param("trackStatus") String trackStatus, @Param("signTime") LocalDateTime signTime, @Param("trackTime") LocalDateTime trackTime);

    /**
     * 根据运单号更新注册状态
     * @param transportNo
     * @param platformOrderNo
     * @param status
     */
    void updateRegisticsStatus(@Param("transportNo") String transportNo, @Param("platformOrderNo") String platformOrderNo, @Param("status") int status);
}
