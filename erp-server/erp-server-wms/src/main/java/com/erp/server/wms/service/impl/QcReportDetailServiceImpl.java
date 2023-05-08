package com.erp.server.wms.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.QcReportDTO;
import com.erp.model.wms.dto.QcReportDetailDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.dto.excel.ExportQcReportExcelDTO;
import com.erp.model.wms.dto.excel.QcReportDetailImportExcelDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.QcReportDetailEntity;
import com.erp.model.wms.entity.QcReportEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.listener.QcReportDetailExcelListener;
import com.erp.server.wms.mapper.QcReportDetailMapper;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.QcReportDetailService;
import com.erp.server.wms.service.QcReportService;
import com.erp.server.wms.service.WmsAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
@Slf4j
public class QcReportDetailServiceImpl extends SuperServiceImpl<QcReportDetailMapper, QcReportDetailEntity> implements QcReportDetailService {


    @Resource
    private QcReportService qcReportService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    /**
     * 质检报告明细暂存
     *
     * @param billId
     * @param reportDetailList
     * @return void
     * @author yl
     * @date 2023-04-19 11:14
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(String billId, List<QcReportDetailDTO.AddDTO> reportDetailList) {
        List<QcReportDetailEntity> dbList = this.findByMainId(billId);
        if (CollectionUtils.isEmpty(reportDetailList)) {
            List<String> deleteIdList = dbList.stream().map(QcReportDetailEntity::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deleteIdList)) {
                this.removeByIds(deleteIdList);
            }
            return;
        }

        List<QcReportDetailEntity> saveOrUpdateList = new ArrayList<>(reportDetailList.size());
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        //获取到删除的id
        List<String> deleteIdList = getDeleteIds(reportDetailList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        String type = WmsConstant.QC_REPORT;
        for (QcReportDetailDTO.AddDTO item : reportDetailList) {
            QcReportDetailEntity entity = new QcReportDetailEntity();
            BeanMapper.copy(item, entity);
            entity.setMainId(billId);
            saveOrUpdateList.add(entity);
            //附件集合
            List<String> attachmentUrlList = item.getReportUrlList();
            List<String> attachmentNameList = item.getReportNameList();
            int nameSize = CollectionUtils.isNotEmpty(attachmentNameList) ? attachmentNameList.size() : 0;
            if (CollectionUtils.isNotEmpty(attachmentUrlList)) {
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    WmsAttachmentEntity addAttachment = new WmsAttachmentEntity();
                    addAttachment.setAttachUrl(attachmentUrlList.get(i));
                    if (CollectionUtils.isNotEmpty(attachmentNameList)) {
                        if (nameSize > i) {
                            addAttachment.setAttachName(attachmentNameList.get(i));
                        }
                    }
                    addAttachment.setBusinessId(entity.getId());
                    addAttachment.setType(type);
                    batchAttachmentList.add(addAttachment);
                }

            }

        }

        this.saveOrUpdateBatch(saveOrUpdateList);
        if (CollectionUtils.isNotEmpty(batchAttachmentList)) {
            wmsAttachmentService.saveBatch(batchAttachmentList);
        }
    }


    /**
     * 根据质检单id获取 质检报告明细信息
     *
     * @param billId
     * @return java.util.List<com.erp.model.wms.dto.QcReportDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-04-19 12:34
     */
    @Override
    public List<QcReportDetailDTO.ViewDTO> getByMainId(String billId) {
        List<QcReportDetailEntity> list = this.findByMainId(billId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<QcReportDetailDTO.ViewDTO> viewList = BeanMapper.copyList(list, QcReportDetailDTO.ViewDTO.class);
        //质检报告的id 集合
        List<String> qcReportIds = viewList.stream().map(QcReportDetailDTO.ViewDTO::getQcReportId).collect(Collectors.toList());
        List<QcReportEntity> qcReportList = qcReportService.listByIds(qcReportIds);

        //获取到业务表id
        List<String> businessIds = list.stream().map(QcReportDetailEntity::getId).collect(Collectors.toList());

        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(businessIds);

        for (QcReportDetailDTO.ViewDTO item : viewList) {
            QcReportEntity report = qcReportList.stream().filter(q -> q.getId().equals(item.getQcReportId())).
                    findFirst().orElse(null);
            if (report != null) {
                item.setQcReportName(report.getName());
                item.setQcReportContent(report.getContent());
            }
            List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
            String resultDict = item.getResultDict();
            String resultName = dictList.stream().filter(d -> d.getValue().equals(resultDict)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setResultName(resultName);

            List<String> attachmentUrlList = attachmentList.stream().
                    filter(a -> a.getBusinessId().equals(item.getId())).
                    map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).
                    collect(Collectors.toList());
            List<String> attachmentNameList = attachmentList.stream().
                    filter(a -> a.getBusinessId().equals(item.getId())).
                    map(WmsAttachmentDTO.UpdateDTO::getAttachName).
                    collect(Collectors.toList());
            item.setReportUrlList(attachmentUrlList);
            item.setReportNameList(attachmentNameList);

        }

        return viewList;
    }


    /**
     * 导入数据
     *
     * @param excelFile
     * @param response
     * @return com.erp.model.wms.dto.QcReportDetailDTO.ImportDTO
     * @author yl
     * @date 2023-04-21 19:26
     */
    @Override
    public QcReportDetailDTO.ImportDTO importFile(MultipartFile excelFile, String qcType, HttpServletResponse response) {
        List<QcReportDTO.ListDTO> qcReportList = qcReportService.getByQcType(qcType);
        List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
        QcReportDetailExcelListener excelListenerUtil = new QcReportDetailExcelListener(qcReportList, dictList);

        try {
            EasyExcel.read(excelFile.getInputStream(), QcReportDetailImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        QcReportDetailDTO.ImportDTO result = new QcReportDetailDTO.ImportDTO();
        //导入数据处理
        List<QcReportDetailDTO.ListDTO> successList = excelListenerUtil.getSuccessList();
        result.setSuccessList(successList);
        //导出错误数据
        List<QcReportDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "质检单错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, QcReportDetailImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }


    /**
     * 根据质检单id 集合 获取删除数据
     *
     * @param mainIdList
     * @return void
     * @author yl
     * @date 2023-04-25 16:15
     */
    @Override
    public void removeByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            LambdaQueryWrapper<QcReportDetailEntity> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.in(QcReportDetailEntity::getMainId, mainIdList);
            this.remove(queryWrapper);
        }
    }

    /**
     * 质检列表 导出质检报告
     * @author yl
     * @date 2023-04-25 17:12
     * @param mainId
     * @param response
     * @return void
     */
    /**
     * 质检列表 导出质检报告
     *
     * @param mainId
     * @param response
     * @return void
     * @author yl
     * @date 2023-04-25 17:12
     */
    @Override
    public void exportReportByMainId(String mainId, HttpServletResponse response) {
        List<QcReportDetailDTO.ListDTO> list = baseMapper.getByMainId(mainId);
        List<DictBasicDTO> dictList = dictBasicService.getByKey(DictBasicEnum.QC_REPORT_RESULT.getKey());
        List<ExportQcReportExcelDTO> resultList = BeanMapper.copyList(list, ExportQcReportExcelDTO.class);
        for (ExportQcReportExcelDTO item : resultList) {
            String resultDict = item.getResultDict();
            String resultName = dictList.stream().filter(d -> d.getValue().equals(resultDict)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setResultDict(resultName);
        }

        String fileName = "质检报告数据";
        try {
            ExcelUtil.export(fileName, "质检报告", resultList, ExportQcReportExcelDTO.class, response);
        } catch (Exception e) {
            log.error("导出质检报告出错  ==e", e);
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }


    /**
     * 根据主表id 获取数据
     *
     * @param billId
     * @return java.util.List<com.erp.model.wms.entity.QcReportDetailEntity>
     * @author yl
     * @date 2023-04-19 11:20
     */
    private List<QcReportDetailEntity> findByMainId(String billId) {
        return lambdaQuery().eq(QcReportDetailEntity::getMainId, billId).list();
    }


    /**
     * 获取要删除的id 集合
     *
     * @param list
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-04-13 14:59
     */
    private List<String> getDeleteIds(List<QcReportDetailDTO.AddDTO> list, List<QcReportDetailEntity> dbList) {
        List<String> ids = list.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(QcReportDetailDTO.AddDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(QcReportDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }
}
