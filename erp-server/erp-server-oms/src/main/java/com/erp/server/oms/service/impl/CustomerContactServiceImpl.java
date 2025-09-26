package com.erp.server.oms.service.impl;

import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.CustomerContactDTO;
import com.erp.model.oms.entity.CustomerContactEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerContactService;
import com.erp.server.oms.mapper.CustomerContactMapper;
import com.erp.server.oms.service.CustomerContactService;
import com.erp.server.oms.service.OperateLogService;
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

    @Resource
    private SyncKingdeeCustomerContactService syncKingdeeCustomerContactService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

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
        if (CollectionUtils.isEmpty(contactList)){
            return;
        }
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
//            String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.KHLXR, BusinessNoTypeEnum.CODE_KHLXR.getCode()));
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_KHLXR);
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<DmpPushTaskEntity> updateBatchContact(String mainId, List<CustomerContactDTO.ViewDTO> contactList) {
        List<DmpPushTaskEntity> pushTaskList = new ArrayList<>();

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
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_KHLXR);
            req.setCode(code);
        });
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<CustomerContactEntity> dbList = this.listBaseByMainId(mainId);
        List<String> deleteIdList = getDeleteIds(updateList, dbList);
        //这是要删除的
        List<CustomerContactEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            //删除联系人发送金蝶
            removeList.forEach(obj -> {
                DmpPushTaskEntity pushTaskEntity = syncKingdeeCustomerContactService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DELETE.getCode());
                pushTaskList.add(pushTaskEntity);
            });
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
        return pushTaskList;
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
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(CustomerContactEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), CustomerContactEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }
}
