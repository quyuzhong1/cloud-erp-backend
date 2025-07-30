package com.erp.server.scm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.ImportCommonTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.SupplierPlantAddrDTO;
import com.erp.model.scm.entity.SupplierPlantAddrEntity;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.server.scm.mapper.SupplierPlantAddrMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SupplierPlantAddrService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 供应商工厂地信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-07-21
 */
@Slf4j
@Service
public class SupplierPlantAddrServiceImpl extends SuperServiceImpl<SupplierPlantAddrMapper, SupplierPlantAddrEntity> implements SupplierPlantAddrService {
    @Autowired
    private ModuleOperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SupplierPlantAddrDTO.AddDTO addDTO) {
        SupplierPlantAddrEntity supplierPlantAddrEntity = new SupplierPlantAddrEntity();
        BeanMapperUtils.copy(addDTO, supplierPlantAddrEntity);

        // 数据处理
        handleData(supplierPlantAddrEntity);

        log.info("开始新增供应商工厂地信息");
        boolean save = super.save(supplierPlantAddrEntity);
        if(!save) {
            throw new ServiceException("供应商工厂地信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "供应商工厂地信息" , supplierPlantAddrEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, supplierPlantAddrEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(supplierPlantAddrEntity.getId(), supplierPlantAddrEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SupplierPlantAddrDTO.UpdateDTO addOrUpdateDTO) {
        SupplierPlantAddrEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "供应商工厂地信息"));
        SupplierPlantAddrEntity supplierPlantAddrEntity =  BeanMapperUtils.map(SupplierPlantAddrEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(supplierPlantAddrEntity);
        log.info("编辑 开始修改供应商工厂地信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(supplierPlantAddrEntity);
        if(!save) {
            throw new ServiceException("供应商工厂地信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录供应商工厂地信息日志数据，id：【{}】", supplierPlantAddrEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), supplierPlantAddrEntity.getId(), "供应商工厂地信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, supplierPlantAddrEntity, null, supplierPlantAddrEntity.getId(),"", msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importUpdate(String supplierId, List<SupplierPlantAddrDTO.AddDTO> plantAddrList, String type) {
        if (CollUtil.isEmpty(plantAddrList) && ImportCommonTypeEnum.UPDATE_ALL.getCode().equals(type)) {
            //部分更新时无值则无需更新
            return;
        }
        // 删除原有数据
        this.removeBySupplierId(supplierId);
        log.info("开始批量新增供应商工厂地信息，供应商id：【{}】", supplierId);
        // 批量新增
        List<SupplierPlantAddrEntity> supplierPlantAddrList = BeanUtil.copyToList(plantAddrList,SupplierPlantAddrEntity.class);
        //供应商id赋值
        supplierPlantAddrList.forEach(obj -> obj.setSupplierId(supplierId));
        super.saveBatch(supplierPlantAddrList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateBatchPlantAddr(List<SupplierPlantAddrDTO.AddDTO> plantAddrList, String supplierId) {
        //删除原有工厂地址
        this.removeBySupplierId(supplierId);
        log.info("开始批量新增供应商工厂地信息，供应商id：【{}】", supplierId);
        // 批量新增
        List<SupplierPlantAddrEntity> supplierPlantAddrList = BeanUtil.copyToList(plantAddrList, SupplierPlantAddrEntity.class);
        // 供应商id赋值
        supplierPlantAddrList.forEach(obj -> obj.setSupplierId(supplierId));
        return super.saveBatch(supplierPlantAddrList);
    }

    @Override
    public List<SupplierPlantAddrEntity> listBySupplierId(String supplierId) {
        return lambdaQuery().eq(SupplierPlantAddrEntity::getSupplierId,supplierId).list();
    }

    @Override
    public List<SupplierPlantAddrDTO.ViewDTO> listViewBySupplierIdList(List<String> supplierIdList) {
        if (CollUtil.isEmpty(supplierIdList)) {
            return Collections.emptyList();
        }
        List<SupplierPlantAddrEntity> list = lambdaQuery().in(SupplierPlantAddrEntity::getSupplierId, supplierIdList).list();
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        //国家
        List<String> countryIdList = list.stream().map(SupplierPlantAddrEntity::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> dictCountryList = FeignQuery.getByIds(DictCountryEntity.class, countryIdList);
        Map<String, String> countryMap = CollUtil.isEmpty(dictCountryList) ? new HashMap<>() : dictCountryList.stream().collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getNameCn));
        //省份或城市
        List<String> codeList = list.stream().flatMap(obj -> Stream.of(obj.getRegion(), obj.getCity())).distinct().collect(Collectors.toList());
        List<DictCityEntity> dictCityList = FeignQuery.create(DictCityEntity.class).in(DictCityEntity::getCode,codeList).list();

        List<SupplierPlantAddrDTO.ViewDTO> resultList = new ArrayList<>();
        for (SupplierPlantAddrEntity plantAddrEntity : list) {
            SupplierPlantAddrDTO.ViewDTO viewDTO = new SupplierPlantAddrDTO.ViewDTO();
            viewDTO.setId(plantAddrEntity.getId());
            viewDTO.setSupplierId(plantAddrEntity.getSupplierId());
            //国家
            String countryName = countryMap.get(plantAddrEntity.getCountry());
            viewDTO.setCountryName(countryName);
            //省份
            if (CharSequenceUtil.isNotBlank(plantAddrEntity.getRegion())) {
                String regionName = dictCityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCountryCode(), plantAddrEntity.getCountry()) && CharSequenceUtil.equals(obj.getCode(), plantAddrEntity.getRegion()))
                        .map(DictCityEntity::getName).findFirst().orElse("");
                viewDTO.setRegionName(regionName);
            }
           //城市
            if (CharSequenceUtil.isNotBlank(plantAddrEntity.getCity())) {
                String cityName = dictCityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCountryCode(), plantAddrEntity.getCountry()) && CharSequenceUtil.equals(obj.getCode(), plantAddrEntity.getCity()))
                        .map(DictCityEntity::getName).findFirst().orElse("");
                viewDTO.setCityName(cityName);
            }
            resultList.add(viewDTO);
        }
        return resultList;
    }

    /**
     * 根据供应商id删除
     * @author will
     * @date 2025/7/24 19:11
     * @param supplierId
     * @return void
     */
    private void removeBySupplierId(String supplierId) {
        log.info("开始删除供应商工厂地信息，供应商id：【{}】", supplierId);
        lambdaUpdate().eq(SupplierPlantAddrEntity::getSupplierId,supplierId).remove();
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(SupplierPlantAddrEntity supplierPlantAddrEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
