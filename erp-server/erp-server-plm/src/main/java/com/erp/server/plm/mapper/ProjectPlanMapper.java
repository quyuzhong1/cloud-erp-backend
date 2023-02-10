package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.entity.ProjectPlanEntity;
import com.erp.model.plm.vo.SchedulePagingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Classname ProjectPlanMapper
 * @Description TODO
 * @Date 2023-02-03 15:16
 * @Created by yl
 */
@Mapper
public interface ProjectPlanMapper extends BaseMapper<ProjectPlanEntity> {
    IPage<SchedulePagingVO> paging(Page query, @Param("params")SearchPagingDTO params,@Param("idList") List<String> idList, @Param("statusList")List<String> statusList);
}
