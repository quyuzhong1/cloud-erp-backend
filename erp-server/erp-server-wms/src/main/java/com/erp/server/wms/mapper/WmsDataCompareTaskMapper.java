package com.erp.server.wms.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.model.wms.entity.WmsDataCompareTaskEntity;


/**
 * <p>
 * 数据对比任务 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
 */
@Mapper
public interface WmsDataCompareTaskMapper extends BaseMapper<WmsDataCompareTaskEntity> {
	IPage<WmsDataCompareTaskDTO.ViewDTO> paging(Page query, @Param("params") WmsDataCompareTaskDTO.PagingParamDTO params);
}
