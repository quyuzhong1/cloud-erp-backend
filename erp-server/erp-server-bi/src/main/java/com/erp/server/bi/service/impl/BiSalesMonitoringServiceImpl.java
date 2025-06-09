package com.erp.server.bi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.SeriesVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.bi.dto.BiSalesMonitoringDTO;
import com.erp.model.bi.dto.BiSalesMonitoringSearchDTO;
import com.erp.model.bi.dto.BiSalesMonitoringTableDTO;
import com.erp.model.bi.entity.BiSalesMonitoringEntity;
import com.erp.model.bi.vo.BiSalesMonitoringTableVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.BiCompareEnum;
import com.erp.server.bi.enums.SalesMonitoringTypeEnum;
import com.erp.server.bi.mapper.BiSalesMonitoringMapper;
import com.erp.server.bi.service.BiSalesMonitoringService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/29 16:24
 */
@Service
public class BiSalesMonitoringServiceImpl extends ServiceImpl<BiSalesMonitoringMapper, BiSalesMonitoringEntity>
        implements BiSalesMonitoringService {

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Override
    public Boolean batchAdd(List<BiSalesMonitoringDTO> list) {
        if (list.isEmpty()) {
            throw new ServiceException(ApiError.ERROR_97016);
        }
        List<BiSalesMonitoringEntity> entityList = new ArrayList<>();
        //当前登录人
        LoginUser loginUser = UserContext.getLoginUser();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_403);
        }
        Map<String, List<BiSalesMonitoringDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getMetrics().concat(obj.getType().toString())));
        for (Map.Entry<String, List<BiSalesMonitoringDTO>> entry:map.entrySet()) {
            List<BiSalesMonitoringDTO> value = entry.getValue();
            int size = value.size();
            if (size > 1) {
                throw new ServiceException(ApiError.ERROR_97018);
            }
            BiSalesMonitoringEntity entity = new BiSalesMonitoringEntity();
            BiSalesMonitoringDTO biSalesMonitoringDTO = value.get(0);
            BeanUtils.copyProperties(biSalesMonitoringDTO,entity);
            entity.setChargeId(loginUser.getUid());
            entity.setChargeName(loginUser.getUserName());
            entityList.add(entity);
        }
       return this.saveBatch(entityList);
    }

    @Override
    public void batchUpdate(List<BiSalesMonitoringDTO> list) {
        //当前登录人
        LoginUser loginUser = UserContext.getLoginUser();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_403);
        }
        //数据较少，可删除后重新新增
        removeByChargeId(loginUser.getUid());
        if (!list.isEmpty()) {
            this.batchAdd(list);
        }
    }

    @Override
    public List<BiSalesMonitoringDTO> listBiSalesMonitoring() {
        List<BiSalesMonitoringDTO> resultList = new ArrayList<>();
        //当前登录人
        LoginUser loginUser = UserContext.getLoginUser();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_403);
        }
        List<BiSalesMonitoringEntity> list = listByChargeId(loginUser.getUid());
        if (CollectionUtils.isNotEmpty(list)) {
            resultList =  BeanUtil.copyToList(list,BiSalesMonitoringDTO.class);
        }
        return resultList;
    }

    @Override
    public LinkedHashMap<String,Object> listBiSalesMonitoringView(BiSalesMonitoringSearchDTO.ParamDTO dto) {
        //当前登录人
        LoginUser loginUser = UserContext.getLoginUser();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_403);
        }
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        List<BiSalesMonitoringEntity> list = listByChargeId(loginUser.getUid());
        if (CollectionUtils.isEmpty(list)) {
            return map;
        }
        //当月第一天
        LocalDateTime currentMonth = LocalDateTime.of(LocalDate.from(LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);

        //上上个月第一天
        LocalDateTime lastsMonth = currentMonth.minusMonths(2);
        dto.setStartTime(null);
        dto.setEndTime(null);
        List<BiSalesMonitoringTableDTO.ViewDTO> resultList = null;
          resultList = this.baseMapper.listBiSalesMonitoring(dto,lastsMonth);
        if (CollectionUtils.isEmpty(resultList)) {
            return map;
        }
        SeriesVO seriesVO = new SeriesVO();
        BiSalesMonitoringEntity  entity= list.stream().filter(obj -> obj.getType().equals(dto.getSearchType()) && obj.getMetrics().equals(dto.getMetrics())).findFirst().orElse(null);

        if (dto.getSearchType().equals(SalesMonitoringTypeEnum.SKU.getCode()) && ObjectUtils.isNotEmpty(entity)) {
            this.listSalesMonitoring(resultList, entity, seriesVO);
        }
        if (dto.getSearchType().equals(SalesMonitoringTypeEnum.DEPT.getCode()) && ObjectUtils.isNotEmpty(entity)) {
            this.listDeptNameMonitoring(resultList, entity, seriesVO);
        }
        if (dto.getSearchType().equals(SalesMonitoringTypeEnum.CATEGORY.getCode()) && ObjectUtils.isNotEmpty(entity)) {
            this.listCategoryMonitoring(resultList, entity, seriesVO);
        }
        if (dto.getSearchType().equals(SalesMonitoringTypeEnum.USER.getCode()) && ObjectUtils.isNotEmpty(entity)) {
            this.listChargeNameMonitoring(resultList, entity, seriesVO);
        }
        if (dto.getSearchType().equals(SalesMonitoringTypeEnum.SHOP.getCode()) && ObjectUtils.isNotEmpty(entity)) {
            this.listShopMonitoring(resultList, entity, seriesVO);
        }
        if (dto.getSearchType().equals(SalesMonitoringTypeEnum.PLATFORM.getCode()) && ObjectUtils.isNotEmpty(entity)) {
            this.listPlatformMonitoring(resultList, entity, seriesVO);
        }
        if (dto.getSearchType().equals(SalesMonitoringTypeEnum.COUNTRY.getCode()) && ObjectUtils.isNotEmpty(entity)) {
            this.listCountryMonitoring(resultList, entity, seriesVO);
        }
        LinkedHashMap<String,Object> head = handleHand(dto);
        map.put("head",head);
        map.put("data",seriesVO);
        return map;
    }

    /**
     * 销量或销售额监控设置数据查询
     */
    private void listSalesMonitoring(List<BiSalesMonitoringTableDTO.ViewDTO> list,BiSalesMonitoringEntity entity,SeriesVO seriesVO) {
        //type:1代表销量,2代表销售额
        Map<String, List<BiSalesMonitoringTableDTO.ViewDTO>> listMap = list.stream()
                .filter(obj ->StringUtils.isNotBlank(obj.getTypeName()))
                .collect(Collectors.groupingBy(BiSalesMonitoringTableDTO.ViewDTO::getTypeName));
        List<BiSalesMonitoringTableVO.SkuDTO> resultList = new ArrayList<>();
        String name = "";
        for (Map.Entry<String, List<BiSalesMonitoringTableDTO.ViewDTO>> entry: listMap.entrySet()) {
            List<BiSalesMonitoringTableDTO.ViewDTO> value = entry.getValue();
            BiSalesMonitoringTableVO.SkuDTO vo = new BiSalesMonitoringTableVO.SkuDTO();
            //获取结果集
            Pair<String, BiSalesMonitoringTableVO.CommonDTO> pair = handleMonitoring(value, entity);
            if (ObjectUtils.isNotEmpty(pair)) {
                BiSalesMonitoringTableVO.CommonDTO biSalesMonitoringTableVO = pair.getValue();
                if (StringUtils.isBlank(name)) {
                    name = pair.getKey();
                }
                BeanUtils.copyProperties(biSalesMonitoringTableVO,vo);
                vo.setSkuNo(value.get(0).getTypeName());
                resultList.add(vo);
            }
        }
        seriesVO.setName(name);
        seriesVO.setData(resultList);
    }

    /**
     * 部门销售额监控设置数据查询
     */
    private void listDeptNameMonitoring(List<BiSalesMonitoringTableDTO.ViewDTO> list,BiSalesMonitoringEntity entity,SeriesVO seriesVO) {
        Map<String, List<BiSalesMonitoringTableDTO.ViewDTO>> listMap = list.stream()
                .filter(obj ->StringUtils.isNotBlank(obj.getTypeName()))
                .collect(Collectors.groupingBy(BiSalesMonitoringTableDTO.ViewDTO::getTypeName));
        List<BiSalesMonitoringTableVO.DeptDTO> resultList = new ArrayList<>();
        String name = "";
        for (Map.Entry<String, List<BiSalesMonitoringTableDTO.ViewDTO>> entry: listMap.entrySet()) {
            List<BiSalesMonitoringTableDTO.ViewDTO> value = entry.getValue();
            BiSalesMonitoringTableVO.DeptDTO vo = new BiSalesMonitoringTableVO.DeptDTO();
            //获取结果集
            Pair<String, BiSalesMonitoringTableVO.CommonDTO> pair = handleMonitoring(value, entity);
            if (ObjectUtils.isNotEmpty(pair)) {
                BiSalesMonitoringTableVO.CommonDTO biSalesMonitoringTableVO = pair.getValue();
                if (StringUtils.isBlank(name)) {
                    name = pair.getKey();
                }
                BeanUtils.copyProperties(biSalesMonitoringTableVO,vo);
                vo.setDeptName(value.get(0).getTypeName());
                resultList.add(vo);
            }
        }
        seriesVO.setName(name);
        seriesVO.setData(resultList);
    }

    /**
     * 品类销售额监控设置数据查询
     */
    private void listCategoryMonitoring(List<BiSalesMonitoringTableDTO.ViewDTO> list,BiSalesMonitoringEntity entity,SeriesVO seriesVO) {
        Map<String, List<BiSalesMonitoringTableDTO.ViewDTO>> listMap = list.stream()
                .filter(obj ->StringUtils.isNotBlank(obj.getTypeName()))
                .collect(Collectors.groupingBy(BiSalesMonitoringTableDTO.ViewDTO::getTypeName));
        List<BiSalesMonitoringTableVO.CategoryDTO> resultList = new ArrayList<>();
        String name = "";
        for (Map.Entry<String, List<BiSalesMonitoringTableDTO.ViewDTO>> entry: listMap.entrySet()) {
            List<BiSalesMonitoringTableDTO.ViewDTO> value = entry.getValue();
            BiSalesMonitoringTableVO.CategoryDTO vo = new BiSalesMonitoringTableVO.CategoryDTO();
            BeanUtils.copyProperties(value.get(0),vo);
            //获取结果集
            Pair<String, BiSalesMonitoringTableVO.CommonDTO> pair = handleMonitoring(value, entity);

            if (ObjectUtils.isNotEmpty(pair)) {
                BiSalesMonitoringTableVO.CommonDTO biSalesMonitoringTableVO = pair.getValue();
                if (StringUtils.isBlank(name)) {
                    name = pair.getKey();
                }
                BeanUtils.copyProperties(biSalesMonitoringTableVO,vo);
                vo.setCategoryName(value.get(0).getTypeName());
                resultList.add(vo);
            }
        }
        seriesVO.setName(name);
        seriesVO.setData(resultList);
    }

    /**
     * 品牌销售额监控设置数据查询
     */
    private void listChargeNameMonitoring(List<BiSalesMonitoringTableDTO.ViewDTO> list,BiSalesMonitoringEntity entity,SeriesVO seriesVO) {
        Map<String, List<BiSalesMonitoringTableDTO.ViewDTO>> listMap = list.stream()
                .filter(obj ->StringUtils.isNotBlank(obj.getTypeName()))
                .collect(Collectors.groupingBy(BiSalesMonitoringTableDTO.ViewDTO::getTypeName));
        List<BiSalesMonitoringTableVO.ChargeDTO> resultList = new ArrayList<>();
        String name = "";
        for (Map.Entry<String, List<BiSalesMonitoringTableDTO.ViewDTO>> entry: listMap.entrySet()) {
            List<BiSalesMonitoringTableDTO.ViewDTO> value = entry.getValue();
            BiSalesMonitoringTableVO.ChargeDTO vo = new BiSalesMonitoringTableVO.ChargeDTO();
            BeanUtils.copyProperties(value.get(0),vo);
            //获取结果集
            Pair<String, BiSalesMonitoringTableVO.CommonDTO> pair = handleMonitoring(value, entity);
            if (ObjectUtils.isNotEmpty(pair)) {
                BiSalesMonitoringTableVO.CommonDTO biSalesMonitoringTableVO = pair.getValue();
                if (StringUtils.isBlank(name)) {
                    name = pair.getKey();
                }
                BeanUtils.copyProperties(biSalesMonitoringTableVO,vo);
                vo.setChargeName(value.get(0).getTypeName());
                resultList.add(vo);
            }
        }
        seriesVO.setName(name);
        seriesVO.setData(resultList);
    }

    /**
     * 品牌销售额监控设置数据查询
     */
    private void listShopMonitoring(List<BiSalesMonitoringTableDTO.ViewDTO> list,BiSalesMonitoringEntity entity,SeriesVO seriesVO) {
        Map<String, List<BiSalesMonitoringTableDTO.ViewDTO>> listMap = list.stream()
                .filter(obj ->StringUtils.isNotBlank(obj.getTypeName()))
                .collect(Collectors.groupingBy(BiSalesMonitoringTableDTO.ViewDTO::getTypeName));
        List<BiSalesMonitoringTableVO.ShopDTO> resultList = new ArrayList<>();
        String name = "";
        for (Map.Entry<String, List<BiSalesMonitoringTableDTO.ViewDTO>> entry: listMap.entrySet()) {
            List<BiSalesMonitoringTableDTO.ViewDTO> value = entry.getValue();
            BiSalesMonitoringTableVO.ShopDTO vo = new BiSalesMonitoringTableVO.ShopDTO();
            BeanUtils.copyProperties(value.get(0),vo);
            //获取结果集
            Pair<String, BiSalesMonitoringTableVO.CommonDTO> pair = handleMonitoring(value, entity);
            if (ObjectUtils.isNotEmpty(pair)) {
                BiSalesMonitoringTableVO.CommonDTO biSalesMonitoringTableVO = pair.getValue();
                if (StringUtils.isBlank(name)) {
                    name = pair.getKey();
                }
                BeanUtils.copyProperties(biSalesMonitoringTableVO,vo);
                vo.setShopName(value.get(0).getTypeName());
                resultList.add(vo);
            }
        }
        seriesVO.setName(name);
        seriesVO.setData(resultList);
    }

    /**
     * 品牌销售额监控设置数据查询
     */
    private void listPlatformMonitoring(List<BiSalesMonitoringTableDTO.ViewDTO> list,BiSalesMonitoringEntity entity,SeriesVO seriesVO) {
        Map<String, List<BiSalesMonitoringTableDTO.ViewDTO>> listMap = list.stream()
                .filter(obj ->StringUtils.isNotBlank(obj.getTypeName()))
                .collect(Collectors.groupingBy(BiSalesMonitoringTableDTO.ViewDTO::getTypeName));
        List<BiSalesMonitoringTableVO.PlatformDTO> resultList = new ArrayList<>();
        String name = "";
        for (Map.Entry<String, List<BiSalesMonitoringTableDTO.ViewDTO>> entry: listMap.entrySet()) {
            List<BiSalesMonitoringTableDTO.ViewDTO> value = entry.getValue();
            BiSalesMonitoringTableVO.PlatformDTO vo = new BiSalesMonitoringTableVO.PlatformDTO();
            BeanUtils.copyProperties(value.get(0),vo);
            //获取结果集
            Pair<String, BiSalesMonitoringTableVO.CommonDTO> pair = handleMonitoring(value, entity);
            if (ObjectUtils.isNotEmpty(pair)) {
                BiSalesMonitoringTableVO.CommonDTO biSalesMonitoringTableVO = pair.getValue();
                if (StringUtils.isBlank(name)) {
                    name = pair.getKey();
                }
                BeanUtils.copyProperties(biSalesMonitoringTableVO,vo);
                vo.setPlatform(value.get(0).getTypeName());
                resultList.add(vo);
            }
        }
        seriesVO.setName(name);
        seriesVO.setData(resultList);
    }

    /**
     * 品牌销售额监控设置数据查询
     */
    private void listCountryMonitoring(List<BiSalesMonitoringTableDTO.ViewDTO> list,BiSalesMonitoringEntity entity,SeriesVO seriesVO) {
        Map<String, List<BiSalesMonitoringTableDTO.ViewDTO>> listMap = list.stream()
                .filter(obj ->StringUtils.isNotBlank(obj.getTypeName()))
                .collect(Collectors.groupingBy(BiSalesMonitoringTableDTO.ViewDTO::getTypeName));

        List<BiSalesMonitoringTableVO.CountryDTO> resultList = new ArrayList<>();
        String name = "";
        for (Map.Entry<String, List<BiSalesMonitoringTableDTO.ViewDTO>> entry: listMap.entrySet()) {
            List<BiSalesMonitoringTableDTO.ViewDTO> value = entry.getValue();
            BiSalesMonitoringTableVO.CountryDTO vo = new BiSalesMonitoringTableVO.CountryDTO();
            BeanUtils.copyProperties(value.get(0),vo);
            //获取结果集
            Pair<String, BiSalesMonitoringTableVO.CommonDTO> pair = handleMonitoring(value, entity);
            if (ObjectUtils.isNotEmpty(pair)) {
                BiSalesMonitoringTableVO.CommonDTO biSalesMonitoringTableVO = pair.getValue();
                if (StringUtils.isBlank(name)) {
                    name = pair.getKey();
                }
                BeanUtils.copyProperties(biSalesMonitoringTableVO,vo);
                vo.setCountryName(value.get(0).getTypeName());
                resultList.add(vo);
            }
        }
        seriesVO.setName(name);
        seriesVO.setData(resultList);
    }

    /**
     * @description: 格式化
     * @author Will
     * @date: 2023/9/20 9:04
     * @param value
     * @param entity
     * @return Pair<CommonDTO>
     */
    private Pair<String, BiSalesMonitoringTableVO.CommonDTO> handleMonitoring (List<BiSalesMonitoringTableDTO.ViewDTO> value,BiSalesMonitoringEntity entity) {

        LocalDate now = LocalDate.now();
        //当前时间年月
        Integer month = now.getMonthValue();
        //上月
        Integer lastMonth =now.minusMonths(1).getMonthValue();
        //上上月
        Integer lastsMonth =  now.minusMonths(2).getMonthValue();

        //本月销售额
        BigDecimal sumSecondMonthSale =  value.stream().filter(obj -> month.equals(obj.getMonth()))
                .map(BiSalesMonitoringTableDTO.ViewDTO::getValue).findFirst().orElse(BigDecimal.ZERO);
        //上月销售额
        BigDecimal sumFirstMonthSale =  value.stream().filter(obj -> lastMonth.equals(obj.getMonth()))
                .map(BiSalesMonitoringTableDTO.ViewDTO::getValue).findFirst().orElse(BigDecimal.ZERO);
        //上上月销售额
        BigDecimal sumLastMonthSale =  value.stream().filter(obj -> lastsMonth.equals(obj.getMonth()))
                .map(BiSalesMonitoringTableDTO.ViewDTO::getValue).findFirst().orElse(BigDecimal.ZERO);

        return setBiSalesMonitoringTableVO(entity, sumSecondMonthSale, sumFirstMonthSale,sumLastMonthSale);
    }

    /**
     * @description: 处理列表头数据
     * @author Will
     * @date: 2023/9/19 16:09
     * @param dto
     * @return LinkedHashMap<Object>
     */
    private LinkedHashMap<String,Object> handleHand (BiSalesMonitoringSearchDTO.ParamDTO dto) {
        LinkedHashMap<String,Object> head = new LinkedHashMap<>();
        head.put(SalesMonitoringTypeEnum.getName(dto.getSearchType()), SalesMonitoringTypeEnum.getDesc(dto.getSearchType()));
        head.put("sumSecondMonthSale","本期");
        head.put("sumFirstMonthSale","上期");
        head.put("sumLastMonthSale","上上期");
        head.put("relativeRatioName","本期环比");
        head.put("lastRelativeRatioName","上期环比");
        return head;
    }

    /**
     * 设置
     */
    private Pair<String,BiSalesMonitoringTableVO.CommonDTO> setBiSalesMonitoringTableVO(BiSalesMonitoringEntity entity,BigDecimal sumSecondMonthSale,BigDecimal sumFirstMonthSale,BigDecimal sumLastMonthSale) {
        String name = "";
        String baseExceed = "(最新月基础值超过";
        String baseNotExceed = "(最新月基础值不超过";
        String qoqNotExceed = "(，环比不超过";
        String qoqNotExceedT = "，环比不超过";
        BiSalesMonitoringTableVO.CommonDTO vo = new BiSalesMonitoringTableVO.CommonDTO();
        //比较最新月基础值
        if (MathUtil.compareTo(entity.getLatestMonthValue(), BigDecimal.ZERO) > 0) {
            //大于等于
            if (BiCompareEnum.GREATER_THAN_EQUAL.getCode().equals(entity.getLatestMonthCompare())) {
                name = name.concat(baseExceed + entity.getLatestMonthValue().setScale(2,2));
                if (!(MathUtil.compareTo(sumSecondMonthSale,entity.getLatestMonthValue()) >= 0)) {
                    return null;
                }
            }
            //小于等于
            if (BiCompareEnum.LESS_THAN_EQUAL.getCode().equals(entity.getLatestMonthCompare())) {
                name = name.concat(baseNotExceed + entity.getLatestMonthValue().setScale(2,2));
                if (!(MathUtil.compareTo(sumSecondMonthSale,entity.getLatestMonthValue()) <= 0)) {
                    return null;
                }
            }
            //大于
            if (BiCompareEnum.GREATER_THAN.getCode().equals(entity.getLatestMonthCompare())) {
                name = name.concat(baseExceed + entity.getLatestMonthValue().setScale(2,2));
                if (!(MathUtil.compareTo(sumSecondMonthSale,entity.getLatestMonthValue()) > 0)) {
                    return null;
                }
            }
            //小于
            if (BiCompareEnum.LESS_THAN.getCode().equals(entity.getLatestMonthCompare())) {
                name = name.concat(baseNotExceed + entity.getLatestMonthValue().setScale(2,2));
                if (!(MathUtil.compareTo(sumSecondMonthSale,entity.getLatestMonthValue()) < 0)) {
                    return null;
                }
            }
            //连续两个月大于等于
            if (BiCompareEnum.TOW_MONTH_GREATER_THEN_EQUAL.getCode().equals(entity.getLatestMonthCompare())) {
                name = name.concat(baseExceed + entity.getLatestMonthValue().setScale(2,2));
                if (!(MathUtil.compareTo(sumFirstMonthSale,entity.getLatestMonthValue()) >= MathUtil.ZERO && MathUtil.compareTo(sumSecondMonthSale,entity.getLatestMonthValue()) >= MathUtil.ZERO)) {
                    return null;
                }
            }
            //连续两个月小于等于
            if (BiCompareEnum.TOW_MONTH_LESS_THEN_EQUAL.getCode().equals(entity.getLatestMonthCompare())) {
                name = name.concat(baseNotExceed + entity.getLatestMonthValue().setScale(2,2));
                if (!(MathUtil.compareTo(sumFirstMonthSale,entity.getLatestMonthValue()) <= MathUtil.ZERO && MathUtil.compareTo(sumSecondMonthSale,entity.getLatestMonthValue()) <= MathUtil.ZERO)) {
                    return null;
                }
            }
        }
            //环比（本期-上期）/上期*100%
            BigDecimal radio = BigDecimal.ZERO;
            if (MathUtil.compareTo(sumFirstMonthSale, BigDecimal.ZERO) != 0) {
                radio = MathUtil.divide(MathUtil.subtract(sumSecondMonthSale,sumFirstMonthSale), sumFirstMonthSale).multiply(MathUtil.BigDecimal_100);
            }else if (MathUtil.compareTo(sumSecondMonthSale, BigDecimal.ZERO) != 0){
                radio = MathUtil.BigDecimal_100;
            }
            //上期环比 (上期－上上期）÷上上期×100%
            BigDecimal lastRadio = BigDecimal.ZERO;
            if (MathUtil.compareTo(sumLastMonthSale, BigDecimal.ZERO) != 0) {
                lastRadio = MathUtil.divide(MathUtil.subtract(sumFirstMonthSale, sumLastMonthSale), sumLastMonthSale).multiply(MathUtil.BigDecimal_100);
            }else if (MathUtil.compareTo(sumFirstMonthSale, BigDecimal.ZERO) != 0){
                lastRadio = MathUtil.BigDecimal_100;
            }

            //比较环比
            if (MathUtil.compareTo(entity.getRelativeRatio(), BigDecimal.ZERO) > 0) {
                //大于等于
                if (BiCompareEnum.GREATER_THAN_EQUAL.getCode().equals(entity.getRelativeRatioCompare())) {
                    if (!(MathUtil.compareTo(radio,entity.getRelativeRatio()) >= 0)) {
                        return null;
                    }
                    if (StringUtils.isNotBlank(name)) {
                        name = name.concat(qoqNotExceedT + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    } else {
                        name = name.concat("(环比超过" + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    }
                }
                //小于等于
                if (BiCompareEnum.LESS_THAN_EQUAL.getCode().equals(entity.getRelativeRatioCompare())) {
                    if (!(MathUtil.compareTo(radio,entity.getRelativeRatio()) <= 0)) {
                        return null;
                    }
                    if (StringUtils.isNotBlank(name)) {
                        name = name.concat(qoqNotExceedT + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    } else {
                        name = name.concat(qoqNotExceed + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    }
                }
                //大于
                if (BiCompareEnum.GREATER_THAN.getCode().equals(entity.getRelativeRatioCompare())) {
                    if (!(MathUtil.compareTo(radio,entity.getRelativeRatio()) > 0)) {
                        return null;
                    }
                    if (StringUtils.isNotBlank(name)) {
                        name = name.concat("，环比超过" + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    } else {
                        name = name.concat("(环比超过" + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    }
                }
                //小于
                if (BiCompareEnum.LESS_THAN.getCode().equals(entity.getRelativeRatioCompare())) {
                    if (!(MathUtil.compareTo(radio,entity.getRelativeRatio()) < 0)) {
                        return null;
                    }
                    if (StringUtils.isNotBlank(name)) {
                        name = name.concat(qoqNotExceedT + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    } else {
                        name = name.concat(qoqNotExceed + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    }
                }
                //连续两个月大于等于
                if (BiCompareEnum.TOW_MONTH_GREATER_THEN_EQUAL.getCode().equals(entity.getRelativeRatioCompare())) {
                    if (!(MathUtil.compareTo(radio, entity.getRelativeRatio()) >= 0 && MathUtil.compareTo(lastRadio, entity.getRelativeRatio()) >= 0) ) {
                        return null;
                    }
                    if (StringUtils.isNotBlank(name)) {
                        name = name.concat(qoqNotExceedT + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    } else {
                        name = name.concat(qoqNotExceed + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    }
                }
                //连续两个月小于等于
                if (BiCompareEnum.TOW_MONTH_LESS_THEN_EQUAL.getCode().equals(entity.getRelativeRatioCompare())) {
                    if (!(MathUtil.compareTo(radio, entity.getRelativeRatio()) <= 0 && MathUtil.compareTo(lastRadio, entity.getRelativeRatio()) <= 0) ) {
                        return null;
                    }
                    if (StringUtils.isNotBlank(name)) {
                        name = name.concat(qoqNotExceedT + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    } else {
                        name = name.concat(qoqNotExceed + entity.getRelativeRatio().setScale(2,2).toString().concat("%"));
                    }
                }
            }
            vo.setSumSecondMonthSale(sumSecondMonthSale);
            vo.setSumFirstMonthSale(sumFirstMonthSale);
            vo.setSumLastMonthSale(sumLastMonthSale);
            vo.setRelativeRatioName(radio.stripTrailingZeros().toPlainString().concat("%"));
            vo.setLastRelativeRatioName(lastRadio.stripTrailingZeros().toPlainString().concat("%"));
            name = StringUtils.isNotBlank(name) ? name.concat(")") : name;
            return new Pair<>(name, vo);
        }


    /**
     * @description: 根据负责人删除配置
     * @author Will
     * @date: 2022/12/30 10:45
     * @param chargeId
     */
    public void removeByChargeId(String chargeId) {
        LambdaUpdateWrapper<BiSalesMonitoringEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(BiSalesMonitoringEntity::getChargeId,chargeId);
        this.remove(updateWrapper);
    }

    /**
     * @description: 根据负责人查询
     * @author Will
     * @date: 2022/12/30 11:34
     * @param chargeId
     * @return List<BiSalesMonitoringEntity>
     */
    public List<BiSalesMonitoringEntity> listByChargeId(String chargeId) {
        LambdaQueryWrapper<BiSalesMonitoringEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSalesMonitoringEntity::getChargeId,chargeId);
        queryWrapper.orderByAsc(BiSalesMonitoringEntity::getId);
        return this.list(queryWrapper);
    }

    /**
     * @description: 根据类型和负责人查询
     * @author Will
     * @date: 2022/12/30 10:34
     * @param type
     * @param chargeId
     * @return BiSalesMonitoringEntity
     */
    public BiSalesMonitoringEntity getByType(Integer type,String chargeId) {
        LambdaQueryWrapper<BiSalesMonitoringEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSalesMonitoringEntity::getType,type);
        queryWrapper.eq(BiSalesMonitoringEntity::getChargeId,chargeId);
        return this.getOne(queryWrapper);
    }
}
