package com.erp.server.dmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskHistoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 中台同步任务表 Mapper 接口
 * </p>
 *
 * @author Cloud
 * @since 2023-09-06
 */
@Mapper
public interface DmpPushTaskHistoryMapper extends BaseMapper<DmpPushTaskHistoryEntity> {

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/10/13 14:19
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<DmpPushTaskDTO.ListDTO> paging(Page query, @Param("params") DmpPushTaskDTO.ParamDTO params);
    /**
     * @description:获取已归档数量
     */
    DmpPushTaskDTO.TabListDTO getStatusCount( @Param("permissionSql")String permissionSql);
    /**
     * @description: 导出查询
     * @author Will
     * @date: 2023/10/13 15:36
     * @param dto
     * @return List<ListDTO>
     */
    List<DmpPushTaskDTO.ListDTO> listExportExcel(@Param("params") DmpPushTaskDTO.ParamDTO dto);
    Page<DmpPushTaskDTO.ListDTO> listExportExcel(@Param("page")Page<DmpPushTaskDTO.ListDTO> page, @Param("params") DmpPushTaskDTO.ParamDTO dto);
     /* 根据条件查询数据
     *
     * @param params
     * @return
     */
    DmpPushTaskEntity getEntityByCondition(@Param("params") DmpPushTaskEntity params);
}
