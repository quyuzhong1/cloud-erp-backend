package com.erp.server.oms.service.impl;

import com.common.business.constant.BusinessNoConstant;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.CustomerAddressMapper;
import com.erp.server.oms.service.CustomerAddressService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoInfoService;
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
public class CustomerAddressServiceImpl extends SuperServiceImpl<CustomerAddressMapper, CustomerAddressEntity> implements CustomerAddressService {


    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoInfoService soInfoService;

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
        long count = addressList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_92006);
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
        List<CustomerAddressEntity> addList = BeanMapper.copyList(addressList, CustomerAddressEntity.class);
        for (CustomerAddressEntity addDTO : addList) {
            addDTO.setMainId(mainId);
            //生成单号
            String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.KHDZ, BusinessNoTypeEnum.CODE_KHDZ.getCode()));
            addDTO.setCode(code);
        }
        this.saveBatch(addList);
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
        List<CustomerAddressEntity> dbList = this.listBaseByMainId(mainId);
        if (CollectionUtils.isEmpty(dbList)) {
            return Collections.emptyList();
        }
        List<CustomerAddressDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, CustomerAddressDTO.ViewDTO.class);
        return resultList;
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
        List<CustomerAddressEntity> saveOrUpdateList = new ArrayList<>(addressList.size());
        //这是修改的
        List<CustomerAddressDTO.ViewDTO> updateList = addressList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());

        //这是要添加的
        List<CustomerAddressDTO.ViewDTO> addList = addressList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        for (CustomerAddressDTO.ViewDTO viewDTO : addList) {
            //生成单号
            String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.KHDZ, BusinessNoTypeEnum.CODE_KHDZ.getCode()));
            viewDTO.setCode(code);
        }
        //这个是要修改的实体
        List<CustomerAddressEntity> updateEntityList = BeanMapper.copyList(updateList, CustomerAddressEntity.class);
        //这个是要添加的
        List<CustomerAddressEntity> addEntityList = BeanMapper.copyList(addList, CustomerAddressEntity.class);
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<CustomerAddressEntity> dbList = this.listBaseByMainId(mainId);
        List<String> deleteIdList = getDeleteIds(updateList, dbList);
        //这是要删除的
        List<CustomerAddressEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());


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
        operateLogService.batchAddModuleOperateLog("删除了一个联系地址【%s】", ModuleTypeEnum.CUSTOMER.getCode(), removePairList, "编辑操作");
        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getAddress())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个联系地址【%s】", ModuleTypeEnum.CUSTOMER.getCode(), addPairList, "编辑操作");
        //修改的
        for (CustomerAddressEntity update : updateEntityList) {
            String id = update.getId();
            CustomerAddressEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.CUSTOMER.getCode(), mainId, "", "");
            }
        }
        if(CollectionUtils.isNotEmpty(saveOrUpdateList)){
            this.saveOrUpdateBatch(saveOrUpdateList);
        }

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
    private List<String> getDeleteIds(List<CustomerAddressDTO.ViewDTO> addressList, List<CustomerAddressEntity> dbList) {
        List<String> ids = addressList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CustomerAddressDTO.ViewDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(CustomerAddressEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<CustomerAddressEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(CustomerAddressEntity::getMainId, mainId).list();
    }
}
