package com.erp.server.bi.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiProductInfoEntity;
import com.erp.model.bi.entity.BiTargetStaffSettingEntity;

import com.common.core.exception.ServiceException;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.server.bi.mapper.BiTargetStaffSettingMapper;
import com.erp.server.bi.service.BiTargetStaffSettingService;
import com.erp.server.bi.service.BiTargetYearService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetStaffSettingDTO.AddDTO addDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, targetYear);
        List<String> metricsList = addDTO.getMetricsList();
        targetYear.setMetrics(metricsList.stream().collect(Collectors.joining(",")));
        List<BiTargetStaffSettingDTO.CommonDTO> detailList = addDTO.getDetailList();
        // 数据处理
        handleData(targetYear, detailList);
        Boolean save = biTargetYearService.save(targetYear);
        if (!save) {
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
        }
        this.saveBatch(addList);
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
        handleData(targetYear, detailList);
        Boolean result = biTargetYearService.updateById(targetYear);
        if (!result) {
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
                staffList.add(common);

            }
            detail.setStaffSettingList(staffList);
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
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<BiTargetStaffSettingDTO.PagingViewDTO> list = pageData.getRecords();
        pullPaging(list);
        return new PagingVO<>(pageData);
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("warehouse downloadTemplate  出错了 e=={}", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    /**
     * 导入人员目标设置
     * @author yl
     * @date 2023-09-15 14:20
     * @param excelFile
     * @param response
     * @return com.erp.model.bi.dto.BiTargetStaffSettingDTO.ImportDTO
     */
    @Override
    public BiTargetStaffSettingDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        List<String> metricsList=MetricsEnum.listName();
        List<FindUserDTO> userList=sysUserFeign.getUserList();

        return null;
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
     * 填充分页的数据
     *
     * @param list
     */
    private void pullPaging(List<BiTargetStaffSettingDTO.PagingViewDTO> list) {
        List<String> mainIdList = list.stream().map(BiTargetStaffSettingDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<BiTargetStaffSettingEntity> staffSettingDbList = this.listBaseByMainIdList(mainIdList);
        for (BiTargetStaffSettingDTO.PagingViewDTO item : list) {
            List<BiTargetStaffSettingEntity> dbList = staffSettingDbList.stream().
                    filter(s -> s.getMainId().equals(item.getId())).collect(Collectors.toList());
            List<BiTargetStaffSettingDTO.CommonDTO> commonList = getCommon(dbList);
            item.setDetailList(commonList);
        }

    }

    private List<BiTargetStaffSettingDTO.CommonDTO> getCommon(List<BiTargetStaffSettingEntity> dbList) {
        List<BiTargetStaffSettingDTO.CommonDTO> resultList = new ArrayList<>(10);
        //根据指标分组
        Map<MetricsEnum, List<BiTargetStaffSettingEntity>> map = dbList.stream().
                collect(Collectors.groupingBy(BiTargetStaffSettingEntity::getMetrics));

        for (Map.Entry<MetricsEnum, List<BiTargetStaffSettingEntity>> item : map.entrySet()) {
            MetricsEnum metricsEnum = item.getKey();
            String metrics = metricsEnum.getCode();
            List<BiTargetStaffSettingEntity> staffSettingList = item.getValue();
            //根据人分组
            Map<String, List<BiTargetStaffSettingEntity>> staffMap = staffSettingList.stream().
                    collect(Collectors.groupingBy(BiTargetStaffSettingEntity::getStaffId));
            for (Map.Entry<String, List<BiTargetStaffSettingEntity>> staff : staffMap.entrySet()) {
                String staffId = staff.getKey();
                List<BiTargetStaffSettingEntity> staffDbList = staff.getValue();
                BiTargetStaffSettingDTO.CommonDTO common = new BiTargetStaffSettingDTO.CommonDTO();
                common.setStaffId(staffId);
                common.setStaffName(dbList.get(0).getStaffName());
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

    private List<BiTargetStaffSettingEntity> listBaseByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(BiTargetStaffSettingEntity::getMainId, mainIdList).list();
    }


    /**
     * 填充显示的数据
     */
    private BigDecimal pullView(String metrics, Integer month, List<BiTargetStaffSettingEntity> dbList) {
        BigDecimal value = dbList.stream().filter(d -> d.getMonth().equals(month)).findFirst().
                map(BiTargetStaffSettingEntity::getValue).orElse(null);
        return value;

    }


    /**
     * 新增修改处理数据
     */
    private void handleData(BiTargetYearEntity targetYear, List<BiTargetStaffSettingDTO.CommonDTO> detailList) {
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
//        List<BiTargetStaffSettingDTO.ListDetailDTO> existList = baseMapper.listByYear(year);
//        List<String> existStaff = Lists.newArrayList();
//        for (BiTargetStaffSettingDTO.CommonDTO item : detailList) {
//            BiTargetStaffSettingDTO.ListDetailDTO existDb = existList.stream().filter(e -> !e.getId().equals(item.getId()) &&
//                    e.getStaffId().equals(item.getStaffId()) &&
//                    e.getMetrics().equals(item.getMetrics()) &&
//                    e.getMonth().equals(item.getMonth())).findFirst().orElse(null);
//            if (existDb != null) {
//                existStaff.add(existDb.getStaffName());
//            }
//        }
//        if (CollectionUtils.isNotEmpty(existStaff)) {
//            String existStaffName = existStaff.stream().collect(Collectors.joining(","));
//            throw new ServiceException(ApiError.YEAR_METRICS_EXIST, existStaffName);
//        }


    }
}
