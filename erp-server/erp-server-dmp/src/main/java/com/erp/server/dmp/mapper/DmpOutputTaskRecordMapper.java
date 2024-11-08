package com.erp.server.dmp.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.DmpSyncTaskDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 推送任务记录 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpOutputTaskRecordMapper extends BaseMapper<DmpOutputTaskRecordEntity> {


    /**
     * 获取 tab列表
     * @Author Luo_WG
     * @Date 2024/9/3 15:05
     * @param permissionSql
     * @return java.util.List<com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.TabListDTO>
     **/
    List<DmpOutputTaskRecordDTO.TabListDTO> listStatusCount(@Param("permissionSql")String permissionSql);

    /**
     * 添加进黑名单的数量
     * @Author Luo_WG
     * @Date 2024/9/11 17:22
     * @return java.util.List<com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.TabListDTO>
     **/
    Integer listBlackCount(@Param("permissionSql")String permissionSql);

    /**
     * 推送任务列表分页查询
     * @Author Luo_WG
     * @Date 2024/9/3 14:35
     * @param params
     * @return com.common.business.vo.PagingVO<com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.PagingDTO>
     **/
    IPage<DmpOutputTaskRecordDTO.PagingDTO> paging(Page query, @Param("params") DmpOutputTaskRecordDTO.PagingParamDTO params);
    
    IPage<DmpOutputTaskRecordDTO.PagingDTO> blackPaging(Page query, @Param("params") DmpOutputTaskRecordDTO.PagingParamDTO params);

    /**
     * 导出数据查询
     * @Author Luo_WG
     * @Date 2024/9/5 17:40
     * @param objectPage
     * @param params
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.PagingDTO>
     **/
    Page<DmpOutputTaskRecordDTO.PagingDTO> listExportExcel(Page<Object> objectPage, @Param("params") DmpOutputTaskRecordDTO.ExpotParamDTO params);
    /**
     * 查询新中台推送任务记录
     * @author will
     * @date 2024/10/22 9:13
     * @param params
     * @return SyncInfoDTO
     */
    DmpPushTaskDTO.SyncInfoDTO getErrorData(@Param("params") DmpSyncTaskDTO.OneDTO params);
}
