package com.erp.server.oms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.InvoiceDTO;
import com.erp.model.oms.entity.CustomerB2cInvoiceEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.CustomerB2cInvoiceMapper;
import com.erp.server.oms.service.CustomerB2cInvoiceService;
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
public class CustomerB2cInvoiceServiceImpl extends SuperServiceImpl<CustomerB2cInvoiceMapper, CustomerB2cInvoiceEntity> implements CustomerB2cInvoiceService {


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
        if (CollectionUtils.isNotEmpty(invoiceList)) {
            long count = invoiceList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
            if (count > 1) {
                throw new ServiceException(ApiError.ERROR_92007);
            }
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
        List<CustomerB2cInvoiceEntity> addList = BeanMapper.copyList(invoiceList, CustomerB2cInvoiceEntity.class);
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
        List<CustomerB2cInvoiceEntity> dbList = this.listBaseByMainId(mainId);
        List<InvoiceDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, InvoiceDTO.ViewDTO.class);
        return resultList;
    }

    /**
     * 批量修改发票信息
     *
     * @param mainId
     * @param invoiceList
     * @return void
     * @author yl
     * @date 2023-05-15 11:16
     */
    @Override
    public void updateBatchInvoice(String mainId, List<InvoiceDTO.ViewDTO> invoiceList) {

        List<CustomerB2cInvoiceEntity> saveOrUpdateList = new ArrayList<>(invoiceList.size());
        //这是修改的
        List<InvoiceDTO.ViewDTO> updateList = invoiceList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<InvoiceDTO.ViewDTO> addList = invoiceList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //这个是要修改的实体
        List<CustomerB2cInvoiceEntity> updateEntityList = BeanMapper.copyList(updateList, CustomerB2cInvoiceEntity.class);
        //这个是要添加的
        List<CustomerB2cInvoiceEntity> addEntityList = BeanMapper.copyList(addList, CustomerB2cInvoiceEntity.class);
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<CustomerB2cInvoiceEntity> dbList = this.listBaseByMainId(mainId);
        List<String> deleteIdList = getDeleteIds(updateList, dbList);
        //这是要删除的
        List<CustomerB2cInvoiceEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        saveOrUpdateList.forEach(s -> s.setMainId(mainId));

        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getHead())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个发票【%s】", ModuleTypeEnum.CUSTOMER_B2C.getCode(), removePairList, "编辑操作");
        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getHead())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个联系人【%s】", ModuleTypeEnum.CUSTOMER_B2C.getCode(), addPairList, "编辑操作");
        //修改的
        for (CustomerB2cInvoiceEntity update : updateEntityList) {
            String id = update.getId();
            CustomerB2cInvoiceEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.CUSTOMER_B2C.getCode(), mainId, "", "");
            }
        }
        if (CollectionUtils.isNotEmpty(saveOrUpdateList)) {
            this.saveOrUpdateBatch(saveOrUpdateList);
        }

    }

    /**
     * 获取删除字段的信息
     *
     * @param invoiceList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-15 11:04
     */
    private List<String> getDeleteIds(List<InvoiceDTO.ViewDTO> invoiceList, List<CustomerB2cInvoiceEntity> dbList) {
        List<String> ids = invoiceList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(InvoiceDTO.ViewDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(CustomerB2cInvoiceEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<CustomerB2cInvoiceEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(CustomerB2cInvoiceEntity::getMainId, mainId).list();
    }
}
