package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.mrp.dto.HistoryImportRecordDTO;
import com.erp.model.mrp.entity.HistoryImportRecordEntity;
import com.erp.server.mrp.mapper.HistoryImportRecordMapper;
import com.erp.server.mrp.service.HistoryImportRecordService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

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


    @GlobalTransactional(rollbackFor = Exception.class)
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
        //导入文件
        try {
            File file = ExcelUtil.batchExportFile(addDTO.getExportFileDTO());
            if (file == null || file.isDirectory()) {
                throw new ServiceException("成功文件记录上传失败");
            }
            String url = FastDFSClientUtil.uploadFile(file, addDTO.getExportFileDTO().getFileName());
            //url不能为空
            if (StrUtil.isBlank(url)) {
                throw new ServiceException("导入失败！");
            }
            entity.setFileUrl(url);
        } catch (Exception e) {
            throw new ServiceException("导入文件上传到fastdfs失败");
        }
        return entity;
    }
}
