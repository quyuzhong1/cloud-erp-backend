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
import com.erp.model.bi.dto.excel.TargetSkuSettingImportExcelDTO;
import com.erp.model.bi.entity.BiProductDetailEntity;
import com.erp.model.bi.entity.BiTargetSkuSettingEntity;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.listener.BiTargetSkuSettingExcelListener;
import com.erp.server.bi.mapper.BiTargetSkuSettingMapper;
import com.erp.server.bi.service.BiProductDetailService;
import com.erp.server.bi.service.BiTargetCategorySettingService;
import com.erp.server.bi.service.BiTargetSkuSettingService;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * sku 目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetSkuSettingServiceImpl extends SuperServiceImpl<BiTargetSkuSettingMapper, BiTargetSkuSettingEntity> implements BiTargetSkuSettingService {

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private BiTargetYearService biTargetYearService;

    @Autowired
    private BiProductDetailService biProductDetailService;

    @Autowired
    private BiTargetCategorySettingService biTargetCategorySettingService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetSkuSettingDTO.AddDTO addDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, targetYear);
        List<String> metricsList = addDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetSkuSettingDTO.CommonDTO> detailList = addDTO.getDetailList();
        // 数据处理
        handleData(targetYear, detailList, LogActionEnum.INSERT);
        boolean save = biTargetYearService.save(targetYear);
        if (!save) {
            throw new ServiceException("sku 目标设置单保存失败");
        }

        //添加明细
        this.batchAdd(targetYear.getId(), detailList);
        //汇总分类的集合
        List<BiTargetSkuSettingDTO.CommonDTO> gatherCategoryList = detailList.stream().
                filter(d -> d.getIsGatherCategory()).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(gatherCategoryList)){
            addDTO.setDetailList(gatherCategoryList);
            autoCreateCategorySetting(addDTO);
        }


        return targetYear.getId();
    }

    /**
     * 自动创建分类设置 值
     *
     * @param addDTO
     * @return void
     * @author yl
     * @date 2023-09-26 18:21
     */
    public void autoCreateCategorySetting(BiTargetSkuSettingDTO.AddDTO addDTO) {
        BiTargetCategorySettingDTO.AddDTO categorySetting = new BiTargetCategorySettingDTO.AddDTO();
        BeanMapperUtils.copy(addDTO, categorySetting);
        //sku 详情
        List<BiTargetSkuSettingDTO.CommonDTO> skuDetailList = addDTO.getDetailList();
        List<String> skuIdList = skuDetailList.stream().map(BiTargetSkuSettingDTO.CommonDTO::getSkuId).collect(Collectors.toList());
        List<SkuSalesDTO.ProductSkuDTO> productSkuList = biProductDetailService.listProductSkuBySkuIdList(skuIdList);
        Map<MetricsEnum, List<BiTargetSkuSettingDTO.CommonDTO>> map = skuDetailList.stream().
                collect(Collectors.groupingBy(BiTargetSkuSettingDTO.CommonDTO::getMetrics));
        //品类集合
        List<BasicCategoryDTO> categoryList = plmTaskFeign.listCategoryTree();
        List<BiTargetCategorySettingDTO.CommonDTO> addCategoryDetailList = new ArrayList<>(10);
        for (BasicCategoryDTO item : categoryList) {
            String categoryId = item.getId();
            List<String> categoryIdsList = new ArrayList<>(10);
            getChildrenCategoryIds(item, categoryIdsList);
            List<String> categorySkuIdList = productSkuList.stream().filter(p -> categoryIdsList.contains(p.getCategoryId())).
                    map(SkuSalesDTO.ProductSkuDTO::getSkuId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(categorySkuIdList)) {
                for (Map.Entry<MetricsEnum, List<BiTargetSkuSettingDTO.CommonDTO>> mapItem : map.entrySet()) {
                    MetricsEnum metricsEnum = mapItem.getKey();
                    List<BiTargetSkuSettingDTO.CommonDTO> mapSkuSettingList = mapItem.getValue();
                    //一月
                    BigDecimal january = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getJanuary() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getJanuary).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BiTargetCategorySettingDTO.CommonDTO addCommon = new BiTargetCategorySettingDTO.CommonDTO();
                    addCommon.setCategoryId(categoryId);
                    addCommon.setMetrics(metricsEnum);
                    addCommon.setJanuary(january);
                    //二月
                    BigDecimal february = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getFebruary() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getFebruary).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addCommon.setFebruary(february);
                    //三月
                    BigDecimal march = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getMarch() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getMarch).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addCommon.setMarch(march);
                    //四月
                    BigDecimal april = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getApril() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getApril).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addCommon.setApril(april);
                    //五月
                    BigDecimal may = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getMay() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getMay).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addCommon.setMay(may);
                    //六月
                    BigDecimal june = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getJune() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getJune).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addCommon.setJune(june);
                    //七月
                    BigDecimal july = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getJuly() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getJuly).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addCommon.setJuly(july);
                    //八月
                    BigDecimal august = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getAugust() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getAugust).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addCommon.setAugust(august);
                    //九月
                    BigDecimal september = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getSeptember() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getSeptember).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addCommon.setSeptember(september);
                    //十月
                    BigDecimal october = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getOctober() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getOctober).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addCommon.setOctober(october);
                    //十一月
                    BigDecimal november = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getNovember() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getNovember).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addCommon.setNovember(november);
                    //十二月
                    BigDecimal december = mapSkuSettingList.stream().filter(s -> categorySkuIdList.contains(s.getSkuId()) &&
                                    s.getDecember() != null).
                            map(BiTargetSkuSettingDTO.CommonDTO::getDecember).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addCommon.setDecember(december);

                    addCategoryDetailList.add(addCommon);

                }


            }

        }


        categorySetting.setDetailList(addCategoryDetailList);
        biTargetCategorySettingService.add(categorySetting);
    }

    /**
     * 獲取到所有的子类
     *
     * @param item
     * @return
     */
    private void getChildrenCategoryIds(BasicCategoryDTO item, List<String> resultList) {
        List<BasicCategoryDTO> childrenListList = item.getChildrenList();
        if (CollectionUtils.isNotEmpty(childrenListList)) {
            List<String> list = childrenListList.stream().map(BasicCategoryDTO::getId).collect(Collectors.toList());
            resultList.addAll(list);
            for (BasicCategoryDTO itemChildren : childrenListList) {
                getChildrenCategoryIds(itemChildren, resultList);
            }
        }

    }

    /**
     * 添加明细的
     *
     * @param mainId
     * @param detailList
     */
    public void batchAdd(String mainId, List<BiTargetSkuSettingDTO.CommonDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }

        List<BiTargetSkuSettingEntity> addList = new ArrayList<>(20);
        for (BiTargetSkuSettingDTO.CommonDTO item : detailList) {
            List<BiTargetSkuSettingEntity> list = listAdd(item);
            addList.addAll(list);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            List<String> skuIdList = addList.stream().map(BiTargetSkuSettingEntity::getSkuId).
                    collect(Collectors.toList());
            List<BiProductDetailEntity> skuList = CollectionUtils.isNotEmpty(skuIdList) ? biProductDetailService.listByIds(skuIdList) : Collections.emptyList();
            if (CollectionUtils.isEmpty(skuList)) {
                throw new ServiceException("SKU 不存在");
            }
            for (BiTargetSkuSettingEntity item : addList) {
                item.setMainId(mainId);
                String skuNo = skuList.stream().filter(s -> s.getId().equals(item.getSkuId())).
                        findFirst().map(BiProductDetailEntity::getSkuNo).orElse("");
                item.setSkuNo(skuNo);
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
    private List<BiTargetSkuSettingEntity> listAdd(BiTargetSkuSettingDTO.CommonDTO item) {
        List<BiTargetSkuSettingEntity> addList = new ArrayList<>(12);
        String skuId = item.getSkuId();
        //指标
        MetricsEnum metrics = item.getMetrics();
        //一月值
        if (Objects.nonNull(item.getJanuary())) {
            addList.add(putEntity(skuId, item.getJanuary(), metrics, MonthEnum.JANUARY.getValue()));
        }
        //二月值
        BigDecimal february = item.getFebruary();
        if (Objects.nonNull(february)) {
            addList.add(putEntity(skuId, february, metrics, MonthEnum.FEBRUARY.getValue()));
        }
        //三月值
        BigDecimal march = item.getMarch();
        if (Objects.nonNull(march)) {
            addList.add(putEntity(skuId, march, metrics, MonthEnum.MARCH.getValue()));
        }
        //四月值
        BigDecimal april = item.getApril();
        if (Objects.nonNull(april)) {
            addList.add(putEntity(skuId, april, metrics, MonthEnum.APRIL.getValue()));
        }
        //五月值
        BigDecimal may = item.getMay();
        if (Objects.nonNull(may)) {
            addList.add(putEntity(skuId, may, metrics, MonthEnum.MAY.getValue()));
        }
        //六月值
        BigDecimal june = item.getJune();
        if (Objects.nonNull(june)) {
            addList.add(putEntity(skuId, june, metrics, MonthEnum.JUNE.getValue()));
        }
        //七月值
        BigDecimal july = item.getJuly();
        if (Objects.nonNull(july)) {
            addList.add(putEntity(skuId, july, metrics, MonthEnum.JULY.getValue()));
        }
        //八月值
        BigDecimal august = item.getAugust();
        if (Objects.nonNull(august)) {
            addList.add(putEntity(skuId, august, metrics, MonthEnum.AUGUST.getValue()));
        }
        //九月值
        BigDecimal september = item.getSeptember();
        if (Objects.nonNull(september)) {
            addList.add(putEntity(skuId, september, metrics, MonthEnum.SEPTEMBER.getValue()));
        }
        //十月值
        BigDecimal october = item.getOctober();
        if (Objects.nonNull(october)) {
            addList.add(putEntity(skuId, october, metrics, MonthEnum.OCTOBER.getValue()));
        }
        //十一月值
        BigDecimal november = item.getNovember();
        if (Objects.nonNull(november)) {
            addList.add(putEntity(skuId, november, metrics, MonthEnum.NOVEMBER.getValue()));
        }
        //十二月值
        BigDecimal december = item.getDecember();
        if (Objects.nonNull(december)) {
            addList.add(putEntity(skuId, december, metrics, MonthEnum.DECEMBER.getValue()));
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
    private BiTargetSkuSettingEntity putEntity(String skuId, BigDecimal value, MetricsEnum metrics, Integer month) {
        BiTargetSkuSettingEntity entity = new BiTargetSkuSettingEntity();
        entity.setMonth(month);
        entity.setSkuId(skuId);
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
    public Boolean update(BiTargetSkuSettingDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        BiTargetYearEntity oldTargetYear = biTargetYearService.getById(id);
        Optional.ofNullable(oldTargetYear).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "sku目标设置单"));
        BiTargetYearEntity targetYear = BeanMapperUtils.map(BiTargetYearEntity.class, updateDTO);
        List<String> metricsList = updateDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetSkuSettingDTO.CommonDTO> detailList = updateDTO.getDetailList();

        // 数据处理
        handleData(targetYear, detailList, LogActionEnum.UPDATE);
        boolean save = biTargetYearService.updateById(targetYear);
        if (!save) {
            throw new ServiceException("sku目标设置单保存失败");
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
        this.lambdaUpdate().eq(BiTargetSkuSettingEntity::getMainId, mainId).remove();
    }

    /**
     * 详情信息
     *
     * @param id
     * @return
     */
    @Override
    public BiTargetSkuSettingDTO.ViewDTO view(String id) {
        BiTargetSkuSettingDTO.ViewDTO view = new BiTargetSkuSettingDTO.ViewDTO();
        BiTargetYearEntity targetYear = biTargetYearService.getById(id);
        if (Objects.isNull(targetYear)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "单品目标设置");
        }
        BeanMapperUtils.copy(targetYear, view);
        String metrics = targetYear.getMetrics();
        view.setMetricsList(Arrays.asList(metrics.split(",")));
        List<BiTargetSkuSettingEntity> skuSettingDbList = this.listBaseByMainIds(Arrays.asList(id));
        //根据指标分组
        Map<MetricsEnum, List<BiTargetSkuSettingEntity>> map = skuSettingDbList.stream().
                collect(Collectors.groupingBy(BiTargetSkuSettingEntity::getMetrics));
        //详情
        List<BiTargetSkuSettingDTO.DetailDTO> detailList = new ArrayList<>(map.size());
        for (Map.Entry<MetricsEnum, List<BiTargetSkuSettingEntity>> item : map.entrySet()) {
            BiTargetSkuSettingDTO.DetailDTO detail = new BiTargetSkuSettingDTO.DetailDTO();
            MetricsEnum metricsEnum = item.getKey();
            String metricsFlag = metricsEnum.getCode();
            detail.setMetrics(metricsEnum);
            detail.setMetricsName(metricsEnum.getName());
            List<BiTargetSkuSettingEntity> skuSettingList = item.getValue();
            //根据sku分组
            Map<String, List<BiTargetSkuSettingEntity>> skuMap = skuSettingList.stream().
                    collect(Collectors.groupingBy(BiTargetSkuSettingEntity::getSkuId));
            List<BiTargetSkuSettingDTO.CommonDTO> skuList = new ArrayList<>(skuMap.size());
            for (Map.Entry<String, List<BiTargetSkuSettingEntity>> sku : skuMap.entrySet()) {
                String skuId = sku.getKey();
                List<BiTargetSkuSettingEntity> dbList = sku.getValue();
                BiTargetSkuSettingDTO.CommonDTO common = new BiTargetSkuSettingDTO.CommonDTO();
                common.setSkuId(skuId);
                common.setSkuNo(dbList.get(0).getSkuNo());
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
                skuList.add(common);
            }
            detail.setSettingList(skuList);
            detailList.add(detail);
        }
        view.setDetailList(detailList);
        return view;
    }

    /**
     * 分页查询
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.bi.dto.BiTargetSkuSettingDTO.PagingViewDTO>
     * @author yl
     * @date 2023-09-14 18:12
     */
    @Override
    public PagingVO<BiTargetSkuSettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        BiTargetYearDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        //乘的值
        BigDecimal multiplyNum = getMultiplyNum(params.getMetrics());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<BiTargetSkuSettingDTO.PagingViewDTO> pageData = baseMapper.paging(query, params, multiplyNum);
        List<BiTargetSkuSettingDTO.PagingViewDTO> list = pageData.getRecords();
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
    public List<TargetFinishDTO.ViewDTO> listTargetFinish(TargetFinishDTO.ParamDTO dto) {
        return baseMapper.listTargetFinish(dto);
    }


    /**
     * 下载模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-09-17 14:22
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/TargetSkuSetting.xlsx";
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
     *
     * @param excelFile
     * @param response
     * @return
     */
    @Override
    public BiTargetSkuSettingDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        List<String> metricsNameList = MetricsEnum.listName();
        BiTargetSkuSettingExcelListener excelListenerUtil = new BiTargetSkuSettingExcelListener(metricsNameList, biProductDetailService);
        try {
            read(excelFile.getInputStream(), TargetSkuSettingImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("单品目标设置 导入错误>>>{}", e);
        }
        BiTargetSkuSettingDTO.ImportDTO result = new BiTargetSkuSettingDTO.ImportDTO();
        List<BiTargetSkuSettingDTO.CommonDTO> successList = excelListenerUtil.getSuccessList();
        Map<MetricsEnum, List<BiTargetSkuSettingDTO.CommonDTO>> map = successList.stream().
                collect(Collectors.groupingBy(BiTargetSkuSettingDTO.CommonDTO::getMetrics));

        List<BiTargetSkuSettingDTO.DetailDTO> detailList = new ArrayList<>(map.size());
        for (Map.Entry<MetricsEnum, List<BiTargetSkuSettingDTO.CommonDTO>> item : map.entrySet()) {
            BiTargetSkuSettingDTO.DetailDTO detail = new BiTargetSkuSettingDTO.DetailDTO();
            detail.setMetrics(item.getKey());
            detail.setMetricsName(item.getKey().getName());
            detail.setSettingList(item.getValue());
            detailList.add(detail);
        }
        result.setSuccessList(detailList);
        List<TargetSkuSettingImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "单品设置错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, TargetSkuSettingImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }

    /**
     * 列表删除单品
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean delete(BiTargetSkuSettingDTO.RemoveDTO dto) {
        Boolean result = this.lambdaUpdate().
                eq(BiTargetSkuSettingEntity::getSkuId, dto.getSkuId()).
                eq(BiTargetSkuSettingEntity::getMainId, dto.getId()).
                eq(BiTargetSkuSettingEntity::getMetrics, dto.getMetrics()).
                remove();
        return result;

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
        BiTargetYearDTO.PagingTotalDTO pagingTotal = baseMapper.pagingTotal(dto, multiplyNum);
        return pagingTotal;

    }


    /**
     * 填充显示的数据
     *
     * @param metrics
     * @param month
     * @param dbList
     * @return
     */
    private BigDecimal pullView(String metrics, Integer month, List<BiTargetSkuSettingEntity> dbList) {
        BigDecimal value = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                map(BiTargetSkuSettingEntity::getValue).orElse(null);
        if (value != null && MetricsEnum.GROSS_PROFIT_RATE.getCode().equals(metrics)) {
            value = MathUtil.multiply(value, MathUtil.NUMBER_100);
        }
        return value;
    }

    private List<BiTargetSkuSettingEntity> listBaseByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(BiTargetSkuSettingEntity::getMainId, mainIdList).
                orderByDesc(BiTargetSkuSettingEntity::getId).list();
    }


    /**
     * 新增修改处理数据
     */
    public void handleData(BiTargetYearEntity targetYear, List<BiTargetSkuSettingDTO.CommonDTO> detailList, LogActionEnum action) {
        //部门id
        String deptId = targetYear.getDeptId();
        List<BiTargetSkuSettingDTO.ListDetailDTO> existList = baseMapper.listByYearAndDept(targetYear.getYear(), deptId);
        List<String> existSku = Lists.newArrayList();
        for (BiTargetSkuSettingDTO.CommonDTO item : detailList) {
            String skuId = item.getSkuId();
            MetricsEnum metrics = item.getMetrics();

            //一月
            BigDecimal january = item.getJanuary();
            if (Objects.nonNull(january)) {
                Integer januaryMoth = MonthEnum.JANUARY.getValue();
                putListDetailDTO(existList, skuId, metrics, januaryMoth, existSku, action);
            }
            //二月
            BigDecimal february = item.getFebruary();
            if (Objects.nonNull(february)) {
                Integer februaryMoth = MonthEnum.FEBRUARY.getValue();
                putListDetailDTO(existList, skuId, metrics, februaryMoth, existSku, action);
            }
            //三月
            BigDecimal march = item.getMarch();
            if (Objects.nonNull(march)) {
                Integer marchMoth = MonthEnum.MARCH.getValue();
                putListDetailDTO(existList, skuId, metrics, marchMoth, existSku, action);
            }
            //四月
            BigDecimal april = item.getApril();
            if (Objects.nonNull(april)) {
                Integer aprilMoth = MonthEnum.APRIL.getValue();
                putListDetailDTO(existList, skuId, metrics, aprilMoth, existSku, action);
            }
            //五月
            BigDecimal may = item.getMay();
            if (Objects.nonNull(may)) {
                Integer mayMoth = MonthEnum.MAY.getValue();
                putListDetailDTO(existList, skuId, metrics, mayMoth, existSku, action);
            }
            //六月
            BigDecimal june = item.getJune();
            if (Objects.nonNull(june)) {
                Integer juneMoth = MonthEnum.JUNE.getValue();
                putListDetailDTO(existList, skuId, metrics, juneMoth, existSku, action);
            }
            //七月
            BigDecimal july = item.getJuly();
            if (Objects.nonNull(july)) {
                Integer julyMoth = MonthEnum.JULY.getValue();
                putListDetailDTO(existList, skuId, metrics, julyMoth, existSku, action);
            }
            //八月
            BigDecimal august = item.getAugust();
            if (Objects.nonNull(august)) {
                Integer augustMoth = MonthEnum.AUGUST.getValue();
                putListDetailDTO(existList, skuId, metrics, augustMoth, existSku, action);
            }
            //九月
            BigDecimal september = item.getSeptember();
            if (Objects.nonNull(september)) {
                Integer septemberMoth = MonthEnum.SEPTEMBER.getValue();
                putListDetailDTO(existList, skuId, metrics, septemberMoth, existSku, action);
            }
            //十月
            BigDecimal october = item.getOctober();
            if (Objects.nonNull(october)) {
                Integer octoberMoth = MonthEnum.OCTOBER.getValue();
                putListDetailDTO(existList, skuId, metrics, octoberMoth, existSku, action);
            }

            //十一月
            BigDecimal november = item.getNovember();
            if (Objects.nonNull(november)) {
                Integer novemberMoth = MonthEnum.NOVEMBER.getValue();
                putListDetailDTO(existList, skuId, metrics, novemberMoth, existSku, action);
            }

            //十二月
            BigDecimal december = item.getDecember();
            if (Objects.nonNull(december)) {
                Integer decemberMoth = MonthEnum.DECEMBER.getValue();
                putListDetailDTO(existList, skuId, metrics, decemberMoth, existSku, action);
            }
        }
        if (CollectionUtils.isNotEmpty(existSku)) {
            String existSkuName = existSku.stream().distinct().collect(Collectors.joining(","));
            throw new ServiceException(ApiError.YEAR_METRICS_EXIST, existSkuName);
        }


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

    private void putListDetailDTO(List<BiTargetSkuSettingDTO.ListDetailDTO> existList, String skuId, MetricsEnum metrics, Integer month, List<String> existSku, LogActionEnum action) {
        //添加的
        if (LogActionEnum.INSERT.equals(action)) {
            BiTargetSkuSettingDTO.ListDetailDTO exist = existList.stream().filter(e ->
                    e.getSkuId().equals(skuId) &&
                            e.getMetrics().equals(metrics)
            ).findFirst().orElse(null);
            if (exist != null) {
                existSku.add(exist.getSkuNo());
            }
        } else {
            //修改的
            List<BiTargetSkuSettingDTO.ListDetailDTO> list = existList.stream().filter(e ->
                    e.getSkuId().equals(skuId) &&
                            e.getMetrics().equals(metrics) &&
                            e.getMonth().equals(month)
            ).collect(Collectors.toList());
            if (list.size() > 1) {
                existSku.add(list.get(0).getSkuNo());
            }
        }

    }
}
