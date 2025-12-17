package com.erp.server.bi.service.impl;


import static com.alibaba.excel.EasyExcel.read;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.dto.excel.TargetStaffSettingImportExcelDTO;
import com.erp.model.bi.entity.BiTargetStaffSettingEntity;

import com.common.core.exception.ServiceException;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.listener.BiTargetStaffSettingExcelListener;
import com.erp.server.bi.mapper.BiTargetStaffSettingMapper;
import com.erp.server.bi.service.BiTargetStaffSettingService;
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
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;

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
 * 人员目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetStaffSettingServiceImpl extends SuperServiceImpl<BiTargetStaffSettingMapper, BiTargetStaffSettingEntity> implements BiTargetStaffSettingService {

    @Autowired
    private BiTargetYearService biTargetYearService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetStaffSettingDTO.AddDTO addDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, targetYear);
        List<String> metricsList = addDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetStaffSettingDTO.CommonDTO> detailList = addDTO.getDetailList();
        // 数据处理
        handleData(targetYear, detailList, LogActionEnum.INSERT);
        if (!biTargetYearService.save(targetYear)) {
            throw new ServiceException("人员目标设置单保存失败");
        }
        //添加明细
        this.batchAdd(targetYear.getId(), detailList);
        return targetYear.getId();
    }

    /**
     * 添加明细的
     *
     * @param mainId
     * @param detailList
     */
    public void batchAdd(String mainId, List<BiTargetStaffSettingDTO.CommonDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<BiTargetStaffSettingEntity> addList = new ArrayList<>(10);
        for (BiTargetStaffSettingDTO.CommonDTO item : detailList) {
            List<BiTargetStaffSettingEntity> list = listAdd(item);
            addList.addAll(list);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            List<String> staffIdList = addList.stream().map(BiTargetStaffSettingEntity::getStaffId).
                    collect(Collectors.toList());
            //用户信息
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(staffIdList);
            for (BiTargetStaffSettingEntity item : addList) {
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
    private List<BiTargetStaffSettingEntity> listAdd(BiTargetStaffSettingDTO.CommonDTO item) {
        List<BiTargetStaffSettingEntity> addList = new ArrayList<>(12);
        String staffId = item.getStaffId();
        //指标
        MetricsEnum metrics = item.getMetrics();
        //一月值
        if (Objects.nonNull(item.getJanuary())) {
            addList.add(putEntity(staffId, item.getJanuary(), metrics, MonthEnum.JANUARY.getValue()));
        }
        //二月值
        BigDecimal february = item.getFebruary();
        if (Objects.nonNull(february)) {
            addList.add(putEntity(staffId, february, metrics, MonthEnum.FEBRUARY.getValue()));
        }
        //三月值
        BigDecimal march = item.getMarch();
        if (Objects.nonNull(march)) {
            addList.add(putEntity(staffId, march, metrics, MonthEnum.MARCH.getValue()));
        }
        //四月值
        BigDecimal april = item.getApril();
        if (Objects.nonNull(april)) {
            addList.add(putEntity(staffId, april, metrics, MonthEnum.APRIL.getValue()));
        }
        //五月值
        BigDecimal may = item.getMay();
        if (Objects.nonNull(may)) {
            addList.add(putEntity(staffId, may, metrics, MonthEnum.MAY.getValue()));
        }
        //六月值
        BigDecimal june = item.getJune();
        if (Objects.nonNull(june)) {
            addList.add(putEntity(staffId, june, metrics, MonthEnum.JUNE.getValue()));
        }
        //七月值
        BigDecimal july = item.getJuly();
        if (Objects.nonNull(july)) {
            addList.add(putEntity(staffId, july, metrics, MonthEnum.JULY.getValue()));
        }
        //八月值
        BigDecimal august = item.getAugust();
        if (Objects.nonNull(august)) {
            addList.add(putEntity(staffId, august, metrics, MonthEnum.AUGUST.getValue()));
        }
        //九月值
        BigDecimal september = item.getSeptember();
        if (Objects.nonNull(september)) {
            addList.add(putEntity(staffId, september, metrics, MonthEnum.SEPTEMBER.getValue()));
        }
        //十月值
        BigDecimal october = item.getOctober();
        if (Objects.nonNull(october)) {
            addList.add(putEntity(staffId, october, metrics, MonthEnum.OCTOBER.getValue()));
        }
        //十一月值
        BigDecimal november = item.getNovember();
        if (Objects.nonNull(november)) {
            addList.add(putEntity(staffId, november, metrics, MonthEnum.NOVEMBER.getValue()));
        }
        //十二月值
        BigDecimal december = item.getDecember();
        if (Objects.nonNull(december)) {
            addList.add(putEntity(staffId, december, metrics, MonthEnum.DECEMBER.getValue()));
        }
        return addList;

    }

    /**
     * 填充保存数据
     *
     * @param
     * @param month 月份
     * @return
     */
    private BiTargetStaffSettingEntity putEntity(String staffId, BigDecimal value, MetricsEnum metrics, Integer month) {
        BiTargetStaffSettingEntity entity = new BiTargetStaffSettingEntity();
        entity.setMonth(month);
        entity.setStaffId(staffId);
        entity.setValue(value);
        entity.setMetrics(metrics);
        if (MetricsEnum.GROSS_PROFIT_RATE.equals(metrics)) {
            entity.setValue(MathUtil.divide(value, MathUtil.BigDecimal_100, 4));
        }
        return entity;
    }


    /**
     * 修改人员目标设置
     *
     * @param mainId
     * @param detailList
     */
    public void batchUpdate(String mainId, List<BiTargetStaffSettingDTO.CommonDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        this.removeByMainId(mainId);
        this.batchAdd(mainId, detailList);


    }

    /**
     * 删除
     *
     * @param mainId
     */
    public void removeByMainId(String mainId) {
        this.lambdaUpdate().eq(BiTargetStaffSettingEntity::getMainId, mainId).remove();
    }


    /**
     * 根据主表id获取对应信息
     *
     * @param mainId
     * @return
     */
    public List<BiTargetStaffSettingEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(BiTargetStaffSettingEntity::getMainId, mainId).
                orderByDesc(BiTargetStaffSettingEntity::getId).list();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetStaffSettingDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        BiTargetYearEntity oldTargetYear = biTargetYearService.getById(id);
        Optional.ofNullable(oldTargetYear).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "人员目标设置"));
        BiTargetYearEntity targetYear = BeanMapperUtils.map(BiTargetYearEntity.class, updateDTO);
        List<String> metricsList = updateDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetStaffSettingDTO.CommonDTO> detailList = updateDTO.getDetailList();
        handleData(targetYear, detailList, LogActionEnum.UPDATE);
        if (!biTargetYearService.updateById(targetYear)) {
            throw new ServiceException("人员目标设置单保存失败");
        }
        this.batchUpdate(id, detailList);
        return Boolean.TRUE;
    }

    /**
     * 获取到详情信息
     *
     * @param id
     * @return com.erp.model.bi.dto.BiTargetStaffSettingDTO.ViewDTO
     * @author yl
     * @date 2023-09-13 15:17
     */
    @Override
    public BiTargetStaffSettingDTO.ViewDTO view(String id) {
        BiTargetStaffSettingDTO.ViewDTO view = new BiTargetStaffSettingDTO.ViewDTO();
        BiTargetYearEntity targetYear = biTargetYearService.getById(id);
        if (Objects.isNull(targetYear)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "人员目标设置");
        }
        BeanMapperUtils.copy(targetYear, view);
        String metrics = targetYear.getMetrics();
        view.setMetricsList(Arrays.asList(metrics.split(",")));
        List<BiTargetStaffSettingEntity> staffSettingDbList = this.listBaseByMainId(id);
        //根据指标分组
        Map<MetricsEnum, List<BiTargetStaffSettingEntity>> map = staffSettingDbList.stream().
                collect(Collectors.groupingBy(BiTargetStaffSettingEntity::getMetrics));
        //详情
        List<BiTargetStaffSettingDTO.DetailDTO> detailList = new ArrayList<>(map.size());
        for (Map.Entry<MetricsEnum, List<BiTargetStaffSettingEntity>> item : map.entrySet()) {
            BiTargetStaffSettingDTO.DetailDTO detail = new BiTargetStaffSettingDTO.DetailDTO();
            MetricsEnum metricsEnum = item.getKey();
            String metricsFlag = metricsEnum.getCode();
            detail.setMetrics(metricsEnum);
            detail.setMetricsName(metricsEnum.getName());
            List<BiTargetStaffSettingEntity> staffSettingList = item.getValue();
            //根据人分组
            Map<String, List<BiTargetStaffSettingEntity>> staffMap = staffSettingList.stream().
                    collect(Collectors.groupingBy(BiTargetStaffSettingEntity::getStaffId));
            List<BiTargetStaffSettingDTO.CommonDTO> staffList = new ArrayList<>(staffMap.size());
            for (Map.Entry<String, List<BiTargetStaffSettingEntity>> staff : staffMap.entrySet()) {
                String staffId = staff.getKey();
                List<BiTargetStaffSettingEntity> dbList = staff.getValue();
                BiTargetStaffSettingDTO.CommonDTO common = new BiTargetStaffSettingDTO.CommonDTO();
                common.setStaffId(staffId);
                common.setStaffName(dbList.get(0).getStaffName());
                //一月
                Integer january = MonthEnum.JANUARY.getValue();
                common.setJanuary(pullView(metricsFlag, january, dbList));
                //二月
                Integer february = MonthEnum.FEBRUARY.getValue();
                common.setFebruary(pullView(metricsFlag, february, dbList));
                //三月
                Integer march = MonthEnum.MARCH.getValue();
                common.setMarch(pullView(metricsFlag, march, dbList));
                //四月
                Integer april = MonthEnum.APRIL.getValue();
                common.setApril(pullView(metricsFlag, april, dbList));
                //五月
                Integer may = MonthEnum.MAY.getValue();
                common.setMay(pullView(metricsFlag, may, dbList));
                //六月
                Integer june = MonthEnum.JUNE.getValue();
                common.setJune(pullView(metricsFlag, june, dbList));
                //七月
                Integer july = MonthEnum.JULY.getValue();
                common.setJuly(pullView(metricsFlag, july, dbList));
                //八月
                Integer august = MonthEnum.AUGUST.getValue();
                common.setAugust(pullView(metricsFlag, august, dbList));
                //九月
                Integer september = MonthEnum.SEPTEMBER.getValue();
                common.setSeptember(pullView(metricsFlag, september, dbList));
                //十月
                Integer october = MonthEnum.OCTOBER.getValue();
                common.setOctober(pullView(metricsFlag, october, dbList));

                //十一月
                Integer november = MonthEnum.NOVEMBER.getValue();
                common.setNovember(pullView(metricsFlag, november, dbList));

                //十月
                Integer december = MonthEnum.DECEMBER.getValue();
                common.setDecember(pullView(metricsFlag, december, dbList));
                staffList.add(common);

            }
            detail.setSettingList(staffList);
            detailList.add(detail);

        }
        view.setDetailList(detailList);
        return view;
    }

    /**
     * 分页显示
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<BiTargetStaffSettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        BiTargetYearDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        //乘的值
        BigDecimal multiplyNum = getMultiplyNum(params.getMetrics());
        IPage<BiTargetStaffSettingDTO.PagingViewDTO> pageData = baseMapper.paging(query, params, multiplyNum);
        List<BiTargetStaffSettingDTO.PagingViewDTO> list = pageData.getRecords();
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

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/TargetStaffSetting.xlsx";
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
     * 导入人员目标设置
     *
     * @param excelFile
     * @param response
     * @return com.erp.model.bi.dto.BiTargetStaffSettingDTO.ImportDTO
     * @author yl
     * @date 2023-09-15 14:20
     */
    @Override
    public BiTargetStaffSettingDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        List<String> metricsNameList = MetricsEnum.listName();
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        BiTargetStaffSettingExcelListener excelListenerUtil = new BiTargetStaffSettingExcelListener(metricsNameList, userList);
        try {
            read(excelFile.getInputStream(), TargetStaffSettingImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("人员目标设置 导入错误>>>{}", e);
        }
        BiTargetStaffSettingDTO.ImportDTO result = new BiTargetStaffSettingDTO.ImportDTO();
        List<BiTargetStaffSettingDTO.CommonDTO> successList = excelListenerUtil.getSuccessList();

        Map<MetricsEnum, List<BiTargetStaffSettingDTO.CommonDTO>> map = successList.stream().
                collect(Collectors.groupingBy(BiTargetStaffSettingDTO.CommonDTO::getMetrics));

        List<BiTargetStaffSettingDTO.DetailDTO> detailList = new ArrayList<>(map.size());
        for (Map.Entry<MetricsEnum, List<BiTargetStaffSettingDTO.CommonDTO>> item : map.entrySet()) {
            BiTargetStaffSettingDTO.DetailDTO detail = new BiTargetStaffSettingDTO.DetailDTO();
            detail.setMetrics(item.getKey());
            detail.setMetricsName(item.getKey().getName());
            detail.setSettingList(item.getValue());
            detailList.add(detail);
        }
        result.setSuccessList(detailList);
        List<TargetStaffSettingImportExcelDTO> errorList = excelListenerUtil.getErrorList();

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "人员设置错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, TargetStaffSettingImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }

    /**
     * 列表删除员工目标
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean delete(BiTargetStaffSettingDTO.RemoveDTO dto) {
        return this.lambdaUpdate().
                eq(BiTargetStaffSettingEntity::getStaffId, dto.getStaffId()).
                eq(BiTargetStaffSettingEntity::getMainId, dto.getId()).
                eq(BiTargetStaffSettingEntity::getMetrics, dto.getMetrics()).
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


    @Override
    public List<TargetFinishDTO.ViewDTO> listDeptTargetFinish(TargetFinishDTO.ParamDTO dto) {
        return baseMapper.listDeptTargetFinish(dto);
    }

    @Override
    public List<TargetFinishDTO.ViewDTO> listUserTargetFinish(TargetFinishDTO.ParamDTO dto) {
        return baseMapper.listUserTargetFinish(dto);
    }




    /**
     * 填充显示的数据
     */
    private BigDecimal pullView(String metrics, Integer month, List<BiTargetStaffSettingEntity> dbList) {
        BigDecimal value = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                map(BiTargetStaffSettingEntity::getValue).orElse(null);
        if (value != null && MetricsEnum.GROSS_PROFIT_RATE.getCode().equals(metrics)) {
            value = MathUtil.multiplyWithTwo(value, MathUtil.NUMBER_100);
        }
        return value;

    }


    /**
     * 新增修改处理数据
     */
    public void handleData(BiTargetYearEntity targetYear, List<BiTargetStaffSettingDTO.CommonDTO> detailList, LogActionEnum action) {
        List<BiTargetStaffSettingDTO.ListDetailDTO> existList = baseMapper.listByYear(targetYear.getYear());
        List<String> existStaff = Lists.newArrayList();
        for (BiTargetStaffSettingDTO.CommonDTO item : detailList) {
            String staffId = item.getStaffId();
            MetricsEnum metrics = item.getMetrics();

            //一月
            BigDecimal january = item.getJanuary();
            if (Objects.nonNull(january)) {
                Integer januaryMoth = MonthEnum.JANUARY.getValue();
                putListDetailDTO(existList, staffId, metrics, januaryMoth, existStaff,action);
            }
            //二月
            BigDecimal february = item.getFebruary();
            if (Objects.nonNull(february)) {
                Integer februaryMoth = MonthEnum.FEBRUARY.getValue();
                putListDetailDTO(existList, staffId, metrics, februaryMoth, existStaff,action);
            }
            //三月
            BigDecimal march = item.getMarch();
            if (Objects.nonNull(march)) {
                Integer marchMoth = MonthEnum.MARCH.getValue();
                putListDetailDTO(existList, staffId, metrics, marchMoth, existStaff,action);
            }
            //四月
            BigDecimal april = item.getApril();
            if (Objects.nonNull(april)) {
                Integer aprilMoth = MonthEnum.APRIL.getValue();
                putListDetailDTO(existList, staffId, metrics, aprilMoth, existStaff,action);
            }
            //五月
            BigDecimal may = item.getMay();
            if (Objects.nonNull(may)) {
                Integer mayMoth = MonthEnum.MAY.getValue();
                putListDetailDTO(existList, staffId, metrics, mayMoth, existStaff,action);
            }
            //六月
            BigDecimal june = item.getJune();
            if (Objects.nonNull(june)) {
                Integer juneMoth = MonthEnum.JUNE.getValue();
                putListDetailDTO(existList, staffId, metrics, juneMoth, existStaff,action);
            }
            //七月
            BigDecimal july = item.getJuly();
            if (Objects.nonNull(july)) {
                Integer julyMoth = MonthEnum.JULY.getValue();
                putListDetailDTO(existList, staffId, metrics, julyMoth, existStaff,action);
            }
            //八月
            BigDecimal august = item.getAugust();
            if (Objects.nonNull(august)) {
                Integer augustMoth = MonthEnum.AUGUST.getValue();
                putListDetailDTO(existList, staffId, metrics, augustMoth, existStaff,action);
            }
            //九月
            BigDecimal september = item.getSeptember();
            if (Objects.nonNull(september)) {
                Integer septemberMoth = MonthEnum.SEPTEMBER.getValue();
                putListDetailDTO(existList, staffId, metrics, septemberMoth, existStaff,action);
            }
            //十月
            BigDecimal october = item.getOctober();
            if (Objects.nonNull(october)) {
                Integer octoberMoth = MonthEnum.OCTOBER.getValue();
                putListDetailDTO(existList, staffId, metrics, octoberMoth, existStaff,action);
            }

            //十一月
            BigDecimal november = item.getNovember();
            if (Objects.nonNull(november)) {
                Integer novemberMoth = MonthEnum.NOVEMBER.getValue();
                putListDetailDTO(existList, staffId, metrics, novemberMoth, existStaff,action);
            }

            //十二月
            BigDecimal december = item.getDecember();
            if (Objects.nonNull(december)) {
                Integer decemberMoth = MonthEnum.DECEMBER.getValue();
                putListDetailDTO(existList, staffId, metrics, decemberMoth, existStaff,action);
            }


        }
        if (CollectionUtils.isNotEmpty(existStaff)) {
            String existStaffName = existStaff.stream().distinct().collect(Collectors.joining(","));
            throw new ServiceException(ApiError.YEAR_METRICS_EXIST, existStaffName);
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


    private void putListDetailDTO(List<BiTargetStaffSettingDTO.ListDetailDTO> existList, String staffId, MetricsEnum metrics,Integer month, List<String> existStaff, LogActionEnum action) {
        //添加的
        if (LogActionEnum.INSERT.equals(action)) {
            BiTargetStaffSettingDTO.ListDetailDTO exist = existList.stream().filter(e ->
                    e.getStaffId().equals(staffId) &&
                            e.getMetrics().equals(metrics)
            ).findFirst().orElse(null);
            if (exist != null) {
                existStaff.add(exist.getStaffName());
            }
        } else {
            //修改的
            List<BiTargetStaffSettingDTO.ListDetailDTO> list = existList.stream().filter(e ->
                    e.getStaffId().equals(staffId) &&
                            e.getMetrics().equals(metrics)&&
                            e.getMonth().equals(month)
            ).collect(Collectors.toList());
            if (list.size() > 1) {
                existStaff.add(list.get(0).getStaffName());
            }
        }

    }
}
