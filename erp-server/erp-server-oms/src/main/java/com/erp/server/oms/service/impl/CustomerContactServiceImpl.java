package com.erp.server.oms.service.impl;

import com.common.business.constant.BusinessNoConstant;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.CustomerContactDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerContactEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerContactService;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerGroupService;
import com.erp.server.oms.mapper.CustomerContactMapper;
import com.erp.server.oms.service.CustomerContactService;
import com.erp.server.oms.service.OperateLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
public class CustomerContactServiceImpl extends SuperServiceImpl<CustomerContactMapper, CustomerContactEntity> implements CustomerContactService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SysUserFeign sysUserFeign;
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
        long count = contactList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_92005);
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
        List<CustomerContactEntity> addList = BeanMapper.copyList(contactList, CustomerContactEntity.class);
        for (CustomerContactEntity addDTO : addList) {
            addDTO.setMainId(mainId);
            //生成单号
            String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.KHLXR, BusinessNoTypeEnum.CODE_KHLXR.getCode()));
            addDTO.setCode(code);
        }
        this.saveBatch(addList);
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
        List<CustomerContactEntity> dbList = this.listBaseByMainId(mainId);
        if (CollectionUtils.isEmpty(dbList)) {
            return Collections.emptyList();
        }
        List<CustomerContactDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, CustomerContactDTO.ViewDTO.class);
        return resultList;
    }

    @Override
    public List<CustomerContactEntity> listEntityByMainId(String mainId) {
        List<CustomerContactEntity> dbList = this.listBaseByMainId(mainId);
        if (CollectionUtils.isEmpty(dbList)) {
            return Collections.emptyList();
        }
        return dbList;
    }

    /**
     * 修改联系人信息
     * @author yl
     * @date 2023-05-15 10:57
     * @param mainId
     * @param contactList
     * @return void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchContact(String mainId, List<CustomerContactDTO.ViewDTO> contactList) {
        List<CustomerContactEntity> saveOrUpdateList = new ArrayList<>(contactList.size());
        //这是修改的
        List<CustomerContactDTO.ViewDTO> updateList = contactList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<CustomerContactDTO.ViewDTO> addList = contactList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //这个是要修改的实体
        List<CustomerContactEntity> updateEntityList = BeanMapper.copyList(updateList, CustomerContactEntity.class);
        //这个是要添加的
        List<CustomerContactEntity> addEntityList = BeanMapper.copyList(addList, CustomerContactEntity.class);
        addEntityList.forEach(req -> {
            //生成单号
            String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.KHLXR, BusinessNoTypeEnum.CODE_KHLXR.getCode()));
            req.setCode(code);
        });
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<CustomerContactEntity> dbList = this.listBaseByMainId(mainId);
        List<String> deleteIdList = getDeleteIds(updateList, dbList);
        //这是要删除的
        List<CustomerContactEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        saveOrUpdateList.forEach(s -> s.setMainId(mainId));

        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getPerson())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个联系人【%s】", ModuleTypeEnum.CUSTOMER.getCode(), removePairList, "编辑操作");
        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getPerson())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个联系人【%s】", ModuleTypeEnum.CUSTOMER.getCode(), addPairList, "编辑操作");
        //修改的
        for (CustomerContactEntity update : updateEntityList) {
            String id = update.getId();
            CustomerContactEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if(old!=null){
                operateLogService.addModuleOperateLogByObj(old,update, ModuleTypeEnum.CUSTOMER.getCode(),mainId,"","");
            }
        }
        if(CollectionUtils.isNotEmpty(saveOrUpdateList)){
            this.saveOrUpdateBatch(saveOrUpdateList);
        }
    }

    
    /**
     * 获取删除字段的信息
     * @author yl
     * @date 2023-05-15 11:04
     * @param contactList
     * @param dbList
     * @return java.util.List<java.lang.String>
     */
    private List<String> getDeleteIds(List<CustomerContactDTO.ViewDTO> contactList, List<CustomerContactEntity> dbList) {
        List<String> ids = contactList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CustomerContactDTO.ViewDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(CustomerContactEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<CustomerContactEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(CustomerContactEntity::getMainId, mainId).list();
    }

    @Override
    public Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus, String syncKingdeeId, String syncOperate) {
        return this.lambdaUpdate()
                .eq(CustomerContactEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeStatus), CustomerContactEntity::getSyncKingdeeStatus, syncKingdeeStatus)
                .set(StringUtils.isNotBlank(syncKingdeeStatus), CustomerContactEntity::getSyncKingdeeTime, LocalDateTime.now())
                .set(StringUtils.isNotBlank(syncKingdeeId), CustomerContactEntity::getSyncKingdeeId, syncKingdeeId)
                .set(StringUtils.isNotBlank(syncOperate), CustomerContactEntity::getSyncOperate, syncOperate)
                .update();
    }
}
