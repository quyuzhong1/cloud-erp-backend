package com.erp.server.oms.service.impl;

import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.constant.SqlConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.CustomerContactDTO;
import com.erp.model.oms.entity.CustomerB2cContactEntity;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.CustomerB2cContactMapper;
import com.erp.server.oms.service.CustomerB2cContactService;
import com.erp.server.oms.service.OperateLogService;
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
 * 客户联系人信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerB2cContactServiceImpl extends SuperServiceImpl<CustomerB2cContactMapper, CustomerB2cContactEntity> implements CustomerB2cContactService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CustomerB2cContactService customerB2cContactService;

    /**
     * 检查客户默认联系人是否多个
     *
     * @param contactList
     * @return void
     * @author yl
     * @date 2023-05-12 14:00
     */
    @Override
    public void checkIsDefault(List<CustomerContactDTO.AddDTO> contactList) {
        if (CollectionUtils.isNotEmpty(contactList)) {
            long count = contactList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
            if (count > 1) {
                throw new ServiceException(ApiError.ERROR_92005);
            }
        }

    }


    /**
     * 保存联系人信息
     *
     * @param mainId
     * @param contactList
     * @return void
     * @author yl
     * @date 2023-05-12 15:53
     */
    @Override
    public void saveBatchContact(String mainId, List<CustomerContactDTO.AddDTO> contactList) {
        if (CollectionUtils.isEmpty(contactList)) {
            return;
        }
        List<CustomerB2cContactEntity> addList = BeanMapper.copyList(contactList, CustomerB2cContactEntity.class);
        for (CustomerB2cContactEntity addDTO : addList) {
            addDTO.setMainId(mainId);
            //生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_KHLXRC);
            addDTO.setCode(code);
        }
        customerB2cContactService.saveBatch(addList);
    }


    /**
     * 获取联系人信息
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.CustomerContactDTO.ViewDTO>
     * @author yl
     * @date 2023-05-15 9:53
     */
    @Override
    public List<CustomerContactDTO.ViewDTO> listByMainId(String mainId) {
        List<CustomerB2cContactEntity> dbList = this.listBaseByMainId(mainId);
        if (CollectionUtils.isEmpty(dbList)) {
            return Collections.emptyList();
        }
        return BeanMapper.copyList(dbList, CustomerContactDTO.ViewDTO.class);
    }

    @Override
    public List<CustomerB2cContactEntity> listEntityByMainId(String mainId) {
        List<CustomerB2cContactEntity> dbList = this.listBaseByMainId(mainId);
        if (CollectionUtils.isEmpty(dbList)) {
            return Collections.emptyList();
        }
        return dbList;
    }

    /**
     * 修改联系人信息
     *
     * @param mainId
     * @param contactList
     * @return void
     * @author yl
     * @date 2023-05-15 10:57
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchContact(String mainId, List<CustomerContactDTO.ViewDTO> contactList) {
        List<CustomerB2cContactEntity> saveOrUpdateList = new ArrayList<>(contactList.size());
        //这是修改的
        List<CustomerContactDTO.ViewDTO> updateList = contactList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<CustomerContactDTO.ViewDTO> addList = contactList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //这个是要修改的实体
        List<CustomerB2cContactEntity> updateEntityList = BeanMapper.copyList(updateList, CustomerB2cContactEntity.class);
        //这个是要添加的
        List<CustomerB2cContactEntity> addEntityList = BeanMapper.copyList(addList, CustomerB2cContactEntity.class);
        addEntityList.forEach(req -> {
            //生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_KHLXRC);
            req.setCode(code);
        });
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<CustomerB2cContactEntity> dbList = this.listBaseByMainId(mainId);
        List<String> deleteIdList = getDeleteIds(updateList, dbList);
        //这是要删除的
        List<CustomerB2cContactEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        saveOrUpdateList.forEach(s -> s.setMainId(mainId));

        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getPerson())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个联系人【%s】", ModuleTypeEnum.CUSTOMER_B2C.getCode(), removePairList, "编辑操作");
        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getPerson())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个联系人【%s】", ModuleTypeEnum.CUSTOMER_B2C.getCode(), addPairList, "编辑操作");
        //修改的
        for (CustomerB2cContactEntity update : updateEntityList) {
            String id = update.getId();
            CustomerB2cContactEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.CUSTOMER_B2C.getCode(), mainId, "", "");
            }
        }
        if (CollectionUtils.isNotEmpty(saveOrUpdateList)) {
            customerB2cContactService.saveOrUpdateBatch(saveOrUpdateList);
        }
    }


    /**
     * 获取删除字段的信息
     *
     * @param contactList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-15 11:04
     */
    private List<String> getDeleteIds(List<CustomerContactDTO.ViewDTO> contactList, List<CustomerB2cContactEntity> dbList) {
        List<String> ids = contactList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CustomerContactDTO.ViewDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(CustomerB2cContactEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<CustomerB2cContactEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(CustomerB2cContactEntity::getMainId, mainId).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateEntity(PlatformOrderDTO dto, CustomerB2cEntity mainEntity, SoB2cReceiverEntity receiverEntity) {
        CustomerB2cContactEntity entity = this.getByMainId(mainEntity.getId());
        if (null == entity){
            CustomerB2cContactEntity newEntity = new CustomerB2cContactEntity();
            newEntity.setMainId(mainEntity.getId());
            newEntity.setPerson(receiverEntity.getName());
            newEntity.setTelNumber(receiverEntity.getTelNumber());
            newEntity.setEmail(receiverEntity.getEmail());
            newEntity.setIsDefault(true);
            newEntity.setDisabled(false);
            if (!save(newEntity)){
                throw new ServiceException("[CustomerB2cSellerEntity] 保存失败");
            }
        } else {
            if (StringUtils.isBlank(entity.getPerson())){
                entity.setPerson(receiverEntity.getName());
            }
            if (StringUtils.isBlank(entity.getTelNumber())){
                entity.setTelNumber(receiverEntity.getTelNumber());
            }
            if (StringUtils.isBlank(entity.getEmail())){
                entity.setEmail(receiverEntity.getEmail());
            }
            if (Boolean.FALSE.equals(entity.getIsDefault())){
                entity.setIsDefault(true);
            }
            if (Boolean.TRUE.equals(entity.getDisabled())){
                entity.setIsDefault(false);
            }
            updateById(entity);
        }
    }

    @Override
    public CustomerB2cContactEntity getByMainId(String mainId) {
        return lambdaQuery().eq(CustomerB2cContactEntity::getMainId, mainId).last( SqlConstants.LIMIT_1).one();
    }

}
