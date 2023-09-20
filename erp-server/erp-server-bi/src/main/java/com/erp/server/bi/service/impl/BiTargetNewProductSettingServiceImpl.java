package com.erp.server.bi.service.impl;


import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiTargetSkuSettingDTO;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.excel.TargetNewProductSettingImportExcelDTO;
import com.erp.model.bi.dto.excel.TargetSkuSettingImportExcelDTO;
import com.erp.model.bi.entity.BiTargetNewProductSettingEntity;

import com.erp.model.bi.entity.BiTargetStaffSettingEntity;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.listener.BiTargetNewProductSettingExcelListener;
import com.erp.server.bi.listener.BiTargetSkuSettingExcelListener;
import com.erp.server.bi.mapper.BiTargetNewProductSettingMapper;
import com.erp.server.bi.service.BiTargetNewProductSettingService;
import com.common.core.exception.ServiceException;
import com.erp.server.bi.service.BiTargetYearService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetNewProductSettingDTO;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 新品目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetNewProductSettingServiceImpl extends SuperServiceImpl<BiTargetNewProductSettingMapper, BiTargetNewProductSettingEntity> implements BiTargetNewProductSettingService {


    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private BiTargetYearService biTargetYearService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetNewProductSettingDTO.AddDTO addDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, targetYear);
        List<String> metricsList = addDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetNewProductSettingDTO.CommonDTO> detailList = addDTO.getDetailList();
        // 数据处理
        handleData(targetYear, detailList);
        boolean save = biTargetYearService.save(targetYear);
        if (!save) {
            throw new ServiceException("新品目标设置单保存失败");
        }
        //添加明细
        this.batchAdd(targetYear.getId(), detailList);
        return targetYear.getId();
    }

    /**
     * 添加明细
     *
     * @param mainId
     * @param detailList
     */
    public void batchAdd(String mainId, List<BiTargetNewProductSettingDTO.CommonDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<BiTargetNewProductSettingEntity> addList = new ArrayList<>(20);
        for (BiTargetNewProductSettingDTO.CommonDTO item : detailList) {
            List<BiTargetNewProductSettingEntity> list = listAdd(item);
            addList.addAll(list);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            List<String> staffIdList = addList.stream().map(BiTargetNewProductSettingEntity::getStaffId).
                    collect(Collectors.toList());
            //用户信息
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(staffIdList);
            for (BiTargetNewProductSettingEntity item : addList) {
                item.setMainId(mainId);
                String staffName = userList.stream().filter(u -> u.getUserId().equals(item.getStaffId())).
                        findFirst().map(FindUserDTO::getUserName).orElse("");
                item.setStaffName(staffName);
            }
        }
        this.saveBatch(addList);

    }


    /**
     * 获取到添加的数据
     *
     * @param item
     * @return
     */
    private List<BiTargetNewProductSettingEntity> listAdd(BiTargetNewProductSettingDTO.CommonDTO item) {
        List<BiTargetNewProductSettingEntity> addList = new ArrayList<>(24);
        String staffId = item.getStaffId();
        //指标
        MetricsEnum metrics = item.getMetrics();
        //一月值
        BigDecimal january = item.getJanuary();
        BigDecimal januaryRate = item.getJanuaryRate();
        //一月值
        if (Objects.nonNull(january) || Objects.nonNull(januaryRate)) {
            addList.add(putEntity(staffId, item.getJanuary(), januaryRate, metrics, MonthEnum.JANUARY.getValue()));
        }
        //二月值
        BigDecimal february = item.getFebruary();
        BigDecimal februaryRate = item.getFebruaryRate();
        if (Objects.nonNull(february) || Objects.nonNull(februaryRate)) {
            addList.add(putEntity(staffId, february, februaryRate, metrics, MonthEnum.FEBRUARY.getValue()));
        }
        //三月值
        BigDecimal march = item.getMarch();
        BigDecimal marchRate = item.getMarchRate();
        if (Objects.nonNull(march) || Objects.nonNull(marchRate)) {
            addList.add(putEntity(staffId, march, marchRate, metrics, MonthEnum.MARCH.getValue()));
        }
        //四月值
        BigDecimal april = item.getApril();
        BigDecimal aprilRate = item.getAprilRate();
        if (Objects.nonNull(april) || Objects.nonNull(aprilRate)) {
            addList.add(putEntity(staffId, april, aprilRate, metrics, MonthEnum.APRIL.getValue()));
        }
        //五月值
        BigDecimal may = item.getMay();
        BigDecimal mayRate = item.getMayRate();
        if (Objects.nonNull(may) || Objects.nonNull(mayRate)) {
            addList.add(putEntity(staffId, may, mayRate, metrics, MonthEnum.MAY.getValue()));
        }
        //六月值
        BigDecimal june = item.getJune();
        BigDecimal juneRate = item.getJuneRate();
        if (Objects.nonNull(june) || Objects.nonNull(juneRate)) {
            addList.add(putEntity(staffId, june, juneRate, metrics, MonthEnum.JUNE.getValue()));
        }
        //七月值
        BigDecimal july = item.getJuly();
        BigDecimal julyRate = item.getJulyRate();
        if (Objects.nonNull(july) || Objects.nonNull(julyRate)) {
            addList.add(putEntity(staffId, july, julyRate, metrics, MonthEnum.JULY.getValue()));
        }
        //八月值
        BigDecimal august = item.getAugust();
        BigDecimal augustRate = item.getAugustRate();
        if (Objects.nonNull(august) || Objects.nonNull(augustRate)) {
            addList.add(putEntity(staffId, august, augustRate, metrics, MonthEnum.AUGUST.getValue()));
        }
        //九月值
        BigDecimal september = item.getSeptember();
        BigDecimal septemberRate = item.getSeptemberRate();
        if (Objects.nonNull(september) || Objects.nonNull(septemberRate)) {
            addList.add(putEntity(staffId, september, septemberRate, metrics, MonthEnum.SEPTEMBER.getValue()));
        }
        //十月值
        BigDecimal october = item.getOctober();
        BigDecimal octoberRate = item.getOctoberRate();
        if (Objects.nonNull(october) || Objects.nonNull(octoberRate)) {
            addList.add(putEntity(staffId, october, octoberRate, metrics, MonthEnum.OCTOBER.getValue()));
        }
        //十一月值
        BigDecimal november = item.getNovember();
        BigDecimal novemberRate = item.getNovemberRate();
        if (Objects.nonNull(november) || Objects.nonNull(novemberRate)) {
            addList.add(putEntity(staffId, november, novemberRate, metrics, MonthEnum.NOVEMBER.getValue()));
        }
        //十二月值
        BigDecimal december = item.getDecember();
        BigDecimal decemberRate = item.getDecemberRate();
        if (Objects.nonNull(december) || Objects.nonNull(decemberRate)) {
            addList.add(putEntity(staffId, december, decemberRate, metrics, MonthEnum.DECEMBER.getValue()));
        }
        return addList;

    }

    /**
     * 填充数据
     *
     * @param staffId
     * @param value
     * @param rate
     * @param metrics
     * @param month
     * @return
     */
    private BiTargetNewProductSettingEntity putEntity(String staffId, BigDecimal value, BigDecimal rate, MetricsEnum metrics, Integer month) {
        BiTargetNewProductSettingEntity entity = new BiTargetNewProductSettingEntity();
        entity.setMonth(month);
        entity.setStaffId(staffId);
        entity.setValue(value);
        entity.setMetrics(metrics);
        if (Objects.nonNull(rate)) {
            BigDecimal dbRate = MathUtil.divide(rate, MathUtil.BigDecimal_100);
            entity.setRate(dbRate);
        }
        return entity;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetNewProductSettingDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        BiTargetYearEntity oldTargetYear = biTargetYearService.getById(id);
        Optional.ofNullable(oldTargetYear).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "新品目标设置"));
        BiTargetYearEntity targetYear = BeanMapperUtils.map(BiTargetYearEntity.class, updateDTO);
        List<String> metricsList = updateDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetNewProductSettingDTO.CommonDTO> detailList = updateDTO.getDetailList();
        handleData(targetYear, detailList);
        boolean result = biTargetYearService.updateById(targetYear);
        if (!result) {
            throw new ServiceException("新品目标设置单保存失败");
        }
        this.removeByMainId(id);
        this.batchAdd(id, detailList);
        return Boolean.TRUE;
    }

    /**
     * 删除
     *
     * @param mainId
     */
    public void removeByMainId(String mainId) {
        this.lambdaUpdate().eq(BiTargetNewProductSettingEntity::getMainId, mainId).remove();
    }
    /**
     * 详情
     *
     * @param id
     * @return com.erp.model.bi.dto.BiTargetNewProductSettingDTO.ViewDTO
     * @author yl
     * @date 2023-09-15 11:26
     */
    @Override
    public BiTargetNewProductSettingDTO.ViewDTO view(String id) {
        BiTargetYearEntity targetYear = biTargetYearService.getById(id);
        if (Objects.isNull(targetYear)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "新品目标设置");
        }
        BiTargetNewProductSettingDTO.ViewDTO view = new BiTargetNewProductSettingDTO.ViewDTO();
        BeanMapperUtils.copy(targetYear, view);
        String metrics = targetYear.getMetrics();
        view.setMetricsList(Arrays.asList(metrics.split(",")));
        List<BiTargetNewProductSettingEntity> newProductSettingDbList = this.listBaseByMainIdList(Arrays.asList(id));
        //根据指标分组
        Map<MetricsEnum, List<BiTargetNewProductSettingEntity>> map = newProductSettingDbList.stream().
                collect(Collectors.groupingBy(BiTargetNewProductSettingEntity::getMetrics));
        //详情
        List<BiTargetNewProductSettingDTO.DetailDTO> detailList = new ArrayList<>(map.size());
        String valueStr = "";
        String rateStr = "";
        for (Map.Entry<MetricsEnum, List<BiTargetNewProductSettingEntity>> item : map.entrySet()) {
            BiTargetNewProductSettingDTO.DetailDTO detail = new BiTargetNewProductSettingDTO.DetailDTO();
            MetricsEnum metricsEnum = item.getKey();
            detail.setMetrics(metricsEnum);
            detail.setMetricsName(metricsEnum.getName());
            List<BiTargetNewProductSettingEntity> productSettingList = item.getValue();
            //根据人分组
            Map<String, List<BiTargetNewProductSettingEntity>> staffMap = productSettingList.stream().
                    collect(Collectors.groupingBy(BiTargetNewProductSettingEntity::getStaffId));
            List<BiTargetNewProductSettingDTO.CommonDTO> staffList = new ArrayList<>(staffMap.size());
            for (Map.Entry<String, List<BiTargetNewProductSettingEntity>> staff : staffMap.entrySet()) {
                String staffId = staff.getKey();
                List<BiTargetNewProductSettingEntity> dbList = staff.getValue();
                BiTargetNewProductSettingDTO.CommonDTO common = new BiTargetNewProductSettingDTO.CommonDTO();
                common.setStaffId(staffId);
                common.setStaffName(dbList.get(0).getStaffName());
                //一月
                Integer january = MonthEnum.JANUARY.getValue();
                common.setJanuary(pullView(metrics, january, dbList, valueStr));
                common.setJanuaryRate(pullView(metrics, january, dbList,rateStr));

                //二月
                Integer february = MonthEnum.FEBRUARY.getValue();
                common.setFebruary(pullView(metrics, february, dbList,valueStr));
                common.setFebruaryRate(pullView(metrics, february, dbList,rateStr));

                //三月
                Integer march = MonthEnum.MARCH.getValue();
                common.setMarch(pullView(metrics, march, dbList,valueStr));
                common.setMarchRate(pullView(metrics, march, dbList,rateStr));
                //四月
                Integer april = MonthEnum.APRIL.getValue();
                common.setApril(pullView(metrics, april, dbList,valueStr));
                common.setAprilRate(pullView(metrics, april, dbList,rateStr));
                //五月
                Integer may = MonthEnum.MAY.getValue();
                common.setMay(pullView(metrics, may, dbList,valueStr));
                common.setMayRate(pullView(metrics, may, dbList,rateStr));
                //六月
                Integer june = MonthEnum.JUNE.getValue();
                common.setJune(pullView(metrics, june, dbList,valueStr));
                common.setJuneRate(pullView(metrics, june, dbList,rateStr));
                //七月
                Integer july = MonthEnum.JULY.getValue();
                common.setJuly(pullView(metrics, july, dbList,valueStr));
                common.setJulyRate(pullView(metrics, july, dbList,rateStr));
                //八月
                Integer august = MonthEnum.AUGUST.getValue();
                common.setAugust(pullView(metrics, august, dbList,valueStr));
                common.setAugustRate(pullView(metrics, august, dbList,rateStr));
                //九月
                Integer september = MonthEnum.SEPTEMBER.getValue();
                common.setSeptember(pullView(metrics, september, dbList,valueStr));
                common.setSeptemberRate(pullView(metrics, september, dbList,rateStr));
                //十月
                Integer october = MonthEnum.OCTOBER.getValue();
                common.setOctober(pullView(metrics, october, dbList,valueStr));
                common.setOctoberRate(pullView(metrics, october, dbList,rateStr));

                //十一月
                Integer november = MonthEnum.NOVEMBER.getValue();
                common.setNovember(pullView(metrics, november, dbList,valueStr));
                common.setNovemberRate(pullView(metrics, november, dbList,rateStr));

                //十二月
                Integer december = MonthEnum.DECEMBER.getValue();
                common.setDecember(pullView(metrics, december, dbList,valueStr));
                common.setDecemberRate(pullView(metrics, december, dbList,rateStr));
                staffList.add(common);
            }
            detail.setNewProductSettingList(staffList);
            detailList.add(detail);
        }
        view.setDetailList(detailList);
        return view;
    }

    /**
     * 分页展示
     * @author yl
     * @date 2023-09-15 12:07
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.bi.dto.BiTargetNewProductSettingDTO.PagingViewDTO>
     */
    @Override
    public PagingVO<BiTargetNewProductSettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        BiTargetYearDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<BiTargetNewProductSettingDTO.PagingViewDTO> list = pageData.getRecords();
        pullPaging(list);
        return new PagingVO<>(pageData);
    }


    /**
     * 下载模板
     * @param response
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/TargetNewProductSetting.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error(" downloadTemplate  出错了 e=={}", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }

    }


    /**
     * 导入
     * @param excelFile
     * @param response
     * @return
     */
    @Override
    public BiTargetNewProductSettingDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        List<String> metricsNameList = MetricsEnum.listName();
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        BiTargetNewProductSettingExcelListener excelListenerUtil = new BiTargetNewProductSettingExcelListener(metricsNameList, userList);
        try {
            EasyExcel.read(excelFile.getInputStream(), TargetNewProductSettingImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("新品目标设置 导入错误>>>{}", e);
        }
        BiTargetNewProductSettingDTO.ImportDTO result = new BiTargetNewProductSettingDTO.ImportDTO();
        List<BiTargetNewProductSettingDTO.CommonDTO> successList = excelListenerUtil.getSuccessList();
        Map<MetricsEnum, List<BiTargetNewProductSettingDTO.CommonDTO>> map = successList.stream().
                collect(Collectors.groupingBy(BiTargetNewProductSettingDTO.CommonDTO::getMetrics));

        List<BiTargetNewProductSettingDTO.DetailDTO> detailList = new ArrayList<>(map.size());
        for (Map.Entry<MetricsEnum, List<BiTargetNewProductSettingDTO.CommonDTO>> item : map.entrySet()) {
            BiTargetNewProductSettingDTO.DetailDTO detail = new BiTargetNewProductSettingDTO.DetailDTO();
            detail.setMetrics(item.getKey());
            detail.setMetricsName(item.getKey().getName());
            detail.setNewProductSettingList(item.getValue());
            detailList.add(detail);
        }
        result.setSuccessList(detailList);
        List<TargetNewProductSettingImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "单品设置错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, TargetNewProductSettingImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }

    /**
     * 列表删除
     * @param dto
     * @return
     */
    @Override
    public Boolean delete(BiTargetNewProductSettingDTO.RemoveDTO dto) {
        Boolean result = this.lambdaUpdate().
                eq(BiTargetNewProductSettingEntity::getStaffId, dto.getStaffId()).
                eq(BiTargetNewProductSettingEntity::getMainId,dto.getId()).
                eq(BiTargetNewProductSettingEntity::getMetrics,dto.getMetricsEnum()).
                remove();
        return result;
    }

    /**
     * 填充分页数据
     * @param list
     */
    private void pullPaging(List<BiTargetNewProductSettingDTO.PagingViewDTO> list) {

        List<String> mainIdList = list.stream().map(BiTargetNewProductSettingDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<BiTargetNewProductSettingEntity> staffSettingDbList = this.listBaseByMainIdList(mainIdList);
        for (BiTargetNewProductSettingDTO.PagingViewDTO item : list) {
            List<BiTargetNewProductSettingEntity> dbList = staffSettingDbList.stream().
                    filter(s -> s.getMainId().equals(item.getId())).collect(Collectors.toList());
            List<BiTargetNewProductSettingDTO.CommonDTO> commonList = getCommon(dbList);
            item.setDetailList(commonList);
        }
    }

    private List<BiTargetNewProductSettingDTO.CommonDTO> getCommon(List<BiTargetNewProductSettingEntity> dbList) {
        List<BiTargetNewProductSettingDTO.CommonDTO> resultList = new ArrayList<>(10);
        String valueStr = "";
        String rateStr = "";
        //根据指标分组
        Map<MetricsEnum, List<BiTargetNewProductSettingEntity>> map = dbList.stream().
                collect(Collectors.groupingBy(BiTargetNewProductSettingEntity::getMetrics));

        for (Map.Entry<MetricsEnum, List<BiTargetNewProductSettingEntity>> item : map.entrySet()) {
            MetricsEnum metricsEnum = item.getKey();
            String metrics = metricsEnum.getCode();
            List<BiTargetNewProductSettingEntity> staffSettingList = item.getValue();
            //根据人分组
            Map<String, List<BiTargetNewProductSettingEntity>> staffMap = staffSettingList.stream().
                    collect(Collectors.groupingBy(BiTargetNewProductSettingEntity::getStaffId));
            for (Map.Entry<String, List<BiTargetNewProductSettingEntity>> staff : staffMap.entrySet()) {
                String staffId = staff.getKey();
                List<BiTargetNewProductSettingEntity> staffDbList = staff.getValue();
                BiTargetNewProductSettingDTO.CommonDTO common = new BiTargetNewProductSettingDTO.CommonDTO();
                common.setStaffId(staffId);
                common.setStaffName(dbList.get(0).getStaffName());
                //一月
                Integer january = MonthEnum.JANUARY.getValue();
                common.setJanuary(pullView(metrics, january, dbList, valueStr));
                common.setJanuaryRate(pullView(metrics, january, dbList,rateStr));

                //二月
                Integer february = MonthEnum.FEBRUARY.getValue();
                common.setFebruary(pullView(metrics, february, dbList,valueStr));
                common.setFebruaryRate(pullView(metrics, february, dbList,rateStr));

                //三月
                Integer march = MonthEnum.MARCH.getValue();
                common.setMarch(pullView(metrics, march, dbList,valueStr));
                common.setMarchRate(pullView(metrics, march, dbList,rateStr));
                //四月
                Integer april = MonthEnum.APRIL.getValue();
                common.setApril(pullView(metrics, april, dbList,valueStr));
                common.setAprilRate(pullView(metrics, april, dbList,rateStr));
                //五月
                Integer may = MonthEnum.MAY.getValue();
                common.setMay(pullView(metrics, may, dbList,valueStr));
                common.setMayRate(pullView(metrics, may, dbList,rateStr));
                //六月
                Integer june = MonthEnum.JUNE.getValue();
                common.setJune(pullView(metrics, june, dbList,valueStr));
                common.setJuneRate(pullView(metrics, june, dbList,rateStr));
                //七月
                Integer july = MonthEnum.JULY.getValue();
                common.setJuly(pullView(metrics, july, dbList,valueStr));
                common.setJulyRate(pullView(metrics, july, dbList,rateStr));
                //八月
                Integer august = MonthEnum.AUGUST.getValue();
                common.setAugust(pullView(metrics, august, dbList,valueStr));
                common.setAugustRate(pullView(metrics, august, dbList,rateStr));
                //九月
                Integer september = MonthEnum.SEPTEMBER.getValue();
                common.setSeptember(pullView(metrics, september, dbList,valueStr));
                common.setSeptemberRate(pullView(metrics, september, dbList,rateStr));
                //十月
                Integer october = MonthEnum.OCTOBER.getValue();
                common.setOctober(pullView(metrics, october, dbList,valueStr));
                common.setOctoberRate(pullView(metrics, october, dbList,rateStr));

                //十一月
                Integer november = MonthEnum.NOVEMBER.getValue();
                common.setNovember(pullView(metrics, november, dbList,valueStr));
                common.setNovemberRate(pullView(metrics, november, dbList,rateStr));

                //十二月
                Integer december = MonthEnum.DECEMBER.getValue();
                common.setDecember(pullView(metrics, december, dbList,valueStr));
                common.setDecemberRate(pullView(metrics, december, dbList,rateStr));

                common.setMetrics(metricsEnum);
                common.setMetricsName(metricsEnum.getName());
                resultList.add(common);
            }

        }

        return resultList;
    }

    /**
     * 填充显示的数据
     */
    private BigDecimal pullView(String metrics, Integer month, List<BiTargetNewProductSettingEntity> dbList, String flagStr) {
        if ("value".equals(flagStr)) {
            BigDecimal value = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                    map(BiTargetNewProductSettingEntity::getValue).orElse(null);
            return value;
        } else {
            BigDecimal rate = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                    map(BiTargetNewProductSettingEntity::getRate).orElse(null);
            return Objects.nonNull(rate) ? MathUtil.multiply(rate, MathUtil.BigDecimal_100) : rate;
        }
    }

    /**
     * 根据主表查询数据
     *
     * @param mainIdList
     * @return
     */
    public List<BiTargetNewProductSettingEntity> listBaseByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(BiTargetNewProductSettingEntity::getMainId, mainIdList).
                orderByDesc(BiTargetNewProductSettingEntity::getId).list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(BiTargetYearEntity targetYear, List<BiTargetNewProductSettingDTO.CommonDTO> detailList) {
        //部门id
        String deptId = targetYear.getDeptId();
        //币种符号
        String currency = targetYear.getCurrency();
        SysDepartmentDTO department = sysUserFeign.getUserDeptById(deptId);
        if (Objects.nonNull(department)) {
            targetYear.setDeptName(department.getName());
        }
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(currency));
        if (CollectionUtils.isNotEmpty(currencyList)) {
            targetYear.setCurrencySymbol(currencyList.get(0).getSymbol());
        }
    }

    @Override
    public List<BiTargetNewProductSettingDTO.DeptTargetDTO> listDeptTarget(BiTargetNewProductSettingDTO.TargetParamDTO dto) {
        return baseMapper.listDeptTarget(dto);
    }

    @Override
    public List<BiTargetNewProductSettingDTO.UserTargetDTO> listUserTarget(BiTargetNewProductSettingDTO.TargetParamDTO dto) {
        return baseMapper.listUserTarget(dto);
    }
}
