package com.erp.server.plm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.sensitive.SensitiveWordMatcher;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.plm.dto.ProductForbiddenWordCheckDTO;
import com.erp.model.plm.dto.excel.ProductForbiddenWordCheckReportExcelDTO;
import com.erp.model.plm.entity.ProductForbiddenWordCheckEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.enums.ProductForbiddenWordCheckStatusEnum;
import com.erp.server.plm.mapper.ProductForbiddenWordCheckMapper;
import com.erp.server.plm.service.ProductForbiddenWordCheckService;
import com.erp.server.plm.support.PlmPagingSortSupport;
import com.erp.server.plm.support.ProductForbiddenWordMatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品违禁词检测记录 服务实现类
 * </p>
 */
@Slf4j
@Service
public class ProductForbiddenWordCheckServiceImpl extends SuperServiceImpl<ProductForbiddenWordCheckMapper, ProductForbiddenWordCheckEntity> implements ProductForbiddenWordCheckService {

    private static final int SCAN_BATCH_SIZE = 1000;

    private static final int REPORT_WRITE_BATCH_SIZE = 5000;

    private static final int MAX_REPORT_ROWS = 100000;

    private static final String DETECT_LOCK_KEY = "plm:productForbiddenWordCheck:detect";

    private static final DateTimeFormatter REPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private ProductForbiddenWordMatcher productForbiddenWordMatcher;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    @Qualifier("customExecutor")
    private ThreadPoolTaskExecutor customExecutor;

    @Override
    public PagingVO<ProductForbiddenWordCheckDTO.ListDTO> paging(PagingDTO<ProductForbiddenWordCheckDTO.PagingParamDTO> dto) {
        PlmPagingSortSupport.sanitizeForbiddenWordCheckSort(dto.getParams().getSortList());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ProductForbiddenWordCheckDTO.ListDTO> pageData = baseMapper.paging(query, dto.getParams());
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductForbiddenWordCheckDTO.DetectDTO detect() {
        RLock detectLock = redissonClient.getLock(DETECT_LOCK_KEY);
        boolean locked = false;
        try {
            locked = detectLock.tryLock(0, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new ServiceException("已有检测任务进行中，请稍后再试");
            }
            if (countActiveDetectTask() > 0) {
                throw new ServiceException("已有检测任务进行中，请稍后再试");
            }
            ProductForbiddenWordCheckEntity entity = buildCheckEntity();
            super.save(entity);
            runAfterCommit(entity.getId());
            return new ProductForbiddenWordCheckDTO.DetectDTO(entity.getId(), entity.getReportName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("创建检测任务失败，请稍后重试");
        } finally {
            if (locked && detectLock.isHeldByCurrentThread()) {
                detectLock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        ProductForbiddenWordCheckEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到产品检测记录"));
        if (Objects.equals(ProductForbiddenWordCheckStatusEnum.RUNNING.getCode(), entity.getStatus())) {
            throw new ServiceException("检测中数据不允许删除");
        }
        boolean removed = lambdaUpdate()
                .eq(ProductForbiddenWordCheckEntity::getId, id)
                .eq(ProductForbiddenWordCheckEntity::getVersion, entity.getVersion())
                .ne(ProductForbiddenWordCheckEntity::getStatus,
                        ProductForbiddenWordCheckStatusEnum.RUNNING.getCode())
                .remove();
        if (!removed) {
            throw new ServiceException("产品检测记录状态已变更，请刷新后重试");
        }
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
            if (!updateRunning(entity)) {
                log.info("产品违禁词检测任务已被删除或状态已变更，跳过执行，id={}", id);
                return;
            }
            SensitiveWordMatcher matcher = productForbiddenWordMatcher.openSnapshot();
            Integer totalCount = baseMapper.countProductForDetect();
            ReportWriteResult writeResult = writeReportFile(entity.getReportName(), matcher, totalCount);
            reportFile = writeResult.getReportFile();
            String reportUrl = FastDFSClientUtil.uploadFile(reportFile, entity.getReportName());
            ProductForbiddenWordCheckEntity updateEntity = new ProductForbiddenWordCheckEntity();
            updateEntity.setId(id);
            updateEntity.setVersion(entity.getVersion());
            updateEntity.setStatus(ProductForbiddenWordCheckStatusEnum.FINISH.getCode());
            updateEntity.setFinishTime(LocalDateTime.now());
            updateEntity.setReportUrl(reportUrl);
            updateEntity.setTotalCount(totalCount);
            updateEntity.setHitCount(writeResult.getHitCount());
            updateEntity.setFailReason(writeResult.isTruncated() ? "命中结果超过上限，仅导出前10万条" : "");
            if (!super.updateById(updateEntity)) {
                throw new ServiceException("检测任务状态已变更，结果保存失败");
            }
            entity.setVersion(updateEntity.getVersion());
        } catch (Exception e) {
            log.error("产品违禁词检测失败，id：{}", id, e);
            updateFail(entity, e);
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
        RLock seqLock = redissonClient.getLock("plm:productForbiddenWordCheck:seq:" + reportDate);
        seqLock.lock();
        try {
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
        } finally {
            if (seqLock.isHeldByCurrentThread()) {
                seqLock.unlock();
            }
        }
    }

    private ReportWriteResult writeReportFile(String reportName, SensitiveWordMatcher matcher, Integer totalCount) throws IOException {
        File tempDirectory = FileUtils.getTempDirectory();
        File templateFile = new File(tempDirectory, "productForbiddenWordCheckTemplate" + LocalDate.now() + ".xlsx");
        File outputFile = new File(tempDirectory, reportName);
        if (!templateFile.exists()) {
            EasyExcel.write(templateFile, ProductForbiddenWordCheckReportExcelDTO.class)
                    .sheet("产品检测")
                    .doWrite(Collections.emptyList());
        }
        ExcelWriter excelWriter = EasyExcel.write(outputFile, ProductForbiddenWordCheckReportExcelDTO.class)
                .withTemplate(templateFile)
                .inMemory(false)
                .build();
        WriteSheet writeSheet = EasyExcel.writerSheet("产品检测").build();
        List<ProductForbiddenWordCheckReportExcelDTO> batch = new ArrayList<>(REPORT_WRITE_BATCH_SIZE);
        int hitCount = 0;
        boolean truncated = false;
        int offset = 0;
        int safeTotalCount = Objects.isNull(totalCount) ? 0 : totalCount;
        try {
            while (offset < safeTotalCount && hitCount < MAX_REPORT_ROWS) {
                List<ProductForbiddenWordCheckDTO.ProductScanDTO> products = baseMapper.listProductForDetect(SCAN_BATCH_SIZE, offset);
                if (CollectionUtils.isEmpty(products)) {
                    break;
                }
                for (ProductForbiddenWordCheckDTO.ProductScanDTO product : products) {
                    List<String> hitWords = matcher.findAll(product.getName());
                    if (CollectionUtils.isEmpty(hitWords)) {
                        continue;
                    }
                    batch.add(buildReportRow(product, hitWords));
                    hitCount++;
                    if (hitCount >= MAX_REPORT_ROWS) {
                        truncated = true;
                        break;
                    }
                    if (batch.size() >= REPORT_WRITE_BATCH_SIZE) {
                        excelWriter.write(batch, writeSheet);
                        batch.clear();
                    }
                }
                offset += products.size();
            }
            if (CollectionUtils.isNotEmpty(batch)) {
                excelWriter.write(batch, writeSheet);
            }
        } finally {
            excelWriter.finish();
        }
        return new ReportWriteResult(outputFile, hitCount, truncated);
    }

    private ProductForbiddenWordCheckReportExcelDTO buildReportRow(ProductForbiddenWordCheckDTO.ProductScanDTO product, List<String> hitWords) {
        return new ProductForbiddenWordCheckReportExcelDTO(
                product.getSkuNo(),
                product.getName(),
                hitWords.stream().collect(Collectors.joining("\n")),
                ProductDetailStatusEnum.getName(product.getStatus())
        );
    }

    private long countActiveDetectTask() {
        return lambdaQuery()
                .in(ProductForbiddenWordCheckEntity::getStatus,
                        ProductForbiddenWordCheckStatusEnum.WAIT.getCode(),
                        ProductForbiddenWordCheckStatusEnum.RUNNING.getCode())
                .count();
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

    private boolean updateRunning(ProductForbiddenWordCheckEntity entity) {
        if (!Objects.equals(ProductForbiddenWordCheckStatusEnum.WAIT.getCode(), entity.getStatus())) {
            return false;
        }
        ProductForbiddenWordCheckEntity updateEntity = new ProductForbiddenWordCheckEntity();
        updateEntity.setId(entity.getId());
        updateEntity.setVersion(entity.getVersion());
        updateEntity.setStatus(ProductForbiddenWordCheckStatusEnum.RUNNING.getCode());
        updateEntity.setFailReason("");
        boolean updated = super.updateById(updateEntity);
        if (updated) {
            entity.setVersion(updateEntity.getVersion());
            entity.setStatus(ProductForbiddenWordCheckStatusEnum.RUNNING.getCode());
        }
        return updated;
    }

    private void updateFail(ProductForbiddenWordCheckEntity entity, Exception e) {
        String failReason = e instanceof ServiceException
                ? ((ServiceException) e).getMsg() : "检测任务执行失败，请稍后重试";
        ProductForbiddenWordCheckEntity updateEntity = new ProductForbiddenWordCheckEntity();
        updateEntity.setId(entity.getId());
        updateEntity.setVersion(entity.getVersion());
        updateEntity.setStatus(ProductForbiddenWordCheckStatusEnum.FAIL.getCode());
        updateEntity.setFinishTime(LocalDateTime.now());
        updateEntity.setFailReason(StringUtils.left(
                StringUtils.defaultIfBlank(failReason, "检测任务执行失败，请稍后重试"), 500));
        if (!super.updateById(updateEntity)) {
            log.warn("产品违禁词检测失败状态保存未生效，任务可能已变更，id={}", entity.getId());
        }
    }

    private void fillList(List<ProductForbiddenWordCheckDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (ProductForbiddenWordCheckDTO.ListDTO item : list) {
            item.setStatusName(ProductForbiddenWordCheckStatusEnum.getName(item.getStatus()));
        }
    }

    private static class ReportWriteResult {
        private final File reportFile;
        private final int hitCount;
        private final boolean truncated;

        private ReportWriteResult(File reportFile, int hitCount, boolean truncated) {
            this.reportFile = reportFile;
            this.hitCount = hitCount;
            this.truncated = truncated;
        }

        private File getReportFile() {
            return reportFile;
        }

        private int getHitCount() {
            return hitCount;
        }

        private boolean isTruncated() {
            return truncated;
        }
    }
}
