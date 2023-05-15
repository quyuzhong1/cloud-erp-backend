package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.model.wms.dto.excel.ExportQcDocumentExcelDTO;
import com.erp.model.wms.dto.excel.ExportQcPersonnelExcelDTO;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.model.wms.enums.QcReportExportExcelTypeEnum;
import com.erp.model.wms.enums.ViewQcTrendEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.QcInfoMapper;
import com.erp.server.wms.service.QcEffectivenessService;
import com.erp.server.wms.service.WarehouseReceiveService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/12 11:18
 */
@Service
public class QcEffectivenessServiceImpl implements QcEffectivenessService {

    @Resource
    private QcInfoMapper qcInfoMapper;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;




    @Override
    public QcEffectivenessDTO.ViewQcOverviewDTO viewQcOverview(QcEffectivenessDTO.CommonSearchParamDTO dto) {
        QcEffectivenessDTO.ViewQcOverviewDTO resultDTO = new QcEffectivenessDTO.ViewQcOverviewDTO();

        List<QcEffectivenessDTO.ViewQcOverviewDetailDTO> details = new ArrayList<>();

        //质检总览
        List<QcEffectivenessDTO.ViewQcOverviewDetailDTO> list = qcInfoMapper.listQcBillGroupQcStatus(dto);

        //总计
        QcEffectivenessDTO.ViewQcOverviewDetailDTO totalDTO = new QcEffectivenessDTO.ViewQcOverviewDetailDTO();
        totalDTO.setType("总计");
        Integer totalCount = MathUtil.ZERO;
        if (CollectionUtils.isNotEmpty(list)) {
             totalCount = list.stream().map(QcEffectivenessDTO.ViewQcOverviewDetailDTO::getCount).reduce(MathUtil.ZERO, Integer::sum);
        }
        totalDTO.setCount(totalCount);
        details.add(totalDTO);

        Integer qcCount = MathUtil.ZERO;
        QcBillStatusEnum[] values = QcBillStatusEnum.values();
        for (QcBillStatusEnum qcBillStatusEnum : values) {
            QcEffectivenessDTO.ViewQcOverviewDetailDTO overviewDetailDTO = new QcEffectivenessDTO.ViewQcOverviewDetailDTO();
            Integer count = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(list)) {
                count = list.stream().filter(obj -> obj.getType().equals(qcBillStatusEnum.getCode())).map(QcEffectivenessDTO.ViewQcOverviewDetailDTO::getCount).findFirst().orElse(MathUtil.ZERO);
            }
            overviewDetailDTO.setCount(count);
            overviewDetailDTO.setType(qcBillStatusEnum.getName());
            if (QcBillStatusEnum.WAIT_QC.getCode().equals(qcBillStatusEnum.getCode())) {
                details.add(overviewDetailDTO);
            }
            if (QcBillStatusEnum.FINISH_QC.getCode().equals(qcBillStatusEnum.getCode()) || QcBillStatusEnum.EXEMPTION.getCode().equals(qcBillStatusEnum.getCode())) {
                details.add(overviewDetailDTO);
                qcCount += count;
            }
        }
        resultDTO.setList(details);
        BigDecimal rate = MathUtil.divide(new BigDecimal(qcCount), new BigDecimal(totalCount)).multiply(MathUtil.BigDecimal_100);
        resultDTO.setCompletionRate(rate);
        return resultDTO;
    }

    @Override
    public QcEffectivenessDTO.ViewQcTrendDTO viewQcTrend(QcEffectivenessDTO.ViewQcTrendSearchParamDTO dto) {
        QcEffectivenessDTO.ViewQcTrendDTO result = new  QcEffectivenessDTO.ViewQcTrendDTO();
        //日期集合
        List<String> dateList = new LinkedList<>();
        //暂存数量集合
        List<Integer> waitSubmitQtyList = new LinkedList<>();
        //待质检数量集合
        List<Integer> waitQcQtyList = new LinkedList<>();
        //质检（已质检、免检）数量集合
        List<Integer> qcQtyList = new LinkedList<>();
        //已取消数量集合
        List<Integer> cancelQtyList = new LinkedList<>();

        //是否是时间段，非时间段则默认取最近15（日，周，月）数据，是时间段则按时间段取质检数据
        Boolean isTimeSlot = dto.getIsTimeSlot();
        LocalDate beginDate = dto.getDateList().get(0);
        LocalDate endDate = dto.getDateList().get(1);

        //按日查询
        if (ViewQcTrendEnum.DAY.getCode().equals(dto.getType())) {
            if (!isTimeSlot) {
                beginDate = endDate.minusDays(15);
            }
        }
        //按周查询
        if (ViewQcTrendEnum.WEEK.getCode().equals(dto.getType())) {
            if (!isTimeSlot) {
                beginDate = endDate.minusWeeks(15);
            }
        }
        //按月查询
        if (ViewQcTrendEnum.MONTH.getCode().equals(dto.getType())) {
            if (!isTimeSlot) {
                beginDate = endDate.minusMonths(15);
            }
        }
        List<QcEffectivenessDTO.GroupQcTrendDTO> list =  qcInfoMapper.listQcBillGroupQcTrend(dto,beginDate,endDate);
        if (CollectionUtils.isNotEmpty(list)) {
            Map<String, List<QcEffectivenessDTO.GroupQcTrendDTO>> map = list.stream().collect(Collectors.groupingBy(QcEffectivenessDTO.GroupQcTrendDTO::getDateStr));
            List<Map.Entry<String, List<QcEffectivenessDTO.GroupQcTrendDTO>>> mapList = map.entrySet().stream().sorted(Comparator.comparing(obj -> obj.getKey())).collect(Collectors.toList());
            for (Map.Entry<String, List<QcEffectivenessDTO.GroupQcTrendDTO>> entry : mapList) {
                String key = entry.getKey();
                List<QcEffectivenessDTO.GroupQcTrendDTO> value = entry.getValue();
                if (ViewQcTrendEnum.WEEK.getCode().equals(dto.getType())) {
                    //周开始日期
                    LocalDate weekBegin = LocalDate.parse(key);
                    //周结束日期
                    LocalDate weekEnd = weekBegin.plusDays(6L);

                    key =  weekBegin.format(DateTimeFormatter.ofPattern("MM-dd")) + "~" + weekEnd.format(DateTimeFormatter.ofPattern("MM-dd"));
                }
                dateList.add(key);
                //暂存
                Integer waitSubmitQty= value.stream().filter(obj -> QcBillStatusEnum.DRAFT.getCode().equals(obj.getStatus())).map(QcEffectivenessDTO.GroupQcTrendDTO::getCount).reduce(MathUtil.ZERO,Integer::sum);
                waitSubmitQtyList.add(waitSubmitQty);
                //待质检
                Integer waitQcQty = value.stream().filter(obj -> QcBillStatusEnum.WAIT_QC.getCode().equals(obj.getStatus())).map(QcEffectivenessDTO.GroupQcTrendDTO::getCount).reduce(MathUtil.ZERO,Integer::sum);
                waitQcQtyList.add(waitQcQty);
                //已质检、免检
                Integer qcQty = value.stream().filter(obj -> QcBillStatusEnum.EXEMPTION.getCode().equals(obj.getStatus()) || QcBillStatusEnum.FINISH_QC.getCode().equals(obj.getStatus())).map(QcEffectivenessDTO.GroupQcTrendDTO::getCount).reduce(MathUtil.ZERO,Integer::sum);
                qcQtyList.add(qcQty);
                //已取消
                Integer cancelQty= value.stream().filter(obj -> QcBillStatusEnum.CANCEL.getCode().equals(obj.getStatus())).map(QcEffectivenessDTO.GroupQcTrendDTO::getCount).reduce(MathUtil.ZERO,Integer::sum);
                cancelQtyList.add(cancelQty);
            }
        }
        result.setDateList(dateList);
        result.setWaitSubmitQtyList(waitSubmitQtyList);
        result.setWaitQcQtyList(waitQcQtyList);
        result.setQcQtyList(qcQtyList);
        result.setCancelQtyList(cancelQtyList);
        return result;
    }

    @Override
    public PagingVO<QcEffectivenessDTO.ViewQcForPersonnelDTO> viewQcForPersonnel(PagingDTO<QcEffectivenessDTO.CommonSearchParamDTO> pagingDTO) {
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        IPage<QcEffectivenessDTO.ViewQcForPersonnelDTO> pageData = this.qcInfoMapper.viewQcForPersonnel(query, pagingDTO.getParams());
        return new PagingVO(pageData);
    }

    @Override
    public PagingVO<QcEffectivenessDTO.ViewQcForDocumentDTO> viewQcForDocument(PagingDTO<QcEffectivenessDTO.ViewQcForDocumentSearchParamDTO> pagingDTO) {
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        IPage<QcEffectivenessDTO.ViewQcForDocumentDTO> pageData = this.qcInfoMapper.viewQcForDocument(query, pagingDTO.getParams());
        List<QcEffectivenessDTO.ViewQcForDocumentDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //格式化数据
        doOpHandleQcForDocument(records);
        return new PagingVO(pageData);
    }

    @Override
    public Boolean exportExcel(QcEffectivenessDTO.ExportExcelSearchParamDTO dto, HttpServletResponse response) {
        //导出类型
        String type = dto.getType();
        String fileName = "";
        Class<?> clazz = null;
        List<?> list = null;
        if (QcReportExportExcelTypeEnum.PERSONNEL.getCode().equals(type)) {
             list =  this.qcInfoMapper.viewExportQcForPersonnel(dto);
             fileName = "按人员导出";
             clazz = ExportQcPersonnelExcelDTO.class;
             list =  BeanMapperUtils.copyList(ExportQcPersonnelExcelDTO.class,list);
        }
        if (QcReportExportExcelTypeEnum.DOCUMENT.getCode().equals(type)) {
             list =  this.qcInfoMapper.viewExportQcForDocument(dto);
             fileName = "按单据导出";
             clazz = ExportQcDocumentExcelDTO.class;
             doOpHandleQcForDocument((List<QcEffectivenessDTO.ViewQcForDocumentDTO>) list);
            list =  BeanMapperUtils.copyList(ExportQcDocumentExcelDTO.class,list);
        }
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        try {
            ExcelUtil.export(fileName, fileName, list, clazz, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    /**
     * @description: 按单据查询格式化数据
     * @author Will
     * @date: 2023/4/19 16:56
     * @param records
     */
    private void doOpHandleQcForDocument (List<QcEffectivenessDTO.ViewQcForDocumentDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> skuIds = records.stream().map(QcEffectivenessDTO.ViewQcForDocumentDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);
        for (QcEffectivenessDTO.ViewQcForDocumentDTO documentDTO : records) {
            //质检状态
            documentDTO.setQcStatusName(QcBillStatusEnum.getByCode(documentDTO.getQcStatus()).getName());

            //产品名称
            if (CollectionUtils.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(obj -> obj.getId().equals(documentDTO.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                documentDTO.setProductName(productName);
            }
            //订单收货时间
            WarehouseReceiveEntity warehouseReceiveEntity = warehouseReceiveService.getById(documentDTO.getSourceId());
            if (ObjectUtils.isNotEmpty(warehouseReceiveEntity)) {

                 LocalDateTime approveTime = warehouseReceiveEntity.getApproveTime();

                //质检耗时
                LocalDateTime nowTime = LocalDateTime.now();
                if (ObjectUtils.isNotEmpty(documentDTO.getQcEndTime())) {
                    nowTime = documentDTO.getQcEndTime();
                }
                Duration userTime = Duration.between(approveTime, nowTime);
                //分钟
                long userMinutes = userTime.toMinutes();

                documentDTO.setQcUseTime(MathUtil.divide(BigDecimal.valueOf(userMinutes),new BigDecimal(60),2).stripTrailingZeros().toPlainString()+ "H");

                //质检预警
                if (QcBillStatusEnum.WAIT_QC.getCode().equals(documentDTO.getQcStatus())) {
                    Duration between = Duration.between(approveTime,nowTime);
                    long hours = between.toHours();
                    if (hours >= 24L) {
                        documentDTO.setWarnRemark("已超时24H");
                    }
                    if (hours >= 48L) {
                        documentDTO.setWarnRemark("已超时48H");
                    }
                    if (hours >= 72L) {
                        documentDTO.setWarnRemark("已超时72H");
                    }
                }
            }
        }
    }
}
