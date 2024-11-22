package com.erp.server.oms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.constant.SqlConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.entity.CustomerB2cAddressEntity;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.CustomerB2cAddressMapper;
import com.erp.server.oms.service.CustomerB2cAddressService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoInfoService;
import groovy.lang.Lazy;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 客户地址信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerB2cAddressServiceImpl extends SuperServiceImpl<CustomerB2cAddressMapper, CustomerB2cAddressEntity> implements CustomerB2cAddressService {


    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Lazy
    @Resource
    private CustomerB2cAddressService customerB2cAddressService;

    /**
     * 检查默认地址是否存在多个
     *
     * @param addressList
     * @return void
     * @author yl
     * @date 2023-05-12 14:55
     */
    @Override
    public void checkIsDefault(List<CustomerAddressDTO.AddDTO> addressList) {
        if (CollectionUtils.isNotEmpty(addressList)) {
            long count = addressList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
            if (count > 1) {
                throw new ServiceException(ApiError.ERROR_92006);
            }
        }

    }

    /**
     * 批量保存地址信息
     *
     * @param mainId
     * @param addressList
     * @return void
     * @author yl
     * @date 2023-05-12 15:56
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchAddress(String mainId, List<CustomerAddressDTO.AddDTO> addressList) {
        if (CollectionUtils.isEmpty(addressList)) {
            return;
        }
        List<CustomerB2cAddressEntity> addList = BeanMapper.copyList(addressList, CustomerB2cAddressEntity.class);
        for (CustomerB2cAddressEntity addDTO : addList) {
            addDTO.setMainId(mainId);
            //生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_KHDZC);
            addDTO.setCode(code);
        }
        customerB2cAddressService.saveBatch(addList);
    }


    /**
     * 根据主表id 获取地址信息
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.CustomerAddressDTO.ViewDTO>
     * @author yl
     * @date 2023-05-15 10:01
     */
    @Override
    public List<CustomerAddressDTO.ViewDTO> listByMainId(String mainId) {
        List<CustomerB2cAddressEntity> dbList = this.listBaseByMainId(mainId);
        if (CollectionUtils.isEmpty(dbList)) {
            return Collections.emptyList();
        }
        return BeanMapper.copyList(dbList, CustomerAddressDTO.ViewDTO.class);
    }


    /**
     * 修改地址信息
     *
     * @param mainId
     * @param addressList
     * @return void
     * @author yl
     * @date 2023-05-15 11:08
     */
    @Override
    public void updateBatchAddress(String mainId, List<CustomerAddressDTO.ViewDTO> addressList) {
        List<CustomerB2cAddressEntity> saveOrUpdateList = new ArrayList<>(addressList.size());
        //这是修改的
        List<CustomerAddressDTO.ViewDTO> updateList = addressList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());

        //这是要添加的
        List<CustomerAddressDTO.ViewDTO> addList = addressList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        for (CustomerAddressDTO.ViewDTO viewDTO : addList) {
            //生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_KHDZC);
            viewDTO.setCode(code);
        }
        //这个是要修改的实体
        List<CustomerB2cAddressEntity> updateEntityList = BeanMapper.copyList(updateList, CustomerB2cAddressEntity.class);
        //这个是要添加的
        List<CustomerB2cAddressEntity> addEntityList = BeanMapper.copyList(addList, CustomerB2cAddressEntity.class);
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<CustomerB2cAddressEntity> dbList = this.listBaseByMainId(mainId);
        List<String> deleteIdList = getDeleteIds(updateList, dbList);
        //这是要删除的
        List<CustomerB2cAddressEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());


        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            int count = soInfoService.getCountByAddressIds(deleteIdList);
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_92046);
            }
            this.removeByIds(deleteIdList);
        }
        saveOrUpdateList.forEach(s -> s.setMainId(mainId));
        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getAddress())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个联系地址【%s】", ModuleTypeEnum.CUSTOMER_B2C.getCode(), removePairList, "编辑操作");
        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getAddress())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个联系地址【%s】", ModuleTypeEnum.CUSTOMER_B2C.getCode(), addPairList, "编辑操作");
        //修改的
        for (CustomerB2cAddressEntity update : updateEntityList) {
            String id = update.getId();
            CustomerB2cAddressEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.CUSTOMER_B2C.getCode(), mainId, "", "");
            }
        }
        if (CollectionUtils.isNotEmpty(saveOrUpdateList)) {
            customerB2cAddressService.saveOrUpdateBatch(saveOrUpdateList);
        }

    }

    @Override
    public CustomerAddressDTO.ViewDTO getCustomerAddressById(String customerAddressId) {
        CustomerB2cAddressEntity entity = this.getById(customerAddressId);
        if (ObjectUtils.isEmpty(entity)) {
            return new CustomerAddressDTO.ViewDTO();
        }
        return BeanMapperUtils.map(CustomerAddressDTO.ViewDTO.class, entity);
    }

        @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateEntity(PlatformOrderDTO dto, CustomerB2cEntity mainEntity, SoB2cReceiverEntity receiverEntity,boolean notUpdateAddress) {
        CustomerB2cAddressEntity entity = this.getByMainId(mainEntity.getId());
        if (null == entity){
            saveEntity(mainEntity, receiverEntity);
        } else {
            udpateEntity(receiverEntity, entity,notUpdateAddress);
        }
    }

    private void udpateEntity(SoB2cReceiverEntity receiverEntity, CustomerB2cAddressEntity entity,boolean notUpdateAddress) {
        if (StringUtils.isBlank(entity.getAddress()) || !notUpdateAddress){
            String address = CharSequenceUtil.concat(true, receiverEntity.getFirstAddress(), receiverEntity.getSecondAddress(), receiverEntity.getFullAddress());
            entity.setAddress(address);
        }
        if (StringUtils.isBlank(entity.getPerson())) {
            entity.setPerson(receiverEntity.getName());
        }
        if (StringUtils.isBlank(entity.getTelNumber())) {
            entity.setTelNumber(receiverEntity.getTelNumber());
        }
        if (Boolean.FALSE.equals(entity.getIsDefault())){
            entity.setIsDefault(true);
        }
        if (Boolean.TRUE.equals(entity.getDisabled())){
            entity.setIsDefault(false);
        }
        if (StringUtils.isBlank(entity.getEmail())){
            entity.setEmail(receiverEntity.getEmail());
        }
        updateById(entity);
    }

    private void saveEntity(CustomerB2cEntity mainEntity, SoB2cReceiverEntity receiverEntity) {
        CustomerB2cAddressEntity newEntity = new CustomerB2cAddressEntity();
        newEntity.setMainId(mainEntity.getId());
        String address = CharSequenceUtil.concat(true, receiverEntity.getFirstAddress(), receiverEntity.getSecondAddress(), receiverEntity.getFullAddress());
        newEntity.setAddress(address);
        newEntity.setPerson(receiverEntity.getName());
        newEntity.setTelNumber(receiverEntity.getTelNumber());
        newEntity.setEmail(receiverEntity.getEmail());
        newEntity.setIsDefault(true);
        newEntity.setDisabled(false);
        if (!save(newEntity)){
            throw new ServiceException("[CustomerB2cAddressEntity] 保存失败");
        }
    }

    @Override
    public CustomerB2cAddressEntity getByMainId(String mainId) {
        return lambdaQuery().eq(CustomerB2cAddressEntity::getMainId, mainId).last( SqlConstants.LIMIT_1).one();
    }

    /**
     * 獲取刪除id
     *
     * @param addressList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-15 11:14
     */
    private List<String> getDeleteIds(List<CustomerAddressDTO.ViewDTO> addressList, List<CustomerB2cAddressEntity> dbList) {
        List<String> ids = addressList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CustomerAddressDTO.ViewDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(CustomerB2cAddressEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<CustomerB2cAddressEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(CustomerB2cAddressEntity::getMainId, mainId).list();
    }
}
