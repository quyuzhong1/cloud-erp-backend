package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.bi.dto.DashboardDTO;
import com.erp.model.bi.dto.SubjectDTO;
import com.erp.model.bi.dto.SubjectPagingDTO;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.model.bi.vo.SubjectVO;
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


    List<DashboardDTO> getDashboardList(@Param("type") String type, @Param("dashboardFlag") String dashboardFlag,@Param("findIdList") List<String> idList,@Param("searchKeyword") String searchKeyword);

    List<DashboardDTO> getMyCreateDashboardList(@Param("type") String type, @Param("dashboardFlag") String dashboardFlag,@Param("userId") String userId,@Param("searchKeyword") String searchKeyword);

    IPage<SubjectPagingDTO> paging(Page<Object> query, @Param("params") BaseSearchDTO params);

    /**
     * @deprecated
     * This method is deprecated and will be removed in future versions.
     * Please use {@link #getUserVisibleModuleIdsNew(String)} instead.
     */
    @Deprecated
    List<String> getUserVisibleSubjectId(@Param("userId") String userId);

    List<SubjectVO> getSubjectByIds(@Param("subjectIdList") List<String> subjectIdList, @Param("searchKeyword") String searchKeyword);

    List<SubjectDTO> getByIds(@Param("subjectIdList") List<String> subjectIdList, @Param("searchKeyword") String searchKeyword);

}

