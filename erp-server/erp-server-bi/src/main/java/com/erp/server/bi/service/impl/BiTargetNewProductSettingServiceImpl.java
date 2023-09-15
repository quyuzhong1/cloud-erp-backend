package com.erp.server.bi.service.impl;


import com.common.business.dto.FindUserDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;
import com.erp.model.bi.entity.BiTargetNewProductSettingEntity;

import com.erp.model.bi.entity.BiTargetStaffSettingEntity;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.mapper.BiTargetNewProductSettingMapper;
import com.erp.server.bi.service.BiTargetNewProductSettingService;
import com.common.core.exception.ServiceException;
import com.erp.server.bi.service.BiTargetYearService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetNewProductSettingDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

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
        BiTargetNewProductSettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "新品目标设置单"));
        BiTargetNewProductSettingEntity biTargetNewProductSettingEntity = BeanMapperUtils.map(BiTargetNewProductSettingEntity.class, updateDTO);

        // 数据处理
      //  handleData(biTargetNewProductSettingEntity);
        log.info("编辑 开始修改新品目标设置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetNewProductSettingEntity);
        if (!save) {
            throw new ServiceException("新品目标设置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        return Boolean.TRUE;
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



        }
        return null;
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
}
