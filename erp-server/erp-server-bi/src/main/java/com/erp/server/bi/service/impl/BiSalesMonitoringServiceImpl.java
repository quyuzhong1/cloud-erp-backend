package com.erp.server.bi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.MathUtil;
import com.erp.common.business.interceptor.CommonInterceptor;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.model.bi.dto.BiSalesMonitoringDTO;
import com.erp.model.bi.entity.BiSalesMonitoringEntity;
import com.erp.model.bi.vo.BiSalesMonitoringViewVO;
import com.erp.model.bi.vo.SeriesVO;
import com.erp.server.bi.mapper.BiSalesMonitoringMapper;
import com.erp.server.bi.service.BiSalesMonitoringService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    public List<BiSalesMonitoringViewVO> listBiSalesMonitoringView() {
        //当前登录人
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_403);
        }
        List<BiSalesMonitoringEntity> list = listByChargeId(loginUser.getUid());
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        for (BiSalesMonitoringEntity entity : list) {
            SeriesVO<BiSalesMonitoringViewVO> seriesVO = new SeriesVO<>();
           switch (entity.getType()) {
               case 0:
               case 1:this.listSalesQtyMonitoring(entity,seriesVO);
               case 2:
               case 3:
               case 4:
               case 5:
               case 6:
               default:{
                   break;
               }
           }

        }

        return null;
    }

    private void listSalesQtyMonitoring(BiSalesMonitoringEntity entity,SeriesVO<BiSalesMonitoringViewVO> seriesVO) {

        //查询销量监控数据
        if (MathUtil.compareTo(entity.getLatestMonthValue(), BigDecimal.ZERO) > 0 ) {
            //最新月基础值大于0则为参数值


        }

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
