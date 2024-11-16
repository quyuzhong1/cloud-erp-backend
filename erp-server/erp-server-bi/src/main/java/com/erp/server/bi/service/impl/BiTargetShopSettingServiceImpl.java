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
import com.erp.model.bi.dto.BiTargetShopSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.dto.excel.TargetShopSettingImportExcelDTO;
import com.erp.model.bi.entity.BiTargetShopSettingEntity;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.listener.BiTargetShopSettingExcelListener;
import com.erp.server.bi.mapper.BiTargetShopSettingMapper;
import com.erp.server.bi.service.BiTargetShopSettingService;
import com.erp.server.bi.service.BiTargetYearService;
import com.erp.server.bi.service.BiShopInfoService;
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
 * 店铺目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetShopSettingServiceImpl extends SuperServiceImpl<BiTargetShopSettingMapper, BiTargetShopSettingEntity> implements BiTargetShopSettingService {

    @Autowired
    private BiTargetYearService biTargetYearService;

    @Autowired
    private BiShopInfoService biShopInfoService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetShopSettingDTO.AddDTO addDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, targetYear);
        List<String> metricsList = addDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetShopSettingDTO.CommonDTO> detailList = addDTO.getDetailList();
        // 数据处理
        handleData(targetYear, detailList, LogActionEnum.INSERT);

        boolean save = biTargetYearService.save(targetYear);
        if (!save) {
            throw new ServiceException("店铺目标设置单保存失败");
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
    public void batchAdd(String mainId, List<BiTargetShopSettingDTO.CommonDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<BiTargetShopSettingEntity> addList = new ArrayList<>(20);
        for (BiTargetShopSettingDTO.CommonDTO item : detailList) {
            List<BiTargetShopSettingEntity> list = listAdd(item);
            addList.addAll(list);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            List<String> shopIdList = addList.stream().map(BiTargetShopSettingEntity::getShopId).
                    collect(Collectors.toList());
            //店铺信息
            List<BiShopInfoEntity> shopList = CollectionUtils.isNotEmpty(shopIdList) ? biShopInfoService.listByIds(shopIdList) : Collections.emptyList();
            for (BiTargetShopSettingEntity item : addList) {
                item.setMainId(mainId);
                String shopName = shopList.stream().filter(s -> s.getId().equals(item.getShopId())).
                        findFirst().map(BiShopInfoEntity::getName).orElse("");
                item.setShopName(shopName);
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
    private List<BiTargetShopSettingEntity> listAdd(BiTargetShopSettingDTO.CommonDTO item) {
        List<BiTargetShopSettingEntity> addList = new ArrayList<>(12);
        String shopId = item.getShopId();
        //指标
        MetricsEnum metrics = item.getMetrics();
        //一月值
        if (Objects.nonNull(item.getJanuary())) {
            addList.add(putEntity(shopId, item.getJanuary(), metrics, MonthEnum.JANUARY.getValue()));
        }
        //二月值
        BigDecimal february = item.getFebruary();
        if (Objects.nonNull(february)) {
            addList.add(putEntity(shopId, february, metrics, MonthEnum.FEBRUARY.getValue()));
        }
        //三月值
        BigDecimal march = item.getMarch();
        if (Objects.nonNull(march)) {
            addList.add(putEntity(shopId, march, metrics, MonthEnum.MARCH.getValue()));
        }
        //四月值
        BigDecimal april = item.getApril();
        if (Objects.nonNull(april)) {
            addList.add(putEntity(shopId, april, metrics, MonthEnum.APRIL.getValue()));
        }
        //五月值
        BigDecimal may = item.getMay();
        if (Objects.nonNull(may)) {
            addList.add(putEntity(shopId, may, metrics, MonthEnum.MAY.getValue()));
        }
        //六月值
        BigDecimal june = item.getJune();
        if (Objects.nonNull(june)) {
            addList.add(putEntity(shopId, june, metrics, MonthEnum.JUNE.getValue()));
        }
        //七月值
        BigDecimal july = item.getJuly();
        if (Objects.nonNull(july)) {
            addList.add(putEntity(shopId, july, metrics, MonthEnum.JULY.getValue()));
        }
        //八月值
        BigDecimal august = item.getAugust();
        if (Objects.nonNull(august)) {
            addList.add(putEntity(shopId, august, metrics, MonthEnum.AUGUST.getValue()));
        }
        //九月值
        BigDecimal september = item.getSeptember();
        if (Objects.nonNull(september)) {
            addList.add(putEntity(shopId, september, metrics, MonthEnum.SEPTEMBER.getValue()));
        }
        //十月值
        BigDecimal october = item.getOctober();
        if (Objects.nonNull(october)) {
            addList.add(putEntity(shopId, october, metrics, MonthEnum.OCTOBER.getValue()));
        }
        //十一月值
        BigDecimal november = item.getNovember();
        if (Objects.nonNull(november)) {
            addList.add(putEntity(shopId, november, metrics, MonthEnum.NOVEMBER.getValue()));
        }
        //十二月值
        BigDecimal december = item.getDecember();
        if (Objects.nonNull(december)) {
            addList.add(putEntity(shopId, december, metrics, MonthEnum.DECEMBER.getValue()));
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
    private BiTargetShopSettingEntity putEntity(String shopId, BigDecimal value, MetricsEnum metrics, Integer month) {
        BiTargetShopSettingEntity entity = new BiTargetShopSettingEntity();
        entity.setMonth(month);
        entity.setShopId(shopId);
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
    public Boolean update(BiTargetShopSettingDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        BiTargetYearEntity oldTargetYear = biTargetYearService.getById(id);
        Optional.ofNullable(oldTargetYear).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "店铺目标设置单"));
        BiTargetYearEntity targetYear = BeanMapperUtils.map(BiTargetYearEntity.class, updateDTO);
        List<String> metricsList = updateDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetShopSettingDTO.CommonDTO> detailList = updateDTO.getDetailList();

        // 数据处理
        handleData(targetYear, detailList, LogActionEnum.UPDATE);
        boolean save = biTargetYearService.updateById(targetYear);
        if (!save) {
            throw new ServiceException("店铺目标设置单保存失败");
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
        this.lambdaUpdate().eq(BiTargetShopSettingEntity::getMainId, mainId).remove();
    }

    /**
     * 获取详情
     *
     * @param id
     * @return com.erp.model.bi.dto.BiTargetShopSettingDTO.ViewDTO
     * @author yl
     * @date 2023-09-14 16:16
     */
    @Override
    public BiTargetShopSettingDTO.ViewDTO view(String id) {
        BiTargetShopSettingDTO.ViewDTO view = new BiTargetShopSettingDTO.ViewDTO();
        BiTargetYearEntity targetYear = biTargetYearService.getById(id);
        if (Objects.isNull(targetYear)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "店铺目标设置");
        }
        BeanMapperUtils.copy(targetYear, view);
        String metrics = targetYear.getMetrics();
        view.setMetricsList(Arrays.asList(metrics.split(",")));
        List<BiTargetShopSettingEntity> shopSettingDbList = this.listBaseByMainIds(Arrays.asList(id));
        //根据指标分组
        Map<MetricsEnum, List<BiTargetShopSettingEntity>> map = shopSettingDbList.stream().
                collect(Collectors.groupingBy(BiTargetShopSettingEntity::getMetrics));
        //详情
        List<BiTargetShopSettingDTO.DetailDTO> detailList = new ArrayList<>(map.size());
        for (Map.Entry<MetricsEnum, List<BiTargetShopSettingEntity>> item : map.entrySet()) {
            BiTargetShopSettingDTO.DetailDTO detail = new BiTargetShopSettingDTO.DetailDTO();
            MetricsEnum metricsEnum = item.getKey();
            String metricsFlag = metricsEnum.getCode();
            detail.setMetrics(metricsEnum);
            detail.setMetricsName(metricsEnum.getName());
            List<BiTargetShopSettingEntity> shopSettingList = item.getValue();
            //根据店铺分组
            Map<String, List<BiTargetShopSettingEntity>> shopMap = shopSettingList.stream().
                    collect(Collectors.groupingBy(BiTargetShopSettingEntity::getShopId));
            List<BiTargetShopSettingDTO.CommonDTO> shopList = new ArrayList<>(shopMap.size());
            for (Map.Entry<String, List<BiTargetShopSettingEntity>> shop : shopMap.entrySet()) {
                String shopId = shop.getKey();
                List<BiTargetShopSettingEntity> dbList = shop.getValue();
                BiTargetShopSettingDTO.CommonDTO common = new BiTargetShopSettingDTO.CommonDTO();
                common.setShopId(shopId);
                common.setShopName(dbList.get(0).getShopName());
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
                shopList.add(common);
            }
            detail.setSettingList(shopList);
            detailList.add(detail);
        }
        view.setDetailList(detailList);

        return view;
    }

    /**
     * 分页查询
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.bi.dto.BiTargetShopSettingDTO.PagingViewDTO>
     * @author yl
     * @date 2023-09-14 16:43
     */
    @Override
    public PagingVO<BiTargetShopSettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        BiTargetYearDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        String metrics = params.getMetrics();
        //乘的值
        BigDecimal multiplyNum = getMultiplyNum(metrics);
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<BiTargetShopSettingDTO.PagingViewDTO> pageData = baseMapper.paging(query, params,multiplyNum);
        List<BiTargetShopSettingDTO.PagingViewDTO> list = pageData.getRecords();
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
     * 填充显示的数据
     *
     * @param metrics
     * @param month
     * @param dbList
     * @return
     */
    private BigDecimal pullView(String metrics, Integer month, List<BiTargetShopSettingEntity> dbList) {
        BigDecimal value = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                map(BiTargetShopSettingEntity::getValue).orElse(null);
        if (value != null && MetricsEnum.GROSS_PROFIT_RATE.getCode().equals(metrics)) {
            value = MathUtil.multiply(value, MathUtil.NUMBER_100);
        }
        return value;
    }


    private List<BiTargetShopSettingEntity> listBaseByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(BiTargetShopSettingEntity::getMainId, mainIdList).
                orderByDesc(BiTargetShopSettingEntity::getId).list();
    }


    /**
     * 新增修改处理数据
     */
    public void handleData(BiTargetYearEntity targetYear, List<BiTargetShopSettingDTO.CommonDTO> detailList, LogActionEnum action) {
        List<BiTargetShopSettingDTO.ListDetailDTO> existList = baseMapper.listByYear(targetYear.getYear());
        List<String> existShop = Lists.newArrayList();
        for (BiTargetShopSettingDTO.CommonDTO item : detailList) {
            String shopId = item.getShopId();
            MetricsEnum metrics = item.getMetrics();
            //一月
            BigDecimal january = item.getJanuary();
            if (Objects.nonNull(january)) {
                Integer januaryMoth = MonthEnum.JANUARY.getValue();
                putListDetailDTO(existList, shopId, metrics, januaryMoth, existShop,action);
            }
            //二月
            BigDecimal february = item.getFebruary();
            if (Objects.nonNull(february)) {
                Integer februaryMoth = MonthEnum.FEBRUARY.getValue();
                putListDetailDTO(existList, shopId, metrics, februaryMoth, existShop,action);
            }
            //三月
            BigDecimal march = item.getMarch();
            if (Objects.nonNull(march)) {
                Integer marchMoth = MonthEnum.MARCH.getValue();
                putListDetailDTO(existList, shopId, metrics, marchMoth, existShop,action);
            }
            //四月
            BigDecimal april = item.getApril();
            if (Objects.nonNull(april)) {
                Integer aprilMoth = MonthEnum.APRIL.getValue();
                putListDetailDTO(existList, shopId, metrics, aprilMoth, existShop,action);
            }
            //五月
            BigDecimal may = item.getMay();
            if (Objects.nonNull(may)) {
                Integer mayMoth = MonthEnum.MAY.getValue();
                putListDetailDTO(existList, shopId, metrics, mayMoth, existShop,action);
            }
            //六月
            BigDecimal june = item.getJune();
            if (Objects.nonNull(june)) {
                Integer juneMoth = MonthEnum.JUNE.getValue();
                putListDetailDTO(existList, shopId, metrics, juneMoth, existShop,action);
            }
            //七月
            BigDecimal july = item.getJuly();
            if (Objects.nonNull(july)) {
                Integer julyMoth = MonthEnum.JULY.getValue();
                putListDetailDTO(existList, shopId, metrics, julyMoth, existShop,action);
            }
            //八月
            BigDecimal august = item.getAugust();
            if (Objects.nonNull(august)) {
                Integer augustMoth = MonthEnum.AUGUST.getValue();
                putListDetailDTO(existList, shopId, metrics, augustMoth, existShop,action);
            }
            //九月
            BigDecimal september = item.getSeptember();
            if (Objects.nonNull(september)) {
                Integer septemberMoth = MonthEnum.SEPTEMBER.getValue();
                putListDetailDTO(existList, shopId, metrics, septemberMoth, existShop,action);
            }
            //十月
            BigDecimal october = item.getOctober();
            if (Objects.nonNull(october)) {
                Integer octoberMoth = MonthEnum.OCTOBER.getValue();
                putListDetailDTO(existList, shopId, metrics, octoberMoth, existShop,action);
            }



        }
        if (CollectionUtils.isNotEmpty(existShop)) {
            String existShopName = existShop.stream().distinct().collect(Collectors.joining(","));
            throw new ServiceException(ApiError.YEAR_METRICS_EXIST, existShopName);
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

    private void putListDetailDTO(List<BiTargetShopSettingDTO.ListDetailDTO> existList, String shopId, MetricsEnum metrics,Integer month ,List<String> existShop, LogActionEnum action) {
        //添加的
        if (LogActionEnum.INSERT.equals(action)) {
            BiTargetShopSettingDTO.ListDetailDTO exist = existList.stream().filter(e ->
                    e.getShopId().equals(shopId) &&
                            e.getMetrics().equals(metrics)
                            ).findFirst().orElse(null);
            if (exist != null) {
                existShop.add(exist.getShopName());
            }
        } else {
            //修改的
            List<BiTargetShopSettingDTO.ListDetailDTO> list = existList.stream().filter(e ->
                    e.getShopId().equals(shopId) &&
                            e.getMetrics().equals(metrics)&&
                            e.getMonth().equals(month)
                           ).collect(Collectors.toList());
            if (list.size() > 1) {
                existShop.add(list.get(0).getShopName());
            }
        }
    }


    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/TargetShopSetting.xlsx";
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
     * @return com.erp.model.bi.dto.BiTargetShopSettingDTO.ImportDTO
     * @author yl
     * @date 2023-09-15 16:32
     */
    @Override
    public BiTargetShopSettingDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        List<String> metricsNameList = MetricsEnum.listName();
        List<BiShopInfoEntity> shopInfoList = biShopInfoService.list();
        BiTargetShopSettingExcelListener excelListenerUtil = new BiTargetShopSettingExcelListener(metricsNameList, shopInfoList);
        try {
            read(excelFile.getInputStream(), TargetShopSettingImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("人员目标设置 导入错误>>>{}", e);
        }
        BiTargetShopSettingDTO.ImportDTO result = new BiTargetShopSettingDTO.ImportDTO();
        List<BiTargetShopSettingDTO.CommonDTO> successList = excelListenerUtil.getSuccessList();
        Map<MetricsEnum, List<BiTargetShopSettingDTO.CommonDTO>> map = successList.stream().
                collect(Collectors.groupingBy(BiTargetShopSettingDTO.CommonDTO::getMetrics));

        List<BiTargetShopSettingDTO.DetailDTO> detailList = new ArrayList<>(map.size());
        for (Map.Entry<MetricsEnum, List<BiTargetShopSettingDTO.CommonDTO>> item : map.entrySet()) {
            BiTargetShopSettingDTO.DetailDTO detail = new BiTargetShopSettingDTO.DetailDTO();
            detail.setMetrics(item.getKey());
            detail.setMetricsName(item.getKey().getName());
            detail.setSettingList(item.getValue());
            detailList.add(detail);
        }
        result.setSuccessList(detailList);
        List<TargetShopSettingImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "店铺设置错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, TargetShopSettingImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }

    /**
     * 列表删除店铺目标
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean delete(BiTargetShopSettingDTO.RemoveDTO dto) {
        Boolean result = this.lambdaUpdate().
                eq(BiTargetShopSettingEntity::getShopId, dto.getShopId()).
                eq(BiTargetShopSettingEntity::getMainId, dto.getId()).
                eq(BiTargetShopSettingEntity::getMetrics, dto.getMetrics()).
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
        return baseMapper.pagingTotal(dto,multiplyNum);
    }

}
