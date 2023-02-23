package com.erp.server.plm.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.ProjectTaskTimeRecordDTO;
import com.erp.model.plm.entity.ProjectTaskTimeRecordEntity;
import com.erp.model.plm.vo.ProjectTaskTimeRecordPageVO;
import com.erp.server.plm.mapper.ProjectTaskTimeRecordMapper;
import com.erp.server.plm.service.ProjectTaskTimeRecordService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-02-23
 */
@Service
public class ProjectTaskTimeRecordServiceImpl extends ServiceImpl<ProjectTaskTimeRecordMapper, ProjectTaskTimeRecordEntity> implements ProjectTaskTimeRecordService {

    @Override
    public PagingVO<ProjectTaskTimeRecordPageVO> pageRecord(PagingDTO<ProjectTaskTimeRecordDTO.PageRecordDto> dto) {
        // 查询 产品数据分组
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ProjectTaskTimeRecordPageVO> recordPage  = baseMapper.pageTaskTimeRecord(query, dto.getParams(), dto.getParam());
        return new PagingVO(recordPage);
    }

    @Override
    public Boolean exportTaskTimeList(ProjectTaskTimeRecordDTO.PageRecordDto dto, HttpServletResponse response) {
        List<ProjectTaskTimeRecordPageVO> projectTaskTimeRecordList  = baseMapper.pageTaskTimeRecord(dto, dto.getParam());
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/taskTime.xlsx";
        String name = "工时统计";
        String dateStr = LocalDateTimeUtil.format(LocalDateTime.now(), DateUtil.fmt);
        try {
            new ExcelPrintUtils().patchExport(projectTaskTimeRecordList, response, StrUtil.format("{}-{}", name, dateStr), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }
}
