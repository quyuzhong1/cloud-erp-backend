package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 装箱任务表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
 */
@Mapper
public interface PackingTaskMapper extends BaseMapper<PackingTaskEntity> {
    /**
     * 根据类型进行汇总
     * @param permissionSql
     * @return
     */
    List<PackingTaskDTO.TypeCountDTO> listTabCount(@Param("permissionSql") String permissionSql);

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<PackingTaskDTO.PagingViewDTO> paging(@Param("query") Page<PackingTaskDTO.PagingViewDTO> query, @Param("params") PackingTaskDTO.PagingParamDTO params);
    List<PackingTaskDTO.PagingViewDTO> pagingList(@Param("params") PackingTaskDTO.PagingParamDTO params);
    Page<PackingTaskDTO.PagingViewDTO> pagingList(@Param("page") Page<PackingTaskDTO.PagingViewDTO> page, @Param("params") PackingTaskDTO.PagingParamDTO params);

    /**
     * 根据任务id汇总状态
     *
     * @param taskIds
     * @param sourceCodes
     * @return
     */
    List<PackingTaskDTO.StatusDTO> selectB2BPackingStatusByIds(@Param("taskIds") List<String> taskIds, @Param("sourceCodes") List<String> sourceCodes);
    List<PackingTaskDTO.ProductNum> selectProductNumByIds(@Param("taskIds") List<String> taskIds);

    /**
     * 根据装箱任务进行sku分组
     * @param mainId
     * @return
     */
    List<WmsCartonSpecDTO.GroupSkuDTO> listGroupSkuByMainId(@Param("mainId") String mainId);

    /**
     * 批量汇总
     * @param mainIds
     * @return
     */
    List<WmsCartonSpecDTO.GroupSkuDTO> listGroupSkuByMainIds(@Param("mainIds") List<String> mainIds);

    /**
     * 获取装箱清单
     * @param params
     * @return
     */
    List<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetail(@Param("params") PackingTaskDTO.PackedDetailDTO params);
    List<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetailBySkuId(@Param("taskIds") List<String> taskIds, @Param("permissionSql") String permissionSql);
    Page<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetailBySkuId(@Param("params") Page<WmsCartonDetailDTO.ListPackingDetailDTO> page, @Param("taskIds") List<String> taskIds, @Param("permissionSql") String permissionSql);

    /**
     * 从历史装箱数据补充装箱任务和装箱详情
     * @return
     */
    List<PackingTaskDetailDTO.HistoryCartonDTO> selectHistoryCartonList();

    IPage<PackingTaskDTO.PackingTreeDTO> pagingSelect(@Param("query") Page<PackingTaskDTO.PackingTreeDTO> query, @Param("params") PackingTaskDTO.SearchSourceCodeDTO dto);

    List<WmsCartonSpecDTO.SpecDTO> getCartonSpecByTaskId(@Param("taskId") String taskId);

    /**
     * 根据任务id汇总状态
     *
     * @param taskIds
     * @param sourceCodes
     * @return
     */
    List<PackingTaskDTO.StatusDTO> selectRequisitionPackingStatusByIds(@Param("taskIds") List<String> taskIds, @Param("sourceCodes") List<String> sourceCodes);
}
