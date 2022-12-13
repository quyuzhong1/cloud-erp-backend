package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.model.bi.dto.DashboardDTO;
import com.erp.model.bi.dto.SubjectPagingDTO;
import com.erp.model.bi.entity.BiSubjectEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专题表(BiSubject)表数据库访问层
 *
 * @author yl
 * @since 2022-12-08 14:31:58
 */
@Mapper
public interface BiSubjectMapper extends BaseMapper<BiSubjectEntity> {


    List<DashboardDTO> getDashboardFrequentlyList(@Param("type") String type, @Param("dashboardFlag") String dashboardFlag,@Param("findIdList") List<String> idList,@Param("searchKeyword") String searchKeyword);

    List<DashboardDTO> getMyCreateDashboardList(@Param("type") String type, @Param("dashboardFlag") String dashboardFlag,@Param("userId") String userId,@Param("searchKeyword") String searchKeyword);

    IPage<SubjectPagingDTO> paging(Page query, @Param("params") BaseSearchDTO params);
}

