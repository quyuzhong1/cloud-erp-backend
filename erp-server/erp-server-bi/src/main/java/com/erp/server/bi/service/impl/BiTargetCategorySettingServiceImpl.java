package com.erp.server.bi.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.bi.dto.BiTargetCategorySettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiTargetCategorySettingEntity;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.mapper.BiTargetCategorySettingMapper;
import com.erp.server.bi.service.BiTargetCategorySettingService;
import com.erp.server.bi.service.BiTargetYearService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        handleData(targetYear, detailList);

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
         handleData(targetYear,detailList);
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
                common.setJanuary(pullView(metrics, january, dbList));
                //二月
                Integer february = MonthEnum.FEBRUARY.getValue();
                common.setFebruary(pullView(metrics, february, dbList));
                //三月
                Integer march = MonthEnum.MARCH.getValue();
                common.setMarch(pullView(metrics, march, dbList));
                //四月
                Integer april = MonthEnum.APRIL.getValue();
                common.setApril(pullView(metrics, april, dbList));
                //五月
                Integer may = MonthEnum.MAY.getValue();
                common.setMay(pullView(metrics, may, dbList));
                //六月
                Integer june = MonthEnum.JUNE.getValue();
                common.setJune(pullView(metrics, june, dbList));
                //七月
                Integer july = MonthEnum.JULY.getValue();
                common.setJuly(pullView(metrics, july, dbList));
                //八月
                Integer august = MonthEnum.AUGUST.getValue();
                common.setAugust(pullView(metrics, august, dbList));
                //九月
                Integer september = MonthEnum.SEPTEMBER.getValue();
                common.setSeptember(pullView(metrics, september, dbList));
                //十月
                Integer october = MonthEnum.OCTOBER.getValue();
                common.setOctober(pullView(metrics, october, dbList));

                //十一月
                Integer november = MonthEnum.NOVEMBER.getValue();
                common.setNovember(pullView(metrics, november, dbList));

                //十二月
                Integer december = MonthEnum.DECEMBER.getValue();
                common.setDecember(pullView(metrics, december, dbList));
                categoryList.add(common);
            }
            detail.setCategorySettingList(categoryList);
            detailList.add(detail);
        }
        view.setDetailList(detailList);
        return view;
    }

    /**
     * 分页
     * @param dto
     * @return
     */
    @Override
    public PagingVO<BiTargetCategorySettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        BiTargetYearDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<BiTargetCategorySettingDTO.PagingViewDTO> list = pageData.getRecords();
        pullPaging(list);
        return new PagingVO<>(pageData);

    }

    /**
     * 填充分页数据
     * @author yl
     * @date 2023-09-14 16:47
     * @param list
     * @return void
     */
    private void pullPaging(List<BiTargetCategorySettingDTO.PagingViewDTO> list) {
        List<String> mainIdList = list.stream().map(BiTargetCategorySettingDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<BiTargetCategorySettingEntity> categorySettingDbList = this.listBaseByMainIds(mainIdList);
        for (BiTargetCategorySettingDTO.PagingViewDTO item : list) {
            List<BiTargetCategorySettingEntity> dbList = categorySettingDbList.stream().
                    filter(s -> s.getMainId().equals(item.getId())).collect(Collectors.toList());
            List<BiTargetCategorySettingDTO.CommonDTO> commonList = getCommon(dbList);
            item.setDetailList(commonList);
        }
    }

    private List<BiTargetCategorySettingDTO.CommonDTO> getCommon(List<BiTargetCategorySettingEntity> dbList) {
        List<BiTargetCategorySettingDTO.CommonDTO> resultList = new ArrayList<>(10);
        //根据指标分组
        Map<MetricsEnum, List<BiTargetCategorySettingEntity>> map = dbList.stream().
                collect(Collectors.groupingBy(BiTargetCategorySettingEntity::getMetrics));

        for (Map.Entry<MetricsEnum, List<BiTargetCategorySettingEntity>> item : map.entrySet()) {
            MetricsEnum metricsEnum = item.getKey();
            String metrics = metricsEnum.getCode();
            List<BiTargetCategorySettingEntity> categorySettingList = item.getValue();
            //根据sku分组
            Map<String, List<BiTargetCategorySettingEntity>> categoryMap = categorySettingList.stream().
                    collect(Collectors.groupingBy(BiTargetCategorySettingEntity::getCategoryId));
            for (Map.Entry<String, List<BiTargetCategorySettingEntity>> category : categoryMap.entrySet()) {
                String categoryId = category.getKey();
                List<BiTargetCategorySettingEntity> categoryDbList = category.getValue();
                BiTargetCategorySettingDTO.CommonDTO common = new BiTargetCategorySettingDTO.CommonDTO();
                common.setCategoryId(categoryId);
                common.setCategoryName(dbList.get(0).getCategoryName());
                //一月
                Integer january = MonthEnum.JANUARY.getValue();
                common.setJanuary(pullView(metrics, january, dbList));
                //二月
                Integer february = MonthEnum.FEBRUARY.getValue();
                common.setFebruary(pullView(metrics, february, dbList));
                //三月
                Integer march = MonthEnum.MARCH.getValue();
                common.setMarch(pullView(metrics, march, dbList));
                //四月
                Integer april = MonthEnum.APRIL.getValue();
                common.setApril(pullView(metrics, april, dbList));
                //五月
                Integer may = MonthEnum.MAY.getValue();
                common.setMay(pullView(metrics, may, dbList));
                //六月
                Integer june = MonthEnum.JUNE.getValue();
                common.setJune(pullView(metrics, june, dbList));
                //七月
                Integer july = MonthEnum.JULY.getValue();
                common.setJuly(pullView(metrics, july, dbList));
                //八月
                Integer august = MonthEnum.AUGUST.getValue();
                common.setAugust(pullView(metrics, august, dbList));
                //九月
                Integer september = MonthEnum.SEPTEMBER.getValue();
                common.setSeptember(pullView(metrics, september, dbList));
                //十月
                Integer october = MonthEnum.OCTOBER.getValue();
                common.setOctober(pullView(metrics, october, dbList));

                //十一月
                Integer november = MonthEnum.NOVEMBER.getValue();
                common.setNovember(pullView(metrics, november, dbList));

                //十二月
                Integer december = MonthEnum.DECEMBER.getValue();
                common.setDecember(pullView(metrics, december, dbList));
                common.setMetrics(metricsEnum);
                common.setMetricsName(metricsEnum.getName());
                resultList.add(common);
            }

        }

        return resultList;
    }


    /**
     * 填充显示的数据
     * @param metrics
     * @param month
     * @param dbList
     * @return
     */
    private BigDecimal pullView(String metrics, Integer month, List<BiTargetCategorySettingEntity> dbList) {
        BigDecimal value = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                map(BiTargetCategorySettingEntity::getValue).orElse(null);
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
    private void handleData(BiTargetYearEntity targetYear, List<BiTargetCategorySettingDTO.CommonDTO> detailList) {
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
}
