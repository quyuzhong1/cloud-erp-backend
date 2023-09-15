package com.erp.server.bi.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiTargetShopSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.entity.BiProductDetailEntity;
import com.erp.model.bi.entity.BiTargetShopSettingEntity;
import com.erp.model.bi.entity.BiTargetSkuSettingEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.mapper.BiTargetSkuSettingMapper;
import com.erp.server.bi.service.BiProductDetailService;
import com.erp.server.bi.service.BiTargetSkuSettingService;
import com.erp.server.bi.service.BiTargetYearService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetSkuSettingDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

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


    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetSkuSettingDTO.AddDTO addDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, targetYear);
        List<String> metricsList = addDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetSkuSettingDTO.CommonDTO> detailList = addDTO.getDetailList();
        // 数据处理
        handleData(targetYear, detailList);
        boolean save = biTargetYearService.save(targetYear);
        if (!save) {
            throw new ServiceException("sku 目标设置单保存失败");
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
            for (BiTargetSkuSettingEntity item : addList) {
                item.setMainId(mainId);
                String skuNo = skuList.stream().filter(s -> s.getId().equals(item.getSkuId())).
                        findFirst().map(BiProductDetailEntity::getSkuNo).orElse("");
                item.setSkuNo(skuNo);
            }
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
        handleData(targetYear, detailList);
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

                //十月
                Integer december = MonthEnum.DECEMBER.getValue();
                common.setDecember(pullView(metrics, december, dbList));
                skuList.add(common);
            }
            detail.setSkuSettingList(skuList);
            detailList.add(detail);
        }
        view.setDetailList(detailList);
        return view;
    }

    /**
     * 分页查询
     * @author yl
     * @date 2023-09-14 18:12
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.bi.dto.BiTargetSkuSettingDTO.PagingViewDTO>
     */
    @Override
    public PagingVO<BiTargetSkuSettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        BiTargetYearDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<BiTargetSkuSettingDTO.PagingViewDTO> list = pageData.getRecords();
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
    private void pullPaging(List<BiTargetSkuSettingDTO.PagingViewDTO> list) {
        List<String> mainIdList = list.stream().map(BiTargetSkuSettingDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<BiTargetSkuSettingEntity> skuSettingDbList = this.listBaseByMainIds(mainIdList);
        for (BiTargetSkuSettingDTO.PagingViewDTO item : list) {
            List<BiTargetSkuSettingEntity> dbList = skuSettingDbList.stream().
                    filter(s -> s.getMainId().equals(item.getId())).collect(Collectors.toList());
            List<BiTargetSkuSettingDTO.CommonDTO> commonList = getCommon(dbList);
            item.setDetailList(commonList);
        }
    }

    private List<BiTargetSkuSettingDTO.CommonDTO> getCommon(List<BiTargetSkuSettingEntity> dbList) {
        List<BiTargetSkuSettingDTO.CommonDTO> resultList = new ArrayList<>(10);
        //根据指标分组
        Map<MetricsEnum, List<BiTargetSkuSettingEntity>> map = dbList.stream().
                collect(Collectors.groupingBy(BiTargetSkuSettingEntity::getMetrics));

        for (Map.Entry<MetricsEnum, List<BiTargetSkuSettingEntity>> item : map.entrySet()) {
            MetricsEnum metricsEnum = item.getKey();
            String metrics = metricsEnum.getCode();
            List<BiTargetSkuSettingEntity> skuSettingList = item.getValue();
            //根据sku分组
            Map<String, List<BiTargetSkuSettingEntity>> skuMap = skuSettingList.stream().
                    collect(Collectors.groupingBy(BiTargetSkuSettingEntity::getSkuId));
            for (Map.Entry<String, List<BiTargetSkuSettingEntity>> sku : skuMap.entrySet()) {
                String skuId = sku.getKey();
                List<BiTargetSkuSettingEntity> skuDbList = sku.getValue();
                BiTargetSkuSettingDTO.CommonDTO common = new BiTargetSkuSettingDTO.CommonDTO();
                common.setSkuId(skuId);
                common.setSkuNo(dbList.get(0).getSkuNo());
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
    private BigDecimal pullView(String metrics, Integer month, List<BiTargetSkuSettingEntity> dbList) {
        BigDecimal value = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                map(BiTargetSkuSettingEntity::getValue).orElse(null);
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
    private void handleData(BiTargetYearEntity targetYear, List<BiTargetSkuSettingDTO.CommonDTO> detailList) {
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
