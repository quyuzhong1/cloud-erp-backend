package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.ProjectTaskTimeRecordDTO;
import com.erp.model.plm.entity.ProjectTaskTimeRecordEntity;
import com.erp.model.plm.vo.ProjectTaskTimeRecordPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Cloud
 * @since 2023-02-23
 */
@Mapper
public interface ProjectTaskTimeRecordMapper extends BaseMapper<ProjectTaskTimeRecordEntity> {

    /**
     * 分页查询任务列表
     *
     * @param page
     * @param dto
     * @param param
     * @return
     */
    IPage<ProjectTaskTimeRecordPageVO> pageTaskTimeRecord(Page page, @Param("params") ProjectTaskTimeRecordDTO.PageRecordDto dto, @Param("param") String param);

    List<ProjectTaskTimeRecordPageVO> pageTaskTimeRecord(@Param("params") ProjectTaskTimeRecordDTO.PageRecordDto dto, @Param("param") String param);

    List<ProjectTaskTimeRecordDTO.TaskWorkTimeDTO> listByProductIds(@Param("productIdList") List<String> productIds);
}
