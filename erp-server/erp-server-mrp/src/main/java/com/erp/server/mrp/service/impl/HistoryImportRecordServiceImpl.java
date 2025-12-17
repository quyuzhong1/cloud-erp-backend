package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.dto.FileExcelDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.HistoryImportRecordDTO;
import com.erp.model.mrp.entity.HistoryImportRecordEntity;
import com.erp.server.mrp.mapper.HistoryImportRecordMapper;
import com.erp.server.mrp.service.HistoryImportRecordService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * <p>
 * 历史导入记录 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-27
 */
@Slf4j
@Service
public class HistoryImportRecordServiceImpl extends SuperServiceImpl<HistoryImportRecordMapper, HistoryImportRecordEntity> implements HistoryImportRecordService {


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(HistoryImportRecordDTO.AddDTO addDTO) {
        // 数据处理
        HistoryImportRecordEntity historyImportRecordEntity =  upLoadFile(addDTO);

        log.info("开始新增历史导入记录");
        boolean save = super.save(historyImportRecordEntity);
        if(!save) {
            throw new ServiceException("历史导入记录保存失败");
        }
        return new BaseResultDTO.AddDTO(historyImportRecordEntity.getId(), historyImportRecordEntity.getId());
    }

    @Override
    public PagingVO<HistoryImportRecordDTO.ListDTO> paging(PagingDTO<HistoryImportRecordDTO.PagingParamDTO> dto) {
        HistoryImportRecordDTO.PagingParamDTO params = dto.getParams();
        Page<HistoryImportRecordDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<HistoryImportRecordDTO.ListDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);
    }


    /**
     * 上传导入文件到fastdfs
     * @author will
     * @date 2024/8/29 10:32
     * @param addDTO
     * @return HistoryImportRecordEntity
     */
    private HistoryImportRecordEntity upLoadFile(HistoryImportRecordDTO.AddDTO addDTO) {
        HistoryImportRecordEntity entity = new HistoryImportRecordEntity();
        BeanMapperUtils.copy(addDTO,entity);
        //多sheet导出
        if (CollectionUtils.isNotEmpty(addDTO.getExportFileDTO().getSheetList())) {
            sheetPatchExport(entity,addDTO);
        }
        //自定义导出
        if (ObjectUtil.isNotEmpty(addDTO.getExportFileDTO().getCustomSheet())) {
            customExport(entity,addDTO);
        }
        return entity;
    }

    /**
     * 自定义表头导出
     * @author will
     * @date 2024/9/10 15:34
     * @param entity
     * @param addDTO
     */
    private void customExport (HistoryImportRecordEntity entity,HistoryImportRecordDTO.AddDTO addDTO) {
        //导入文件
        try {
            //导出数据
            FileExcelDTO.ExportFileDTO exportFileDTO = addDTO.getExportFileDTO();
            //sheet对象
            FileExcelDTO.ExportFileSheetDTO customSheet = exportFileDTO.getCustomSheet();
            File file = ExcelUtil.exportFile(exportFileDTO.getFileName(), customSheet.getSheetName(), customSheet.getDataResult(), customSheet.getHeads());
            if (file.isDirectory()) {
                throw new ServiceException("成功文件记录上传失败");
            }
            String url = FastDFSClientUtil.uploadFile(file, exportFileDTO.getFileName());
            //url不能为空
            if (CharSequenceUtil.isBlank(url)) {
                throw new ServiceException("导入失败！");
            }
            entity.setFileUrl(url);
        } catch (Exception e) {
            throw new ServiceException("导入文件上传到fastdfs失败");
        }
    }

    /**
     * 多sheet页模板导出
     * @author will
     * @date 2024/9/10 15:33
     * @param entity
     * @param addDTO
     */
    private void sheetPatchExport (HistoryImportRecordEntity entity,HistoryImportRecordDTO.AddDTO addDTO) {
        //导入文件
        StringBuilder sb = new StringBuilder();
        String excelPath = addDTO.getExportFileDTO().getPathUrl();
        String name = addDTO.getExportFileDTO().getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        sb.append(excelPath.substring(excelPath.lastIndexOf(".")));
        try {
            byte[] bytes = new ExcelPrintUtils().sheetPatchExport(addDTO.getExportFileDTO().getSheetList(), sb.toString(),excelPath);
            String s = FastDFSClientUtil.uploadFile(bytes, sb.toString(), null);
            entity.setFileUrl(s);
        } catch (IOException e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new ServiceException(e.getMessage());
        }
    }
}
