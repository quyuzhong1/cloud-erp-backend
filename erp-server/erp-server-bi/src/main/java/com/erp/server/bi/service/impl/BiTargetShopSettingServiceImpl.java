package com.erp.server.bi.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.bi.dto.BiTargetShopSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiTargetShopSettingEntity;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.mapper.BiTargetShopSettingMapper;
import com.erp.server.bi.service.BiTargetShopSettingService;
import com.erp.server.bi.service.BiTargetYearService;
import com.erp.server.bi.service.DmpShopInfoService;
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
    private DmpShopInfoService dmpShopInfoService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetShopSettingDTO.AddDTO addDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, targetYear);
        List<String> metricsList = addDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetShopSettingDTO.CommonDTO> detailList = addDTO.getDetailList();
        // 数据处理
        handleData(targetYear, detailList);

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
            List<DmpShopInfoEntity> shopList = CollectionUtils.isNotEmpty(shopIdList) ? dmpShopInfoService.listByIds(shopIdList) : Collections.emptyList();
            for (BiTargetShopSettingEntity item : addList) {
                item.setMainId(mainId);
                String shopName = shopList.stream().filter(s -> s.getId().equals(item.getShopId())).
                        findFirst().map(DmpShopInfoEntity::getName).orElse("");
                item.setShopName(shopName);
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
        handleData(targetYear, detailList);
        boolean save = biTargetYearService.updateById(targetYear);
        if (!save) {
            throw new ServiceException("店铺目标设置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）
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
                shopList.add(common);
            }
            detail.setShopSettingList(shopList);
            detailList.add(detail);
        }
        view.setDetailList(detailList);

        return view;
    }

    /**
     * 分页查询
     * @author yl
     * @date 2023-09-14 16:43
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.bi.dto.BiTargetShopSettingDTO.PagingViewDTO>
     */
    @Override
    public PagingVO<BiTargetShopSettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        BiTargetYearDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<BiTargetShopSettingDTO.PagingViewDTO> list = pageData.getRecords();
        pullPaging(list);
        return new PagingVO<>(pageData);

    }

    @Override
    public List<BiTargetShopSettingEntity> listTargetFinish(TargetFinishDTO.ParamDTO dto) {
        return baseMapper.listTargetFinish(dto);
    }

    /**
     * 填充分页数据
     * @author yl
     * @date 2023-09-14 16:47
     * @param list
     * @return void
     */
    private void pullPaging(List<BiTargetShopSettingDTO.PagingViewDTO> list) {
        List<String> mainIdList = list.stream().map(BiTargetShopSettingDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<BiTargetShopSettingEntity> shopSettingDbList = this.listBaseByMainIds(mainIdList);
        for (BiTargetShopSettingDTO.PagingViewDTO item : list) {
            List<BiTargetShopSettingEntity> dbList = shopSettingDbList.stream().
                    filter(s -> s.getMainId().equals(item.getId())).collect(Collectors.toList());
            List<BiTargetShopSettingDTO.CommonDTO> commonList = getCommon(dbList);
            item.setDetailList(commonList);
        }
    }


    private List<BiTargetShopSettingDTO.CommonDTO> getCommon(List<BiTargetShopSettingEntity> dbList) {
        List<BiTargetShopSettingDTO.CommonDTO> resultList = new ArrayList<>(10);
        //根据指标分组
        Map<MetricsEnum, List<BiTargetShopSettingEntity>> map = dbList.stream().
                collect(Collectors.groupingBy(BiTargetShopSettingEntity::getMetrics));

        for (Map.Entry<MetricsEnum, List<BiTargetShopSettingEntity>> item : map.entrySet()) {
            MetricsEnum metricsEnum = item.getKey();
            String metrics = metricsEnum.getCode();
            List<BiTargetShopSettingEntity> shopSettingList = item.getValue();
            //根据人分组
            Map<String, List<BiTargetShopSettingEntity>> staffMap = shopSettingList.stream().
                    collect(Collectors.groupingBy(BiTargetShopSettingEntity::getShopId));
            for (Map.Entry<String, List<BiTargetShopSettingEntity>> shop : staffMap.entrySet()) {
                String shopId = shop.getKey();
                List<BiTargetShopSettingEntity> shopDbList = shop.getValue();
                BiTargetShopSettingDTO.CommonDTO common = new BiTargetShopSettingDTO.CommonDTO();
                common.setShopId(shopId);
                common.setShopName(dbList.get(0).getShopName());
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
    private BigDecimal pullView(String metrics, Integer month, List<BiTargetShopSettingEntity> dbList) {
        BigDecimal value = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                map(BiTargetShopSettingEntity::getValue).orElse(null);
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
    private void handleData(BiTargetYearEntity targetYear, List<BiTargetShopSettingDTO.CommonDTO> detailList) {
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
