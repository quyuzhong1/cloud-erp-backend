package com.erp.server.plm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.plm.dto.ProductForbiddenWordCheckDTO;
import com.erp.model.plm.dto.excel.ProductForbiddenWordCheckReportExcelDTO;
import com.erp.model.plm.entity.ProductForbiddenWordCheckEntity;
import com.erp.model.plm.enums.ProductForbiddenWordCheckStatusEnum;
import com.erp.server.plm.mapper.ProductForbiddenWordCheckMapper;
import com.erp.server.plm.service.CfgProductForbiddenWordService;
import com.erp.server.plm.service.ProductForbiddenWordCheckService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品违禁词检测记录 服务实现类
 * </p>
 */
@Slf4j
@Service
public class ProductForbiddenWordCheckServiceImpl extends SuperServiceImpl<ProductForbiddenWordCheckMapper, ProductForbiddenWordCheckEntity> implements ProductForbiddenWordCheckService {

    private static final Object REPORT_SEQ_LOCK = new Object();

    private static final int BATCH_SIZE = 1000;

    private static final DateTimeFormatter REPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private CfgProductForbiddenWordService cfgProductForbiddenWordService;

    @Resource
    @Qualifier("customExecutor")
    private ThreadPoolTaskExecutor customExecutor;

    @Override
    public PagingVO<ProductForbiddenWordCheckDTO.ListDTO> paging(PagingDTO<ProductForbiddenWordCheckDTO.PagingParamDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ProductForbiddenWordCheckDTO.ListDTO> pageData = baseMapper.paging(query, dto.getParams());
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductForbiddenWordCheckDTO.DetectDTO detect() {
        ProductForbiddenWordCheckEntity entity;
        synchronized (REPORT_SEQ_LOCK) {
            entity = buildCheckEntity();
            super.save(entity);
        }
        runAfterCommit(entity.getId());
        return new ProductForbiddenWordCheckDTO.DetectDTO(entity.getId(), entity.getReportName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        ProductForbiddenWordCheckEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到产品检测记录"));
        if (Objects.equals(ProductForbiddenWordCheckStatusEnum.RUNNING.getCode(), entity.getStatus())) {
            throw new ServiceException("检测中数据不允许删除");
        }
        super.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getReportName(), OperationTypeEnum.DELETE);
    }

    @Override
    public String downloadReport(String id) {
        ProductForbiddenWordCheckEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到产品检测记录"));
        if (!Objects.equals(ProductForbiddenWordCheckStatusEnum.FINISH.getCode(), entity.getStatus()) || StringUtils.isBlank(entity.getReportUrl())) {
            throw new ServiceException("检测报告未生成，暂不支持下载");
        }
        if (StringUtils.startsWithIgnoreCase(entity.getReportUrl(), "http")) {
            return entity.getReportUrl();
        }
        return FastDFSClientUtil.publicUrl + entity.getReportUrl();
    }

    @Override
    public void executeDetect(String id) {
        ProductForbiddenWordCheckEntity entity = super.getById(id);
        if (Objects.isNull(entity)) {
            return;
        }
        File reportFile = null;
        try {
            updateRunning(id);
            List<String> enabledWords = cfgProductForbiddenWordService.listEnabledWords();
            Integer totalCount = baseMapper.countProductForDetect();
            List<ProductForbiddenWordCheckReportExcelDTO> reportRows = new ArrayList<>();
            int offset = 0;
            while (offset < totalCount) {
                List<ProductForbiddenWordCheckDTO.ProductScanDTO> products = baseMapper.listProductForDetect(BATCH_SIZE, offset);
                if (CollectionUtils.isEmpty(products)) {
                    break;
                }
                for (ProductForbiddenWordCheckDTO.ProductScanDTO product : products) {
                    List<String> hitWords = cfgProductForbiddenWordService.matchWords(product.getName(), enabledWords);
                    if (CollectionUtils.isEmpty(hitWords)) {
                        continue;
                    }
                    reportRows.add(new ProductForbiddenWordCheckReportExcelDTO(
                            product.getSkuNo(),
                            product.getName(),
                            hitWords.stream().collect(Collectors.joining("\n")),
                            DisabledEnumName.ENABLE
                    ));
                }
                offset += products.size();
            }
            reportFile = ExcelUtil.exportFile(entity.getReportName(), "产品检测", reportRows, ProductForbiddenWordCheckReportExcelDTO.class);
            String reportUrl = FastDFSClientUtil.uploadFile(reportFile, entity.getReportName());
            ProductForbiddenWordCheckEntity updateEntity = new ProductForbiddenWordCheckEntity();
            updateEntity.setId(id);
            updateEntity.setStatus(ProductForbiddenWordCheckStatusEnum.FINISH.getCode());
            updateEntity.setFinishTime(LocalDateTime.now());
            updateEntity.setReportUrl(reportUrl);
            updateEntity.setTotalCount(totalCount);
            updateEntity.setHitCount(reportRows.size());
            updateEntity.setFailReason("");
            super.updateById(updateEntity);
        } catch (Exception e) {
            log.error("产品违禁词检测失败，id：{}", id, e);
            updateFail(id, e);
        } finally {
            if (Objects.nonNull(reportFile) && reportFile.exists()) {
                boolean deleted = reportFile.delete();
                if (!deleted) {
                    log.warn("产品违禁词检测临时报告删除失败：{}", reportFile.getAbsolutePath());
                }
            }
        }
    }

    private ProductForbiddenWordCheckEntity buildCheckEntity() {
        String reportDate = LocalDate.now().format(REPORT_DATE_FORMATTER);
        Integer reportSeq = baseMapper.nextSeq(reportDate);
        String reportName = StrUtil.format("检测报告{}{}.xlsx", reportDate, String.format("%06d", reportSeq));
        ProductForbiddenWordCheckEntity entity = new ProductForbiddenWordCheckEntity();
        entity.setReportDate(reportDate);
        entity.setReportSeq(reportSeq);
        entity.setReportName(reportName);
        entity.setStatus(ProductForbiddenWordCheckStatusEnum.WAIT.getCode());
        entity.setTotalCount(0);
        entity.setHitCount(0);
        return entity;
    }

    private void runAfterCommit(String id) {
        Runnable task = () -> executeDetect(id);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    customExecutor.execute(task);
                }
            });
            return;
        }
        customExecutor.execute(task);
    }

    private void updateRunning(String id) {
        ProductForbiddenWordCheckEntity updateEntity = new ProductForbiddenWordCheckEntity();
        updateEntity.setId(id);
        updateEntity.setStatus(ProductForbiddenWordCheckStatusEnum.RUNNING.getCode());
        updateEntity.setFailReason("");
        super.updateById(updateEntity);
    }

    private void updateFail(String id, Exception e) {
        String failReason = StringUtils.defaultIfBlank(e.getMessage(), e.toString());
        ProductForbiddenWordCheckEntity updateEntity = new ProductForbiddenWordCheckEntity();
        updateEntity.setId(id);
        updateEntity.setStatus(ProductForbiddenWordCheckStatusEnum.FAIL.getCode());
        updateEntity.setFinishTime(LocalDateTime.now());
        updateEntity.setFailReason(StringUtils.left(failReason, 500));
        super.updateById(updateEntity);
    }

    private void fillList(List<ProductForbiddenWordCheckDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (ProductForbiddenWordCheckDTO.ListDTO item : list) {
            item.setStatusName(ProductForbiddenWordCheckStatusEnum.getName(item.getStatus()));
        }
    }

    private static class DisabledEnumName {
        private static final String ENABLE = "启用";
    }
}
