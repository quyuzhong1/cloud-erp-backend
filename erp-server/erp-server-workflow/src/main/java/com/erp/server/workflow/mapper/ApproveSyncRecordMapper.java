package com.erp.server.workflow.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.workflow.dto.ApproveSyncRecordDTO;
import com.erp.model.workflow.entity.ApproveSyncRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * ERP审批同步-通知配置 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Mapper
public interface ApproveSyncRecordMapper extends BaseMapper<ApproveSyncRecordEntity> {

    List<ApproveSyncRecordDTO.TabListDTO> tabList(@Param("params") ApproveSyncRecordDTO.PagingParamDTO searchParam);

    IPage<ApproveSyncRecordDTO.ListDTO> paging(Page query, @Param("params")  ApproveSyncRecordDTO.PagingParamDTO params);

    int insertBatch(@Param("list") List<ApproveSyncRecordEntity> list);
}
