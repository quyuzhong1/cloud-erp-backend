package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.InvoiceDTO;
import com.erp.model.oms.entity.CustomerInvoiceEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.CustomerInvoiceMapper;
import com.erp.server.oms.service.CustomerInvoiceService;
import com.erp.server.oms.service.OperateLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 客户发票信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerInvoiceServiceImpl extends SuperServiceImpl<CustomerInvoiceMapper, CustomerInvoiceEntity> implements CustomerInvoiceService {


    @Resource
    private OperateLogService operateLogService;
    /**
     * 检查默认银行是否是多个
     *
     * @param invoiceList
     * @return void
     * @author yl
     * @date 2023-05-12 14:59
     */
    @Override
    public void checkIsDefault(List<InvoiceDTO.AddDTO> invoiceList) {
        long count = invoiceList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_92007);
        }

    }

    /**
     * 批量保存发票信息
     *
     * @param mainId
     * @param invoiceList
     * @return void
     * @author yl
     * @date 2023-05-12 16:01
     */
    @Override
    public void saveBatchInvoice(String mainId, List<InvoiceDTO.AddDTO> invoiceList) {
        if (CollectionUtils.isEmpty(invoiceList)) {
            return;
        }
        List<CustomerInvoiceEntity> addList = BeanMapper.copyList(invoiceList, CustomerInvoiceEntity.class);
        addList.forEach(c -> c.setMainId(mainId));
        this.saveBatch(addList);
    }


    /**
     * 根据主表信息 获取发票信息
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.InvoiceDTO.ViewDTO>
     * @author yl
     * @date 2023-05-15 10:06
     */
    @Override
    public List<InvoiceDTO.ViewDTO> listByMainId(String mainId) {
        List<CustomerInvoiceEntity> dbList = this.listBaseByMainId(mainId);
        List<InvoiceDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, InvoiceDTO.ViewDTO.class);
        return resultList;
    }

    /**
     * 批量修改发票信息
     * @author yl
     * @date 2023-05-15 11:16
     * @param mainId
     * @param invoiceList
     * @return void
     */
    @Override
    public void updateBatchInvoice(String mainId, List<InvoiceDTO.ViewDTO> invoiceList) {
        if (CollectionUtils.isEmpty(invoiceList)) {
            return;
        }
        List<CustomerInvoiceEntity> saveOrUpdateList = new ArrayList<>(invoiceList.size());
        //这是修改的
        List<InvoiceDTO.ViewDTO> updateList = invoiceList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<InvoiceDTO.ViewDTO> addList = invoiceList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //这个是要修改的实体
        List<CustomerInvoiceEntity> updateEntityList = BeanMapper.copyList(updateList, CustomerInvoiceEntity.class);
        //这个是要添加的
        List<CustomerInvoiceEntity> addEntityList = BeanMapper.copyList(addList, CustomerInvoiceEntity.class);
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<CustomerInvoiceEntity> dbList = this.listBaseByMainId(mainId);
        List<String> deleteIdList = getDeleteIds(updateList, dbList);
        //这是要删除的
        List<CustomerInvoiceEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        saveOrUpdateList.forEach(s -> s.setMainId(mainId));

        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getHead())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个发票【%s】", ModuleTypeEnum.CUSTOMER.getCode(), removePairList, "编辑操作");
        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getHead())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个联系人【%s】", ModuleTypeEnum.CUSTOMER.getCode(), addPairList, "编辑操作");
        //修改的
        for (CustomerInvoiceEntity update : updateEntityList) {
            String id = update.getId();
            CustomerInvoiceEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if(old!=null){
                operateLogService.addModuleOperateLogByObj(old,update, ModuleTypeEnum.CUSTOMER.getCode(),mainId,"","");
            }
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }

    /**
     * 获取删除字段的信息
     * @author yl
     * @date 2023-05-15 11:04
     * @param invoiceList
     * @param dbList
     * @return java.util.List<java.lang.String>
     */
    private List<String> getDeleteIds(List<InvoiceDTO.ViewDTO> invoiceList, List<CustomerInvoiceEntity> dbList) {
        List<String> ids = invoiceList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(InvoiceDTO.ViewDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(CustomerInvoiceEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<CustomerInvoiceEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(CustomerInvoiceEntity::getMainId, mainId).list();
    }
}
