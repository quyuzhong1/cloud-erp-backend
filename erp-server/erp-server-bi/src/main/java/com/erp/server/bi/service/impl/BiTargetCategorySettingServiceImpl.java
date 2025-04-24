package com.erp.server.bi.service.impl;


import static com.alibaba.excel.EasyExcel.read;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.dto.excel.TargetCategorySettingImportExcelDTO;
import com.erp.model.bi.dto.excel.TargetSkuSettingImportExcelDTO;
import com.erp.model.bi.entity.BiTargetCategorySettingEntity;
import com.erp.model.bi.entity.BiTargetStaffSettingEntity;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.listener.BiTargetCategorySettingExcelListener;
import com.erp.server.bi.listener.BiTargetSkuSettingExcelListener;
import com.erp.server.bi.mapper.BiTargetCategorySettingMapper;
import com.erp.server.bi.service.BiTargetCategorySettingService;
import com.erp.server.bi.service.BiTargetYearService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 分类 目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetCategorySettingServiceImpl extends SuperServiceImpl<BiTargetCategorySettingMapper, BiTargetCategorySettingEntity> implements BiTargetCategorySettingService {
    @Autowired
    private BiTargetYearService biTargetYearService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetCategorySettingDTO.AddDTO addDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, targetYear);
        List<String> metricsList = addDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetCategorySettingDTO.CommonDTO> detailList = addDTO.getDetailList();

        // 数据处理
        handleData(targetYear, detailList, LogActionEnum.INSERT);

        log.info("开始新增分类 目标设置单");
        boolean save = biTargetYearService.save(targetYear);
        if (!save) {
            throw new ServiceException("分类 目标设置单保存失败");
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
    public void batchAdd(String mainId, List<BiTargetCategorySettingDTO.CommonDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<BiTargetCategorySettingEntity> addList = new ArrayList<>(20);
        for (BiTargetCategorySettingDTO.CommonDTO item : detailList) {
            List<BiTargetCategorySettingEntity> list = listAdd(item);
            addList.addAll(list);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            List<String> categoryIdList = addList.stream().map(BiTargetCategorySettingEntity::getCategoryId).
                    collect(Collectors.toList());
            List<BasicCategoryEntity> categoryList = CollectionUtils.isNotEmpty(categoryIdList) ? plmTaskFeign.listCategoryByIds(categoryIdList) : Collections.emptyList();

            for (BiTargetCategorySettingEntity item : addList) {
                item.setMainId(mainId);
                String categoryName = categoryList.stream().filter(c -> c.getId().equals(item.getCategoryId())).
                        findFirst().map(BasicCategoryEntity::getName).orElse("");
                item.setCategoryName(categoryName);
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
    private List<BiTargetCategorySettingEntity> listAdd(BiTargetCategorySettingDTO.CommonDTO item) {
        List<BiTargetCategorySettingEntity> addList = new ArrayList<>(12);
        String categoryId = item.getCategoryId();
        //指标
        MetricsEnum metrics = item.getMetrics();

        //一月值
        if (Objects.nonNull(item.getJanuary())) {
            addList.add(putEntity(categoryId, item.getJanuary(), metrics, MonthEnum.JANUARY.getValue()));
        }
        //二月值
        BigDecimal february = item.getFebruary();
        if (Objects.nonNull(february)) {
            addList.add(putEntity(categoryId, february, metrics, MonthEnum.FEBRUARY.getValue()));
        }
        //三月值
        BigDecimal march = item.getMarch();
        if (Objects.nonNull(march)) {
            addList.add(putEntity(categoryId, march, metrics, MonthEnum.MARCH.getValue()));
        }
        //四月值
        BigDecimal april = item.getApril();
        if (Objects.nonNull(april)) {
            addList.add(putEntity(categoryId, april, metrics, MonthEnum.APRIL.getValue()));
        }
        //五月值
        BigDecimal may = item.getMay();
        if (Objects.nonNull(may)) {
            addList.add(putEntity(categoryId, may, metrics, MonthEnum.MAY.getValue()));
        }
        //六月值
        BigDecimal june = item.getJune();
        if (Objects.nonNull(june)) {
            addList.add(putEntity(categoryId, june, metrics, MonthEnum.JUNE.getValue()));
        }
        //七月值
        BigDecimal july = item.getJuly();
        if (Objects.nonNull(july)) {
            addList.add(putEntity(categoryId, july, metrics, MonthEnum.JULY.getValue()));
        }
        //八月值
        BigDecimal august = item.getAugust();
        if (Objects.nonNull(august)) {
            addList.add(putEntity(categoryId, august, metrics, MonthEnum.AUGUST.getValue()));
        }
        //九月值
        BigDecimal september = item.getSeptember();
        if (Objects.nonNull(september)) {
            addList.add(putEntity(categoryId, september, metrics, MonthEnum.SEPTEMBER.getValue()));
        }
        //十月值
        BigDecimal october = item.getOctober();
        if (Objects.nonNull(october)) {
            addList.add(putEntity(categoryId, october, metrics, MonthEnum.OCTOBER.getValue()));
        }
        //十一月值
        BigDecimal november = item.getNovember();
        if (Objects.nonNull(november)) {
            addList.add(putEntity(categoryId, november, metrics, MonthEnum.NOVEMBER.getValue()));
        }
        //十二月值
        BigDecimal december = item.getDecember();
        if (Objects.nonNull(december)) {
            addList.add(putEntity(categoryId, december, metrics, MonthEnum.DECEMBER.getValue()));
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
    private BiTargetCategorySettingEntity putEntity(String categorgId, BigDecimal value, MetricsEnum metrics, Integer month) {
        BiTargetCategorySettingEntity entity = new BiTargetCategorySettingEntity();
        entity.setMonth(month);
        entity.setCategoryId(categorgId);
        entity.setValue(value);
        entity.setMetrics(metrics);
        if (MetricsEnum.GROSS_PROFIT_RATE.equals(metrics)) {
            entity.setValue(MathUtil.divide(value, MathUtil.BigDecimal_100, 2));
        }
        return entity;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetCategorySettingDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        BiTargetYearEntity oldTargetYear = biTargetYearService.getById(id);
        Optional.ofNullable(oldTargetYear).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "品类目标设置单"));
        BiTargetYearEntity targetYear = BeanMapperUtils.map(BiTargetYearEntity.class, updateDTO);
        List<String> metricsList = updateDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetCategorySettingDTO.CommonDTO> detailList = updateDTO.getDetailList();
        // 数据处理
        handleData(targetYear, detailList, LogActionEnum.UPDATE);
        boolean save = biTargetYearService.updateById(targetYear);
        if (!save) {
            throw new ServiceException("分类 目标设置单保存失败");
        }
        this.removeByMainId(id);
        this.batchAdd(id, detailList);
        return Boolean.TRUE;
    }

    @Override
    public List<TargetFinishDTO.ViewDTO> listTargetFinish(TargetFinishDTO.ParamDTO dto) {
        return baseMapper.listTargetFinish(dto);
    }


    /**
     * 删除
     *
     * @param mainId
     */
    public void removeByMainId(String mainId) {
        this.lambdaUpdate().eq(BiTargetCategorySettingEntity::getMainId, mainId).remove();
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @Override
    public BiTargetCategorySettingDTO.ViewDTO view(String id) {
        BiTargetCategorySettingDTO.ViewDTO view = new BiTargetCategorySettingDTO.ViewDTO();
        BiTargetYearEntity targetYear = biTargetYearService.getById(id);
        if (Objects.isNull(targetYear)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "品类目标设置");
        }
        BeanMapperUtils.copy(targetYear, view);
        String metrics = targetYear.getMetrics();
        view.setMetricsList(Arrays.asList(metrics.split(",")));
        List<BiTargetCategorySettingEntity> skuSettingDbList = this.listBaseByMainIds(Arrays.asList(id));
        //根据指标分组
        Map<MetricsEnum, List<BiTargetCategorySettingEntity>> map = skuSettingDbList.stream().
                collect(Collectors.groupingBy(BiTargetCategorySettingEntity::getMetrics));
        //详情
        List<BiTargetCategorySettingDTO.DetailDTO> detailList = new ArrayList<>(map.size());
        for (Map.Entry<MetricsEnum, List<BiTargetCategorySettingEntity>> item : map.entrySet()) {
            BiTargetCategorySettingDTO.DetailDTO detail = new BiTargetCategorySettingDTO.DetailDTO();
            MetricsEnum metricsEnum = item.getKey();
            String metricsFlag = metricsEnum.getCode();
            detail.setMetrics(metricsEnum);
            detail.setMetricsName(metricsEnum.getName());
            List<BiTargetCategorySettingEntity> categorySettingList = item.getValue();
            //根据分类分组
            Map<String, List<BiTargetCategorySettingEntity>> categoryMap = categorySettingList.stream().
                    collect(Collectors.groupingBy(BiTargetCategorySettingEntity::getCategoryId));
            List<BiTargetCategorySettingDTO.CommonDTO> categoryList = new ArrayList<>(categoryMap.size());
            for (Map.Entry<String, List<BiTargetCategorySettingEntity>> category : categoryMap.entrySet()) {
                String categoryId = category.getKey();
                List<BiTargetCategorySettingEntity> dbList = category.getValue();
                BiTargetCategorySettingDTO.CommonDTO common = new BiTargetCategorySettingDTO.CommonDTO();
                common.setCategoryId(categoryId);
                common.setCategoryName(dbList.get(0).getCategoryName());
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

                //十二月
                Integer december = MonthEnum.DECEMBER.getValue();
                common.setDecember(pullView(metricsFlag, december, dbList));
                categoryList.add(common);
            }
            detail.setSettingList(categoryList);
            detailList.add(detail);
        }
        view.setDetailList(detailList);
        return view;
    }

    /**
     * 分页
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<BiTargetCategorySettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        BiTargetYearDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        String metrics = params.getMetrics();
        //乘的值
        BigDecimal multiplyNum = getMultiplyNum(metrics);
        IPage<BiTargetCategorySettingDTO.PagingViewDTO> pageData = baseMapper.paging(query, params, multiplyNum);
        List<BiTargetCategorySettingDTO.PagingViewDTO> list = pageData.getRecords();
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
        String path = "classpath:excel/TargetCategorySetting.xlsx";
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
     * 导入数据
     *
     * @param excelFile
     * @param response
     * @return
     */
    @Override
    public BiTargetCategorySettingDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        List<String> metricsNameList = MetricsEnum.listName();
        List<BasicCategoryEntity> categoryList = plmTaskFeign.listParentCategory();
        BiTargetCategorySettingExcelListener excelListenerUtil = new BiTargetCategorySettingExcelListener(metricsNameList, categoryList);
        try {
            read(excelFile.getInputStream(), TargetCategorySettingImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("单品目标设置 导入错误>>>{}", e);
        }
        BiTargetCategorySettingDTO.ImportDTO result = new BiTargetCategorySettingDTO.ImportDTO();
        List<BiTargetCategorySettingDTO.CommonDTO> successList = excelListenerUtil.getSuccessList();
        Map<MetricsEnum, List<BiTargetCategorySettingDTO.CommonDTO>> map = successList.stream().
                collect(Collectors.groupingBy(BiTargetCategorySettingDTO.CommonDTO::getMetrics));

        List<BiTargetCategorySettingDTO.DetailDTO> detailList = new ArrayList<>(map.size());
        for (Map.Entry<MetricsEnum, List<BiTargetCategorySettingDTO.CommonDTO>> item : map.entrySet()) {
            BiTargetCategorySettingDTO.DetailDTO detail = new BiTargetCategorySettingDTO.DetailDTO();
            detail.setMetrics(item.getKey());
            detail.setMetricsName(item.getKey().getName());
            detail.setSettingList(item.getValue());
            detailList.add(detail);
        }
        result.setSuccessList(detailList);
        List<TargetCategorySettingImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "品类设置错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, TargetCategorySettingImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }

    /**
     * 删除分类
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean delete(BiTargetCategorySettingDTO.RemoveDTO dto) {
        return this.lambdaUpdate().
                eq(BiTargetCategorySettingEntity::getCategoryId, dto.getCategoryId()).
                eq(BiTargetCategorySettingEntity::getMainId, dto.getId()).
                eq(BiTargetCategorySettingEntity::getMetrics, dto.getMetrics()).
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
     *
     * @param metrics
     * @param month
     * @param dbList
     * @return
     */
    private BigDecimal pullView(String metrics, Integer month, List<BiTargetCategorySettingEntity> dbList) {
        BigDecimal value = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                map(BiTargetCategorySettingEntity::getValue).orElse(null);
        if (value != null && MetricsEnum.GROSS_PROFIT_RATE.getCode().equals(metrics)) {
            value = MathUtil.multiply(value, MathUtil.NUMBER_100);
        }
        return value;
    }

    private List<BiTargetCategorySettingEntity> listBaseByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(BiTargetCategorySettingEntity::getMainId, mainIdList).
                orderByDesc(BiTargetCategorySettingEntity::getId).list();
    }


    /**
     * 新增修改处理数据
     */
    public void handleData(BiTargetYearEntity targetYear, List<BiTargetCategorySettingDTO.CommonDTO> detailList, LogActionEnum action) {
        List<BiTargetCategorySettingDTO.ListDetailDTO> existList = baseMapper.listByYearAndDept(targetYear.getYear(), targetYear.getDeptId());
        List<String> existCategory = Lists.newArrayList();

        // 处理月份逻辑提取为方法
        for (BiTargetCategorySettingDTO.CommonDTO item : detailList) {
            processMonthlyData(item, existList, existCategory, action);
        }

        checkAndThrowIfCategoriesExist(existCategory);

        populateTargetYearDetails(targetYear);
    }

    private void processMonthlyData(BiTargetCategorySettingDTO.CommonDTO item, List<BiTargetCategorySettingDTO.ListDetailDTO> existList, List<String> existCategory, LogActionEnum action) {
        String categoryId = item.getCategoryId();
        MetricsEnum metrics = item.getMetrics();

        Map<Integer, BigDecimal> monthMap = getMonthMap(item);
        for (Map.Entry<Integer, BigDecimal> entry : monthMap.entrySet()) {
            if (Objects.nonNull(entry.getValue())) {
                putListDetailDTO(existList, categoryId, metrics, entry.getKey(), existCategory, action);
            }
        }
    }

    private Map<Integer, BigDecimal> getMonthMap(BiTargetCategorySettingDTO.CommonDTO item) {
        Map<Integer, BigDecimal> monthMap = new HashMap<>();
        monthMap.put(MonthEnum.JANUARY.getValue(), item.getJanuary());
        monthMap.put(MonthEnum.FEBRUARY.getValue(), item.getFebruary());
        // 添加其他月份
        return monthMap;
    }

    private void checkAndThrowIfCategoriesExist(List<String> existCategory) {
        if (CollectionUtils.isNotEmpty(existCategory)) {
            String existCategoryName = existCategory.stream().distinct().collect(Collectors.joining(","));
            throw new ServiceException(ApiError.YEAR_METRICS_EXIST, existCategoryName);
        }
    }

    private void populateTargetYearDetails(BiTargetYearEntity targetYear) {
        String deptId = targetYear.getDeptId();
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


    private void putListDetailDTO(List<BiTargetCategorySettingDTO.ListDetailDTO> existList, String categoryId, MetricsEnum metrics,Integer month, List<String> existCategory, LogActionEnum action) {
        //添加的
        if (LogActionEnum.INSERT.equals(action)) {
            BiTargetCategorySettingDTO.ListDetailDTO exist = existList.stream().filter(e ->
                    e.getCategoryId().equals(categoryId) &&
                            e.getMetrics().equals(metrics)
            ).findFirst().orElse(null);
            if (exist != null) {
                existCategory.add(exist.getCategoryName());
            }
        } else {
            //修改的
            List<BiTargetCategorySettingDTO.ListDetailDTO> list = existList.stream().filter(e ->
                    e.getCategoryId().equals(categoryId) &&
                            e.getMetrics().equals(metrics)&&
                            e.getMonth().equals(month)
            ).collect(Collectors.toList());
            if (list.size() > 1) {
                existCategory.add(list.get(0).getCategoryName());
            }
        }
    }
}
