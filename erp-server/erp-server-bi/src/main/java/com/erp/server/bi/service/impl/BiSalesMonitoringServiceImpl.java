package com.erp.server.bi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.common.business.interceptor.CommonInterceptor;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.model.bi.dto.BiSalesMonitoringDTO;
import com.erp.model.bi.dto.BiSalesMonitoringTableDTO;
import com.erp.model.bi.entity.BiModuleEntity;
import com.erp.model.bi.entity.BiSalesMonitoringEntity;
import com.erp.model.bi.vo.*;
import com.erp.server.bi.enums.BiCompareEnum;
import com.erp.server.bi.enums.SalesMonitoringTypeEnum;
import com.erp.server.bi.mapper.BiSalesMonitoringMapper;
import com.erp.server.bi.service.BiModuleService;
import com.erp.server.bi.service.BiSalesMonitoringService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/29 16:24
 */
@Service
public class BiSalesMonitoringServiceImpl extends ServiceImpl<BiSalesMonitoringMapper, BiSalesMonitoringEntity>
        implements BiSalesMonitoringService {

    @Resource
    private BiModuleService biModuleService;

    @Override
    public Boolean batchAdd(List<BiSalesMonitoringDTO> list) {
        if (CollectionUtils.isEmpty(list) || list.size() == 0) {
            throw new ServiceException(ApiError.ERROR_97016);
        }
        List<BiSalesMonitoringEntity> entityList = new ArrayList<>();
        //当前登录人
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_403);
        }
        Map<Integer, List<BiSalesMonitoringDTO>> map = list.stream().collect(Collectors.groupingBy(BiSalesMonitoringDTO::getType));
        for (Map.Entry<Integer, List<BiSalesMonitoringDTO>> entry:map.entrySet()) {
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
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_403);
        }
        //数据较少，可删除后重新新增
        removeByChargeId(loginUser.getUid());
        if (CollectionUtils.isNotEmpty(list) && list.size() > 0) {
            this.batchAdd(list);
        }
    }

    @Override
    public List<BiSalesMonitoringDTO> listBiSalesMonitoring() {
        List<BiSalesMonitoringDTO> resultList = new ArrayList<>();
        //当前登录人
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
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
    public ChartVO listBiSalesMonitoringView(String moduleId) {
        //当前登录人
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_403);
        }
        BiModuleEntity biModuleEntity = biModuleService.getById(moduleId);
        if (ObjectUtils.isEmpty(biModuleEntity)) {
            return new ChartVO();
        }
        ChartVO viewVO = new ChartVO();
        List<BiSalesMonitoringEntity> list = listByChargeId(loginUser.getUid());
        if (CollectionUtils.isEmpty(list)) {
            return new ChartVO();
        }
        List<BiSalesMonitoringTableDTO> biSalesMonitoringTableList = this.baseMapper.listBiSalesMonitoringTable();
        if (CollectionUtils.isEmpty(biSalesMonitoringTableList)) {
            return new ChartVO();
        }
        List<String> head = new ArrayList<>();
        SeriesVO seriesVO = new SeriesVO<>();
        if (biModuleEntity.getName().equals(SalesMonitoringTypeEnum.SALESQTYMONITORING.getDesc())) {
            BiSalesMonitoringEntity  entity= list.stream().filter(obj -> obj.getType().equals(SalesMonitoringTypeEnum.SALESQTYMONITORING.getCode())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(entity)) {
                this.listSalesMonitoring(head,biSalesMonitoringTableList,entity,seriesVO,MathUtil.ONE,null,"销量监控");
            }
        }
        if (biModuleEntity.getName().equals(SalesMonitoringTypeEnum.SALESAMOUNTMONITORING.getDesc())) {
            BiSalesMonitoringEntity entity = list.stream().filter(obj -> obj.getType().equals(SalesMonitoringTypeEnum.SALESAMOUNTMONITORING.getCode())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(entity)) {
                this.listSalesMonitoring(head, biSalesMonitoringTableList, entity, seriesVO, MathUtil.TWO, null, "销售额监控");
            }
        }
        if (biModuleEntity.getName().equals(SalesMonitoringTypeEnum.NEWPRODUCTSMONITORING.getDesc())) {
            BiSalesMonitoringEntity entity = list.stream().filter(obj -> obj.getType().equals(SalesMonitoringTypeEnum.NEWPRODUCTSMONITORING.getCode())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(entity)) {
                this.listSalesMonitoring(head,biSalesMonitoringTableList,entity,seriesVO,MathUtil.TWO,null,"新品销售额监控");
            }
        }
        if (biModuleEntity.getName().equals(SalesMonitoringTypeEnum.OLDPRODUCTSMONITORING.getDesc())) {
            BiSalesMonitoringEntity entity = list.stream().filter(obj -> obj.getType().equals(SalesMonitoringTypeEnum.OLDPRODUCTSMONITORING.getCode())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(entity)) {
                this.listSalesMonitoring(head, biSalesMonitoringTableList, entity, seriesVO, MathUtil.TWO, null, "老品销售额监控");
            }
        }
        if (biModuleEntity.getName().equals(SalesMonitoringTypeEnum.BRANDNAMEMONITORING.getDesc())) {
            BiSalesMonitoringEntity entity = list.stream().filter(obj -> obj.getType().equals(SalesMonitoringTypeEnum.BRANDNAMEMONITORING.getCode())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(entity)) {
                this.listBrandNameMonitoring(head, biSalesMonitoringTableList, entity, seriesVO);
            }
        }
        if (biModuleEntity.getName().equals(SalesMonitoringTypeEnum.CATEGORYMONITORING.getDesc())) {
            BiSalesMonitoringEntity entity = list.stream().filter(obj -> obj.getType().equals(SalesMonitoringTypeEnum.CATEGORYMONITORING.getCode())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(entity)) {
                this.listCategoryMonitoring(head, biSalesMonitoringTableList, entity, seriesVO);
            }
        }
        if (biModuleEntity.getName().equals(SalesMonitoringTypeEnum.CHARGENAMEMONITORING.getDesc())) {
            BiSalesMonitoringEntity entity = list.stream().filter(obj -> obj.getType().equals(SalesMonitoringTypeEnum.CHARGENAMEMONITORING.getCode())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(entity)) {
                this.listChargeNameMonitoring(head, biSalesMonitoringTableList, entity, seriesVO);
            }
        }
        viewVO.setXAxis(head);
        viewVO.setSeries(Arrays.asList(seriesVO));
        return viewVO;
    }

    /**
     * 销量或销售额监控设置数据查询
     */
    private void listSalesMonitoring(List<String> head,List<BiSalesMonitoringTableDTO> biSalesMonitoringTableList,BiSalesMonitoringEntity entity,SeriesVO seriesVO,Integer type,Integer isNewProduct,String name) {
        //type:1代表销量,2代表销售额
        Map<String, List<BiSalesMonitoringTableDTO>> listMap = biSalesMonitoringTableList.stream()
                .filter(obj ->
                        StringUtils.isNotBlank(obj.getSkuNo())
                        && StringUtils.isNotBlank(obj.getItemName())
                        && ObjectUtils.isNotEmpty(obj.getPlatformCreateTime())
                        && (ObjectUtils.isNotEmpty(isNewProduct) ? isNewProduct.equals(obj.getNewSign()) : true)
                )
                .collect(Collectors.groupingBy(obj -> obj.getSkuNo().concat(",").concat(obj.getItemName())));
        LocalDate now = LocalDate.now();
        //当前时间年月
        String month = String.valueOf(now.getYear())+ now.getMonth();
        String lastMonth = String.valueOf(LocalDateUtil.getLastMonthStart(now).getYear()) + now.getMonth();
        String year = String.valueOf(now.getYear());
        List<BiSalesMonitoringTableVO> resultList = new ArrayList<>();
        if (MathUtil.ONE.equals(type)) {
            head.addAll(Arrays.asList("排名","SKU","品名","年累计销量","上个月销量","下个月销量","销量环比"));
        } else {
            head.addAll(Arrays.asList("排名","SKU","品名","年累计销售额","上个月销售额","下个月销售额","销售额环比"));
        }
        Integer seq = MathUtil.ONE;
        for (Map.Entry<String, List<BiSalesMonitoringTableDTO>> entry: listMap.entrySet()) {
            List<BiSalesMonitoringTableDTO> value = entry.getValue();
            BiSalesMonitoringTableVO vo = new BiSalesMonitoringTableVO();
            BeanUtils.copyProperties(value.get(0),vo);
            //本月销量
            BigDecimal sumSecondMonthSale =  value.stream().filter(obj -> month.equals(String.valueOf(obj.getPlatformCreateTime().getYear())+ obj.getPlatformCreateTime().getMonth()))
                    .map(obj-> MathUtil.ONE.equals(type)? new BigDecimal(obj.getQuantity()) : MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            //上月销量
            BigDecimal sumFirstMonthSale =  value.stream().filter(obj -> lastMonth.equals(String.valueOf(obj.getPlatformCreateTime().getYear())+ obj.getPlatformCreateTime().getMonth()))
                    .map(obj-> MathUtil.ONE.equals(type)? new BigDecimal(obj.getQuantity()) : MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            //全年销量
            BigDecimal sumYearSale =  value.stream().filter(obj -> year.equals(String.valueOf(obj.getPlatformCreateTime().getYear())))
                    .map(obj-> MathUtil.ONE.equals(type)? new BigDecimal(obj.getQuantity()) : MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            BiSalesMonitoringTableVO biSalesMonitoringTableVO = setBiSalesMonitoringTableVO(entity, sumSecondMonthSale, sumFirstMonthSale, sumYearSale);
            if (ObjectUtils.isNotEmpty(biSalesMonitoringTableVO)) {
                biSalesMonitoringTableVO.setSeq(seq);
                resultList.add(biSalesMonitoringTableVO);
                seq++;
            }
        }
        seriesVO.setName(name);
        seriesVO.setData(resultList);
    }

    /**
     * 品牌销售额监控设置数据查询
     */
    private void listBrandNameMonitoring(List<String> head,List<BiSalesMonitoringTableDTO> biSalesMonitoringTableList,BiSalesMonitoringEntity entity,SeriesVO seriesVO) {
        Map<String, List<BiSalesMonitoringTableDTO>> listMap = biSalesMonitoringTableList.stream().filter(obj -> ObjectUtils.isNotEmpty(obj.getBrandId()) && ObjectUtils.isNotEmpty(obj.getPlatformCreateTime())).collect(Collectors.groupingBy(obj -> obj.getBrandId()));
        LocalDate now = LocalDate.now();
        //当前时间年月
        String month = String.valueOf(now.getYear())+ now.getMonth();
        String lastMonth = String.valueOf(LocalDateUtil.getLastMonthStart(now).getYear()) + now.getMonth();
        String year = String.valueOf(now.getYear());
        head.addAll(Arrays.asList("排名","品牌","年累计销售额","上个月销售额","下个月销售额","销售额环比"));
        List<BiBrandNameMonitoringTableVO> resultList = new ArrayList<>();
        Integer seq = MathUtil.ONE;
        for (Map.Entry<String, List<BiSalesMonitoringTableDTO>> entry: listMap.entrySet()) {
            List<BiSalesMonitoringTableDTO> value = entry.getValue();
            BiBrandNameMonitoringTableVO vo = new BiBrandNameMonitoringTableVO();
            //本月销售额
            BigDecimal sumSecondMonthSale =  value.stream().filter(obj -> month.equals(String.valueOf(obj.getPlatformCreateTime().getYear())+ obj.getPlatformCreateTime().getMonth()))
                    .map(obj-> MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            //上月销售额
            BigDecimal sumFirstMonthSale =  value.stream().filter(obj -> lastMonth.equals(String.valueOf(obj.getPlatformCreateTime().getYear())+ obj.getPlatformCreateTime().getMonth()))
                    .map(obj-> MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            //全年销售额
            BigDecimal sumYearSale =  value.stream().filter(obj -> year.equals(String.valueOf(obj.getPlatformCreateTime().getYear())))
                    .map(obj-> MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            BiSalesMonitoringTableVO biSalesMonitoringTableVO = setBiSalesMonitoringTableVO(entity, sumSecondMonthSale, sumFirstMonthSale, sumYearSale);
            if (ObjectUtils.isNotEmpty(biSalesMonitoringTableVO)) {
                BeanUtils.copyProperties(biSalesMonitoringTableVO,vo);
                vo.setBrandName(value.get(0).getBrandName());
                vo.setSeq(seq);
                resultList.add(vo);
                seq++;
            }
        }
        seriesVO.setName("品牌销售额监控");
        seriesVO.setData(resultList);
    }

    /**
     * 品类销售额监控设置数据查询
     */
    private void listCategoryMonitoring(List<String> head,List<BiSalesMonitoringTableDTO> biSalesMonitoringTableList,BiSalesMonitoringEntity entity,SeriesVO seriesVO) {
        Map<String, List<BiSalesMonitoringTableDTO>> listMap = biSalesMonitoringTableList.stream().filter(obj -> ObjectUtils.isNotEmpty(obj.getCategoryId()) && ObjectUtils.isNotEmpty(obj.getPlatformCreateTime())).collect(Collectors.groupingBy(obj -> obj.getCategoryId()));
        LocalDate now = LocalDate.now();
        //当前时间年月
        String month = String.valueOf(now.getYear())+ now.getMonth();
        String lastMonth = String.valueOf(LocalDateUtil.getLastMonthStart(now).getYear()) + now.getMonth();
        String year = String.valueOf(now.getYear());
        head.addAll(Arrays.asList("排名","品类","年累计销售额","上个月销售额","下个月销售额","销售额环比"));
        List<BiCategoryMonitoringTableVO> resultList = new ArrayList<>();
        Integer seq = MathUtil.ONE;
        for (Map.Entry<String, List<BiSalesMonitoringTableDTO>> entry: listMap.entrySet()) {
            List<BiSalesMonitoringTableDTO> value = entry.getValue();
            BiCategoryMonitoringTableVO vo = new BiCategoryMonitoringTableVO();
            BeanUtils.copyProperties(value.get(0),vo);
            //本月销售额
            BigDecimal sumSecondMonthSale = value.stream().filter(obj -> month.equals(String.valueOf(obj.getPlatformCreateTime().getYear())+ obj.getPlatformCreateTime().getMonth()))
                    .map(obj-> MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            //上月销售额
            BigDecimal sumFirstMonthSale = value.stream().filter(obj -> lastMonth.equals(String.valueOf(obj.getPlatformCreateTime().getYear())+ obj.getPlatformCreateTime().getMonth()))
                    .map(obj-> MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            //全年销售额
            BigDecimal sumYearSale = value.stream().filter(obj -> year.equals(String.valueOf(obj.getPlatformCreateTime().getYear())))
                    .map(obj-> MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            BiSalesMonitoringTableVO biSalesMonitoringTableVO = setBiSalesMonitoringTableVO(entity, sumSecondMonthSale, sumFirstMonthSale, sumYearSale);
            if (ObjectUtils.isNotEmpty(biSalesMonitoringTableVO)) {
                BeanUtils.copyProperties(biSalesMonitoringTableVO,vo);
                vo.setCategory(value.get(0).getCategory());
                vo.setSeq(seq);
                resultList.add(vo);
                seq++;
            }
        }
        seriesVO.setName("品类销售额监控");
        seriesVO.setData(resultList);
    }

    /**
     * 品牌销售额监控设置数据查询
     */
    private void listChargeNameMonitoring(List<String> head,List<BiSalesMonitoringTableDTO> biSalesMonitoringTableList,BiSalesMonitoringEntity entity,SeriesVO seriesVO) {
        Map<String, List<BiSalesMonitoringTableDTO>> listMap = biSalesMonitoringTableList.stream().filter(obj -> ObjectUtils.isNotEmpty(obj.getChargeId()) && ObjectUtils.isNotEmpty(obj.getPlatformCreateTime())).collect(Collectors.groupingBy(obj -> obj.getChargeId()));
        LocalDate now = LocalDate.now();
        //当前时间年月
        String month = String.valueOf(now.getYear())+ now.getMonth();
        String lastMonth = String.valueOf(LocalDateUtil.getLastMonthStart(now).getYear()) + now.getMonth();
        String year = String.valueOf(now.getYear());
        head.addAll(Arrays.asList("排名","人员","年累计销售额","上个月销售额","下个月销售额","销售额环比"));
        List<BiChargeMonitoringTableVO> resultList = new ArrayList<>();
        Integer seq = MathUtil.ONE;
        for (Map.Entry<String, List<BiSalesMonitoringTableDTO>> entry: listMap.entrySet()) {
            List<BiSalesMonitoringTableDTO> value = entry.getValue();
            BiChargeMonitoringTableVO vo = new BiChargeMonitoringTableVO();
            BeanUtils.copyProperties(value.get(0),vo);
            //本月销售额
            BigDecimal sumSecondMonthSale =  value.stream().filter(obj -> month.equals(String.valueOf(obj.getPlatformCreateTime().getYear())+ obj.getPlatformCreateTime().getMonth()))
                    .map(obj-> MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            //上月销售额
            BigDecimal sumFirstMonthSale =  value.stream().filter(obj -> lastMonth.equals(String.valueOf(obj.getPlatformCreateTime().getYear())+ obj.getPlatformCreateTime().getMonth()))
                    .map(obj-> MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            //全年销售额
            BigDecimal sumYearSale = value.stream().filter(obj -> year.equals(String.valueOf(obj.getPlatformCreateTime().getYear())))
                    .map(obj-> MathUtil.multiply(new BigDecimal(obj.getQuantity()),obj.getSellPrice())).findFirst().orElse(BigDecimal.ZERO);
            BiSalesMonitoringTableVO biSalesMonitoringTableVO = setBiSalesMonitoringTableVO(entity, sumSecondMonthSale, sumFirstMonthSale, sumYearSale);
            if (ObjectUtils.isNotEmpty(biSalesMonitoringTableVO)) {
                BeanUtils.copyProperties(biSalesMonitoringTableVO,vo);
                vo.setChargeName(value.get(0).getChargeName());
                vo.setSeq(seq);
                resultList.add(vo);
                seq++;
            }
        }
        seriesVO.setName("人员销售额监控");
        seriesVO.setData(resultList);
    }

    /**
     * 设置
     */
    private BiSalesMonitoringTableVO setBiSalesMonitoringTableVO(BiSalesMonitoringEntity entity,BigDecimal sumSecondMonthSale,BigDecimal sumFirstMonthSale,BigDecimal sumYearSale) {
        BiSalesMonitoringTableVO vo =new BiSalesMonitoringTableVO();
        //比较最新月基础值
        if (MathUtil.compareTo(entity.getLatestMonthValue(), BigDecimal.ZERO) > 0 ) {
            //大于等于
            if (BiCompareEnum.GREATERTHANEQUAL.getCode().equals(entity.getLatestMonthCompare())) {
                if ((MathUtil.compareTo(sumSecondMonthSale,entity.getLatestMonthValue()) < 0)) {
                    return null;
                }
            }
            //小于等于
            if (BiCompareEnum.LESSTHANEQUAL.getCode().equals(entity.getLatestMonthCompare())) {
                if ((MathUtil.compareTo(sumSecondMonthSale,entity.getLatestMonthValue()) > 0)) {
                    return null;
                }
            }
            //大于
            if (BiCompareEnum.GREATERTHAN.getCode().equals(entity.getLatestMonthCompare())) {
                if ((MathUtil.compareTo(entity.getLatestMonthValue(),sumSecondMonthSale) >= 0)) {
                    return null;
                }
            }
            //小于
            if (BiCompareEnum.LESSTHAN.getCode().equals(entity.getLatestMonthCompare())) {
                if ((MathUtil.compareTo(sumSecondMonthSale,entity.getLatestMonthValue()) >= 0)) {
                    return null;
                }
            }
        }
        //环比（最新月-上个月）/上个月*100%
        BigDecimal radio = BigDecimal.ZERO;
        if (MathUtil.compareTo(sumFirstMonthSale,BigDecimal.ZERO) != 0) {
            radio = MathUtil.divide(MathUtil.subtract(sumSecondMonthSale, sumFirstMonthSale), sumFirstMonthSale).multiply(new BigDecimal(100));
        }
        //比较环比
        if (MathUtil.compareTo(entity.getRelativeRatio(), BigDecimal.ZERO) > 0 ) {
            //大于等于
            if (BiCompareEnum.GREATERTHANEQUAL.getCode().equals(entity.getRelativeRatioCompare())) {
                if ((MathUtil.compareTo(radio,entity.getLatestMonthValue()) < 0)) {
                    return null;
                }
            }
            //小于等于
            if (BiCompareEnum.LESSTHANEQUAL.getCode().equals(entity.getRelativeRatioCompare())) {
                if ((MathUtil.compareTo(radio,entity.getLatestMonthValue()) > 0)) {
                    return null;
                }
            }
            //大于
            if (BiCompareEnum.GREATERTHAN.getCode().equals(entity.getRelativeRatioCompare())) {
                if ((MathUtil.compareTo(entity.getLatestMonthValue(),radio) >= 0)) {
                    return null;
                }
            }
            //小于
            if (BiCompareEnum.LESSTHAN.getCode().equals(entity.getRelativeRatioCompare())) {
                if ((MathUtil.compareTo(radio,entity.getLatestMonthValue()) >= 0)) {
                    return null;
                }
            }
        }
        vo.setSumSecondMonthSale(sumSecondMonthSale);
        vo.setSumFirstMonthSale(sumFirstMonthSale);
        vo.setSumYearSale(sumYearSale);
        vo.setRelativeRatioName(radio.toString().concat("%"));
        return vo;
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
