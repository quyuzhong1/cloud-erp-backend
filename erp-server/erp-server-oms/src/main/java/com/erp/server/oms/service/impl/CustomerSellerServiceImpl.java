package com.erp.server.oms.service.impl;

import com.common.business.dto.FindUserDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerSellerEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.CustomerSellerMapper;
import com.erp.server.oms.service.CustomerSellerService;
import com.erp.server.oms.service.OperateLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 客户销售员信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerSellerServiceImpl extends SuperServiceImpl<CustomerSellerMapper, CustomerSellerEntity> implements CustomerSellerService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    /**
     * 检查开始日期 结束日期
     *
     * @param sellerList
     * @return void
     * @author yl
     * @date 2023-05-12 15:04
     */
    @Override
    public void checkDate(List<SellerDTO.AddDTO> sellerList) {
        if (CollectionUtils.isNotEmpty(sellerList)) {
            List<SellerDTO.AddDTO> list = sellerList.stream().filter(c -> c.getEndDate() != null && c.getStartDate() != null).collect(Collectors.toList());
            long count = list.stream().filter(c -> c.getEndDate().compareTo(c.getStartDate()) < 0).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_92008);
            }

        }

    }


    /**
     * 批量保存销售员信息
     *
     * @param mainId
     * @param sellerList
     * @return void
     * @author yl
     * @date 2023-05-12 16:04
     */
    @Override
    public void saveBatchSeller(String mainId, List<SellerDTO.AddDTO> sellerList) {
        if (CollectionUtils.isEmpty(sellerList)) {
            return;
        }
        List<CustomerSellerEntity> addList = BeanMapper.copyList(sellerList, CustomerSellerEntity.class);
        List<String> userIdList = addList.stream().map(CustomerSellerEntity::getSellerId).collect(Collectors.toList());
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
        for (CustomerSellerEntity item : addList) {
            item.setMainId(mainId);
            String userId = item.getSellerId();
            String userName = userList.stream().filter(d -> d.getUserId().equals(userId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
            item.setSellerName(userName);
        }

        this.saveBatch(addList);

    }


    /**
     * 售货员信息
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SellerDTO.ViewDTO>
     * @author yl
     * @date 2023-05-15 10:10
     */
    @Override
    public List<SellerDTO.ViewDTO> listByMainId(String mainId) {
        List<CustomerSellerEntity> list = this.listBaseByMainId(mainId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<SellerDTO.ViewDTO> resultList = BeanMapper.copyList(list, SellerDTO.ViewDTO.class);
        return resultList;
    }


    /**
     * 批量修改发票信息
     * @author yl
     * @date 2023-05-15 11:21
     * @param mainId
     * @param sellerList
     * @return void
     */
    @Override
    public void updateBatchSeller(String mainId, List<SellerDTO.ViewDTO> sellerList) {
        List<CustomerSellerEntity> saveOrUpdateList = new ArrayList<>(sellerList.size());
        //这是修改的
        List<SellerDTO.ViewDTO> updateList = sellerList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<SellerDTO.ViewDTO> addList = sellerList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //这个是要修改的实体
        List<CustomerSellerEntity> updateEntityList = BeanMapper.copyList(updateList, CustomerSellerEntity.class);
        //这个是要添加的
        List<CustomerSellerEntity> addEntityList = BeanMapper.copyList(addList, CustomerSellerEntity.class);
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<CustomerSellerEntity> dbList = this.listBaseByMainId(mainId);
        List<String> deleteIdList = getDeleteIds(updateList, dbList);
        //这是要删除的
        List<CustomerSellerEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<String> userIdList = saveOrUpdateList.stream().map(CustomerSellerEntity::getSellerId).collect(Collectors.toList());
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
        for (CustomerSellerEntity item : saveOrUpdateList) {
            item.setMainId(mainId);
            String userId = item.getSellerId();
            String userName = userList.stream().filter(d -> d.getUserId().equals(userId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
            item.setSellerName(userName);
        }

        for (SellerDTO.ViewDTO seller : addList) {
            String userId = seller.getSellerId();
            String userName = userList.stream().filter(d -> d.getUserId().equals(userId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
            seller.setSellerName(userName);
        }


        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSellerName())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个联系人【%s】", ModuleTypeEnum.CUSTOMER.getCode(), removePairList, "编辑操作");
        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSellerName())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个联系人【%s】", ModuleTypeEnum.CUSTOMER.getCode(), addPairList, "编辑操作");
        //修改的
        for (CustomerSellerEntity update : updateEntityList) {
            String id = update.getId();
            CustomerSellerEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
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
     * @param sellerList
     * @param dbList
     * @return java.util.List<java.lang.String>
     */
    private List<String> getDeleteIds(List<SellerDTO.ViewDTO> sellerList, List<CustomerSellerEntity> dbList) {
        List<String> ids = sellerList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SellerDTO.ViewDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(CustomerSellerEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }

    private List<CustomerSellerEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(CustomerSellerEntity::getMainId, mainId).orderByDesc(CustomerSellerEntity::getId).list();
    }
}
