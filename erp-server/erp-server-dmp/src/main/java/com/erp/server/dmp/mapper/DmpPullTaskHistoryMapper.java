package com.erp.server.dmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.entity.DmpPullTaskHistoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * <p>
 * 中台同步任务表 Mapper 接口
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Repository
@Mapper
public interface DmpPullTaskHistoryMapper extends BaseMapper<DmpPullTaskHistoryEntity> {

    /**
     * @description:查询已归档数量
     */
    DmpPullTaskDTO.TabListDTO getStatusCount(@Param("permissionSql")String permissionSql);
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/10/17 14:54
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<DmpPullTaskDTO.ListDTO> paging(Page query, @Param("params") DmpPullTaskDTO.ParamDTO params);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/10/17 14:55
     * @param dto
     * @return List<ListDTO>
     */
    List<DmpPullTaskDTO.ListDTO> listExportExcel(@Param("params") DmpPullTaskDTO.ParamDTO dto);
    Page<DmpPullTaskDTO.ListDTO> listExportExcel(@Param("page")Page<DmpPullTaskDTO.ListDTO> page, @Param("params") DmpPullTaskDTO.ParamDTO dto);

}
