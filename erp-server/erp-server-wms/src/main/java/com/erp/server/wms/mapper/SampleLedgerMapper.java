package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.common.business.dto.base.PermissionsDTO;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * 样品台账统计 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Mapper
public interface SampleLedgerMapper extends BaseMapper<SampleLedgerEntity> {

    /**
     * 分页查询
     * @param query 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<SampleLedgerDTO.ListDTO> paging(Page query, @Param("params") SampleLedgerDTO.PagingParamDTO params);

    /**
     * 分页导出Excel查询
     * @param query 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<SampleLedgerDTO.ListDTO> listExport(Page query, @Param("params") SampleLedgerDTO.ExportDTO params);

    /**
     * 获取状态统计
     * @param permissionSql 权限SQL
     * @return 状态统计列表
     */
    List<SampleLedgerDTO.TabListDTO> getAllStatusCounts(@Param("permissionSql") String permissionSql);

    /**
     * 获取状态统计（支持数量为0不显示）
     * @param permissionSql 权限SQL
     * @param hideZeroQty 是否隐藏数量为0的记录
     * @return 状态统计列表
     */
    List<SampleLedgerDTO.TabListDTO> getAllStatusCounts(@Param("permissionSql") String permissionSql, @Param("hideZeroQty") Boolean hideZeroQty);

    /**
     * 根据用户ID查询台账列表
     * @param params
     * @return
     */
    List<SampleLedgerDTO.SkuAvailableQtyDTO> listSkuAvailableQtyByUserId(@Param("params") SampleLedgerDTO.SearchDTO params);


    IPage<SampleLedgerDTO.SkuAvailableQtyDTO> listSku(Page query, @Param("params") SampleLedgerDTO.SearchDTO params);
}
