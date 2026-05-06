package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 头程重量分摊 Mapper 接口
 * </p>
 *
 * @author tmj
 * @since 2024-08-20
 */
@Mapper
public interface FirstMileWeightAllocationMapper extends BaseMapper<FirstMileWeightAllocationEntity> {

    int tabCount(@Param("status") String costAllocationStatus);

    IPage<FirstMileWeightAllocationDTO.ViewDTO> paging(Page<?> query, @Param("params") FirstMileWeightAllocationDTO.PagingParamDTO params);

    List<FirstMileWeightAllocationDTO.ViewDTO> listByParamIds(@Param("ids") List<String> ids);

    List<FirstMileWeightAllocationDTO.ViewDTO> listByParam(@Param("params") FirstMileWeightAllocationDTO.ExportParamDTO dto);
    /**
     * 根据来源id查询重量分摊列表
     * @param sourceIds
     * @param statusList
     * @return
     */
    List<FirstMileWeightAllocationEntity> listBySourceIds(@Param("sourceIds") List<String> sourceIds, @Param("statusList") List<String> statusList);

    FirstMileWeightAllocationDTO.LogisticsBillInfoDTO getLogisticsBillInfo(@Param("logisticsBillId") String logisticsBillId);

    List<FirstMileWeightAllocationDTO.CostAllocationDTO> listCostAllocation(@Param("logisticsBillId") String logisticsBillId);

    /**
     * 统计tab数量
     * @param permissionSql
     * @param statusList
     * @return
     */
    Integer countTabNum(@Param("permissionSql") String permissionSql, @Param("statusList") List<String> statusList);

    /**
     * 查看商品重量
     * @param params
     * @return
     */
    List<FirstMileWeightAllocationDTO.ViewProductWeightDTO> viewProductWeight(@Param("params") FirstMileWeightAllocationDTO.ViewProductWeightParamDTO params);

    /**
     * 游标分页查询可下推分摊的发货单ID
     * @param params 查询参数（含lastId游标、batchSize批大小）
     * @return 发货单ID列表
     * @author jack
     * @date 2026-04-22
     */
    List<String> pageFirstMileDeliveryIds(@Param("params") TmsAsyncTaskRecordDTO.PushParamsDTO params);

    /**
     * 统计可下推分摊的总条数
     * @param params 查询参数
     * @return 总条数
     * @author jack
     * @date 2026-04-22
     */
    int countFirstMileDeliveryIds(@Param("params") TmsAsyncTaskRecordDTO.PushParamsDTO params);
}
