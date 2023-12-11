package com.erp.server.oms.service.impl;

import com.common.business.dto.FindUserDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.model.oms.entity.CustomerB2cSellerEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.CustomerB2cSellerMapper;
import com.erp.server.oms.service.CustomerB2cSellerService;
import com.erp.server.oms.service.OperateLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
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
public class CustomerB2cSellerServiceImpl extends SuperServiceImpl<CustomerB2cSellerMapper, CustomerB2cSellerEntity> implements CustomerB2cSellerService {

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
            List<LocalDate> dateList = new ArrayList<>(sellerList.size());
            for (SellerDTO.AddDTO item : sellerList) {
                dateList.add(item.getStartDate());
            }
            //判断是否按序排序
            for (int i = 0; i < dateList.size() - 1; i++) {
                if (dateList.get(i).compareTo(dateList.get(i + 1)) > 0) {
                    throw new ServiceException(ApiError.ERROR_92048);
                }
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
        List<CustomerB2cSellerEntity> addList = BeanMapper.copyList(sellerList, CustomerB2cSellerEntity.class);
        List<String> userIdList = addList.stream().map(CustomerB2cSellerEntity::getSellerId).collect(Collectors.toList());
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
        for (CustomerB2cSellerEntity item : addList) {
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
        List<CustomerB2cSellerEntity> list = this.listBaseByMainId(mainId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<SellerDTO.ViewDTO> resultList = BeanMapper.copyList(list, SellerDTO.ViewDTO.class);
        return resultList;
    }


    /**
     * 批量修改发票信息
     *
     * @param mainId
     * @param sellerList
     * @return void
     * @author yl
     * @date 2023-05-15 11:21
     */
    @Override
    public void updateBatchSeller(String mainId, List<SellerDTO.ViewDTO> sellerList) {
        List<CustomerB2cSellerEntity> saveOrUpdateList = new ArrayList<>(sellerList.size());
        //这是修改的
        List<SellerDTO.ViewDTO> updateList = sellerList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<SellerDTO.ViewDTO> addList = sellerList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //这个是要修改的实体
        List<CustomerB2cSellerEntity> updateEntityList = BeanMapper.copyList(updateList, CustomerB2cSellerEntity.class);
        //这个是要添加的
        List<CustomerB2cSellerEntity> addEntityList = BeanMapper.copyList(addList, CustomerB2cSellerEntity.class);
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<CustomerB2cSellerEntity> dbList = this.listBaseByMainId(mainId);
        List<String> deleteIdList = getDeleteIds(updateList, dbList);
        //这是要删除的
        List<CustomerB2cSellerEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<String> userIdList = saveOrUpdateList.stream().map(CustomerB2cSellerEntity::getSellerId).collect(Collectors.toList());
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
        for (CustomerB2cSellerEntity item : saveOrUpdateList) {
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
        operateLogService.batchAddModuleOperateLog("删除了一个联系人【%s】", ModuleTypeEnum.CUSTOMER_B2C.getCode(), removePairList, "编辑操作");
        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSellerName())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个联系人【%s】", ModuleTypeEnum.CUSTOMER_B2C.getCode(), addPairList, "编辑操作");
        //修改的
        for (CustomerB2cSellerEntity update : updateEntityList) {
            String id = update.getId();
            CustomerB2cSellerEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.CUSTOMER_B2C.getCode(), mainId, "", "");
            }
        }
        if (CollectionUtils.isNotEmpty(saveOrUpdateList)) {
            this.saveOrUpdateBatch(saveOrUpdateList);
        }
    }

    /**
     * 添加销售员
     *
     * @param mainId
     * @param dto
     * @param
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSeller(String mainId, SysDepartmentUserNumberDTO dto) {
        List<CustomerB2cSellerEntity> customerSellerList = this.listBaseByMainId(mainId);
        //修改时间
        if (CollectionUtils.isNotEmpty(customerSellerList)) {
            CustomerB2cSellerEntity lastSeller = customerSellerList.get(customerSellerList.size() - 1);
            lastSeller.setEndDate(LocalDate.now());
            this.updateById(lastSeller);
        }

        CustomerB2cSellerEntity customerSeller = new CustomerB2cSellerEntity();
        customerSeller.setSellerName(dto.getUserName());
        customerSeller.setSellerId(dto.getUserId());
        customerSeller.setMainId(mainId);
        customerSeller.setDeptId(dto.getDepartmentId());
        customerSeller.setStartDate(LocalDate.now());
        this.save(customerSeller);

    }


    /**
     * 审核通过后批量添加销售员历史信息
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-07-12 18:01
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSellerHistory(List<CustomerB2cEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //销售员
        List<String> sellerIdList = list.stream().map(CustomerB2cEntity::getSellerId).collect(Collectors.toList());
        List<SysDepartmentUserNumberDTO> deptUserList = sysUserFeign.listDeptUserByUserIdList(sellerIdList);
        //主表信息
        List<String> mainIdList = list.stream().map(CustomerB2cEntity::getId).collect(Collectors.toList());
        //数据库存在的
        List<CustomerB2cSellerEntity> dbSellerList = this.listByMainIdList(mainIdList);
        List<CustomerB2cSellerEntity> batchAddList = new ArrayList<>(10);
        //更改的
        List<CustomerB2cSellerEntity> batchUpdateList = new ArrayList<>(10);

        //添加的
        for (CustomerB2cEntity item : list) {
            String mainId = item.getId();
            SysDepartmentUserNumberDTO deptUser = deptUserList.stream().filter(d -> d.getUserId().equals(item.getSellerId())).
                    findFirst().orElse(null);
            CustomerB2cSellerEntity addSeller = new CustomerB2cSellerEntity();
            if (deptUser != null) {
                addSeller.setSellerName(deptUser.getUserName());
                addSeller.setDeptId(deptUser.getDepartmentId());
            }
            addSeller.setSellerId(item.getSellerId());
            addSeller.setMainId(mainId);
            addSeller.setStartDate(LocalDate.now());
            batchAddList.add(addSeller);
            //存在的销售员 就要修改
            List<CustomerB2cSellerEntity> existSellerList = dbSellerList.stream().filter(s -> s.getMainId().equals(mainId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(existSellerList)) {
                existSellerList.sort(Comparator.comparing(CustomerB2cSellerEntity::getId).reversed());
                batchUpdateList.add(existSellerList.get(0));
            }
        }
        //批量添加
        if (CollectionUtils.isNotEmpty(batchAddList)) {
            this.saveBatch(batchAddList);
        }

        //批量修改
        if (CollectionUtils.isNotEmpty(batchUpdateList)) {
            batchUpdateList.forEach(b->b.setEndDate(LocalDate.now()));
            this.updateBatchById(batchUpdateList);
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateEntity(PlatformOrderDTO dto, CustomerB2cEntity mainEntity) {
        CustomerB2cSellerEntity entity = this.getByMainId(mainEntity.getId());
        if (null == entity){
            CustomerB2cSellerEntity newEntity = new CustomerB2cSellerEntity();
            newEntity.setMainId(mainEntity.getId());
            if (!save(newEntity)){
                throw new ServiceException("[CustomerB2cSellerEntity] 保存失败");
            }
        } else {
            if (!updateById(entity)){
                throw new ServiceException("[CustomerB2cSellerEntity] 更新失败");
            }
        }
    }

    @Override
    public CustomerB2cSellerEntity getByMainId(String mainId) {
        return lambdaQuery().eq(CustomerB2cSellerEntity::getMainId, mainId).last("LIMIT 1").one();
    }

    public List<CustomerB2cSellerEntity> listByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(CustomerB2cSellerEntity::getMainId, mainIdList).list();
    }


    /**
     * 获取删除字段的信息
     *
     * @param sellerList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-15 11:04
     */
    private List<String> getDeleteIds(List<SellerDTO.ViewDTO> sellerList, List<CustomerB2cSellerEntity> dbList) {
        List<String> ids = sellerList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SellerDTO.ViewDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(CustomerB2cSellerEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }

    private List<CustomerB2cSellerEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(CustomerB2cSellerEntity::getMainId, mainId).orderByAsc(CustomerB2cSellerEntity::getId).list();
    }
}
