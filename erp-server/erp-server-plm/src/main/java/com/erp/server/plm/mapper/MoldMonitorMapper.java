package com.erp.server.plm.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.MoldMonitorDTO;
import com.erp.model.plm.entity.MoldMonitorEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * 模具监控 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-10-22
 */
@Mapper
public interface MoldMonitorMapper extends BaseMapper<MoldMonitorEntity> {

    List<MoldMonitorDTO.TabListDTO> tabList(@Param("params") MoldMonitorDTO.PagingParamDTO params);

    IPage<MoldMonitorDTO.ListDTO> paging(Page query,@Param("params")  MoldMonitorDTO.PagingParamDTO params);

}
