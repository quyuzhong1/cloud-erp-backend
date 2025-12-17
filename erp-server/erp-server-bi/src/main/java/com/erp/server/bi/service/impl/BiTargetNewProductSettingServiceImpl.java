package com.erp.server.bi.service.impl;


import static com.alibaba.excel.EasyExcel.read;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.dto.excel.TargetNewProductSettingImportExcelDTO;
import com.erp.model.bi.entity.BiTargetNewProductSettingEntity;

import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.listener.BiTargetNewProductSettingExcelListener;
import com.erp.server.bi.mapper.BiTargetNewProductSettingMapper;
import com.erp.server.bi.service.BiTargetNewProductSettingService;
import com.common.core.exception.ServiceException;
import com.erp.server.bi.service.BiTargetYearService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetNewProductSettingDTO.AddDTO addDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, targetYear);
        List<String> metricsList = addDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetNewProductSettingDTO.CommonDTO> detailList = addDTO.getDetailList();
        // 数据处理
        handleData(targetYear, detailList, LogActionEnum.INSERT);
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
            this.saveBatch(addList);
        }


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
        MetricsEnum metrics = item.getMetrics();

        // 将月份及对应值存入数组
        MonthEnum[] months = MonthEnum.values();
        BigDecimal[] values = {
                item.getJanuary(), item.getFebruary(), item.getMarch(), item.getApril(),
                item.getMay(), item.getJune(), item.getJuly(), item.getAugust(),
                item.getSeptember(), item.getOctober(), item.getNovember(), item.getDecember()
        };
        BigDecimal[] rates = {
                item.getJanuaryRate(), item.getFebruaryRate(), item.getMarchRate(), item.getAprilRate(),
                item.getMayRate(), item.getJuneRate(), item.getJulyRate(), item.getAugustRate(),
                item.getSeptemberRate(), item.getOctoberRate(), item.getNovemberRate(), item.getDecemberRate()
        };

        // 使用循环来简化每个月的处理
        for (int i = 0; i < months.length; i++) {
            if (Objects.nonNull(values[i]) || Objects.nonNull(rates[i])) {
                addList.add(putEntity(staffId, values[i], rates[i], metrics, months[i].getValue()));
            }
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
        if (MetricsEnum.GROSS_PROFIT_RATE.equals(metrics)) {
            entity.setValue(MathUtil.divide(value, MathUtil.BigDecimal_100, 2));
        }
        return entity;
    }

    /**
     * 修改
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
        handleData(targetYear, detailList, LogActionEnum.UPDATE);
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
        String valueStr = "value";
        String rateStr = "";
        for (Map.Entry<MetricsEnum, List<BiTargetNewProductSettingEntity>> item : map.entrySet()) {
            BiTargetNewProductSettingDTO.DetailDTO detail = new BiTargetNewProductSettingDTO.DetailDTO();
            MetricsEnum metricsEnum = item.getKey();
            String metricsFlag = metricsEnum.getCode();
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
                common.setJanuary(pullView(metricsFlag, january, dbList, valueStr));
                common.setJanuaryRate(pullView(metricsFlag, january, dbList, rateStr));

                //二月
                Integer february = MonthEnum.FEBRUARY.getValue();
                common.setFebruary(pullView(metricsFlag, february, dbList, valueStr));
                common.setFebruaryRate(pullView(metricsFlag, february, dbList, rateStr));

                //三月
                Integer march = MonthEnum.MARCH.getValue();
                common.setMarch(pullView(metricsFlag, march, dbList, valueStr));
                common.setMarchRate(pullView(metricsFlag, march, dbList, rateStr));
                //四月
                Integer april = MonthEnum.APRIL.getValue();
                common.setApril(pullView(metricsFlag, april, dbList, valueStr));
                common.setAprilRate(pullView(metricsFlag, april, dbList, rateStr));
                //五月
                Integer may = MonthEnum.MAY.getValue();
                common.setMay(pullView(metricsFlag, may, dbList, valueStr));
                common.setMayRate(pullView(metricsFlag, may, dbList, rateStr));
                //六月
                Integer june = MonthEnum.JUNE.getValue();
                common.setJune(pullView(metricsFlag, june, dbList, valueStr));
                common.setJuneRate(pullView(metricsFlag, june, dbList, rateStr));
                //七月
                Integer july = MonthEnum.JULY.getValue();
                common.setJuly(pullView(metricsFlag, july, dbList, valueStr));
                common.setJulyRate(pullView(metricsFlag, july, dbList, rateStr));
                //八月
                Integer august = MonthEnum.AUGUST.getValue();
                common.setAugust(pullView(metricsFlag, august, dbList, valueStr));
                common.setAugustRate(pullView(metricsFlag, august, dbList, rateStr));
                //九月
                Integer september = MonthEnum.SEPTEMBER.getValue();
                common.setSeptember(pullView(metricsFlag, september, dbList, valueStr));
                common.setSeptemberRate(pullView(metricsFlag, september, dbList, rateStr));
                //十月
                Integer october = MonthEnum.OCTOBER.getValue();
                common.setOctober(pullView(metricsFlag, october, dbList, valueStr));
                common.setOctoberRate(pullView(metricsFlag, october, dbList, rateStr));

                //十一月
                Integer november = MonthEnum.NOVEMBER.getValue();
                common.setNovember(pullView(metricsFlag, november, dbList, valueStr));
                common.setNovemberRate(pullView(metricsFlag, november, dbList, rateStr));

                //十二月
                Integer december = MonthEnum.DECEMBER.getValue();
                common.setDecember(pullView(metricsFlag, december, dbList, valueStr));
                common.setDecemberRate(pullView(metricsFlag, december, dbList, rateStr));
                staffList.add(common);
            }
            detail.setSettingList(staffList);
            detailList.add(detail);
        }
        view.setDetailList(detailList);
        return view;
    }

    /**
     * 分页展示
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.bi.dto.BiTargetNewProductSettingDTO.PagingViewDTO>
     * @author yl
     * @date 2023-09-15 12:07
     */
    @Override
    public PagingVO<BiTargetNewProductSettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        BiTargetYearDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        //乘的值
        BigDecimal multiplyNum = getMultiplyNum(params.getMetrics());
        IPage<BiTargetNewProductSettingDTO.PagingViewDTO> pageData = baseMapper.paging(query, params, multiplyNum);
        List<BiTargetNewProductSettingDTO.PagingViewDTO> list = pageData.getRecords();
        list.forEach(s -> s.setMetricsName(s.getMetrics().getName()));
        return new PagingVO<>(pageData);
    }

    /**
     * 获取到乘的值
     *
     * @return
     */
    private BigDecimal getMultiplyNum(String metrics) {
        //乘的值
        BigDecimal multiplyNum = MathUtil.BigDecimal_1;
        if (MetricsEnum.GROSS_PROFIT_RATE.getCode().equals(metrics)) {
            multiplyNum = MathUtil.BigDecimal_100;
        }
        return multiplyNum;
    }

    /**
     * 下载模板
     *
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
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
     *
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
            read(excelFile.getInputStream(), TargetNewProductSettingImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
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
            detail.setSettingList(item.getValue());
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
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean delete(BiTargetNewProductSettingDTO.RemoveDTO dto) {
        return this.lambdaUpdate().
                eq(BiTargetNewProductSettingEntity::getStaffId, dto.getStaffId()).
                eq(BiTargetNewProductSettingEntity::getMainId, dto.getId()).
                eq(BiTargetNewProductSettingEntity::getMetrics, dto.getMetrics()).
                remove();
    }

    /**
     * 分页统计
     *
     * @param dto
     * @return
     */

    @Override
    public BiTargetYearDTO.PagingTotalDTO pagingTotal(BiTargetYearDTO.PagingParamDTO dto) {
        //乘的值
        BigDecimal multiplyNum = getMultiplyNum(dto.getMetrics());
        return baseMapper.pagingTotal(dto, multiplyNum);
    }


    /**
     * 填充显示的数据
     */
    private BigDecimal pullView(String metrics, Integer month, List<BiTargetNewProductSettingEntity> dbList, String flagStr) {
        if ("value".equals(flagStr)) {
            BigDecimal value = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                    map(BiTargetNewProductSettingEntity::getValue).orElse(null);
            if (value != null && MetricsEnum.GROSS_PROFIT_RATE.getCode().equals(metrics)) {
                value = MathUtil.multiplyWithTwo(value, MathUtil.NUMBER_100);
            }
            return value;
        } else {
            BigDecimal rate = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                    map(BiTargetNewProductSettingEntity::getRate).orElse(null);
            return Objects.nonNull(rate) ? MathUtil.multiplyWithTwo(rate, MathUtil.BigDecimal_100) : rate;
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
    public void handleData(BiTargetYearEntity targetYear, List<BiTargetNewProductSettingDTO.CommonDTO> detailList, LogActionEnum action) {

        List<BiTargetNewProductSettingDTO.ListDetailDTO> existList = baseMapper.listByYearAndDept(targetYear.getYear(), targetYear.getDeptId());
        List<String> existStaffList = Lists.newArrayList();
        for (BiTargetNewProductSettingDTO.CommonDTO item : detailList) {
            String staffId = item.getStaffId();
            MetricsEnum metrics = item.getMetrics();

            //一月
            BigDecimal january = item.getJanuary();
            if (Objects.nonNull(january)) {
                Integer januaryMoth = MonthEnum.JANUARY.getValue();
                putListDetailDTO(existList, staffId, metrics, januaryMoth, existStaffList,action);
            }
            //二月
            BigDecimal february = item.getFebruary();
            if (Objects.nonNull(february)) {
                Integer februaryMoth = MonthEnum.FEBRUARY.getValue();
                putListDetailDTO(existList, staffId, metrics, februaryMoth, existStaffList,action);
            }
            //三月
            BigDecimal march = item.getMarch();
            if (Objects.nonNull(march)) {
                Integer marchMoth = MonthEnum.MARCH.getValue();
                putListDetailDTO(existList, staffId, metrics, marchMoth, existStaffList,action);
            }
            //四月
            BigDecimal april = item.getApril();
            if (Objects.nonNull(april)) {
                Integer aprilMoth = MonthEnum.APRIL.getValue();
                putListDetailDTO(existList, staffId, metrics, aprilMoth, existStaffList,action);
            }
            //五月
            BigDecimal may = item.getMay();
            if (Objects.nonNull(may)) {
                Integer mayMoth = MonthEnum.MAY.getValue();
                putListDetailDTO(existList, staffId, metrics, mayMoth, existStaffList,action);
            }
            //六月
            BigDecimal june = item.getJune();
            if (Objects.nonNull(june)) {
                Integer juneMoth = MonthEnum.JUNE.getValue();
                putListDetailDTO(existList, staffId, metrics, juneMoth, existStaffList,action);
            }
            //七月
            BigDecimal july = item.getJuly();
            if (Objects.nonNull(july)) {
                Integer julyMoth = MonthEnum.JULY.getValue();
                putListDetailDTO(existList, staffId, metrics, julyMoth, existStaffList,action);
            }
            //八月
            BigDecimal august = item.getAugust();
            if (Objects.nonNull(august)) {
                Integer augustMoth = MonthEnum.AUGUST.getValue();
                putListDetailDTO(existList, staffId, metrics, augustMoth, existStaffList,action);
            }
            //九月
            BigDecimal september = item.getSeptember();
            if (Objects.nonNull(september)) {
                Integer septemberMoth = MonthEnum.SEPTEMBER.getValue();
                putListDetailDTO(existList, staffId, metrics, septemberMoth, existStaffList,action);
            }
            //十月
            BigDecimal october = item.getOctober();
            if (Objects.nonNull(october)) {
                Integer octoberMoth = MonthEnum.OCTOBER.getValue();
                putListDetailDTO(existList, staffId, metrics, octoberMoth, existStaffList,action);
            }

            //十一月
            BigDecimal november = item.getNovember();
            if (Objects.nonNull(november)) {
                Integer novemberMoth = MonthEnum.NOVEMBER.getValue();
                putListDetailDTO(existList, staffId, metrics, novemberMoth, existStaffList,action);
            }

            //十二月
            BigDecimal december = item.getDecember();
            if (Objects.nonNull(december)) {
                Integer decemberMoth = MonthEnum.DECEMBER.getValue();
                putListDetailDTO(existList, staffId, metrics, decemberMoth, existStaffList,action);
            }


        }
        if (CollectionUtils.isNotEmpty(existStaffList)) {
            String existCategoryName = existStaffList.stream().distinct().collect(Collectors.joining(","));
            throw new ServiceException(ApiError.YEAR_METRICS_EXIST, existCategoryName);
        }
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

    private void putListDetailDTO(List<BiTargetNewProductSettingDTO.ListDetailDTO> existList, String staffId, MetricsEnum metrics,Integer month, List<String> existStaffList, LogActionEnum action) {
        //添加的
        if (LogActionEnum.INSERT.equals(action)) {
            BiTargetNewProductSettingDTO.ListDetailDTO exist = existList.stream().filter(e ->
                    e.getStaffId().equals(staffId) &&
                            e.getMetrics().equals(metrics)
            ).findFirst().orElse(null);
            if (exist != null) {
                existStaffList.add(exist.getStaffName());
            }
        } else {
            //修改的
            List<BiTargetNewProductSettingDTO.ListDetailDTO> list = existList.stream().filter(e ->
                    e.getStaffId().equals(staffId) &&
                            e.getMetrics().equals(metrics)&&
                            e.getMonth().equals(month)
            ).collect(Collectors.toList());
            if (list.size() > 1) {
                existStaffList.add(list.get(0).getStaffName());
            }
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
