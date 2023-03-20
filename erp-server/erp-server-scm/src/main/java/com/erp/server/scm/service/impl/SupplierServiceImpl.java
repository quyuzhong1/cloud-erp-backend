package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.SupplierAccountDTO;
import com.erp.model.scm.dto.SupplierContactDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.SupplierMapper;
import com.erp.server.scm.service.SupplierAccountService;
import com.erp.server.scm.service.SupplierContactService;
import com.erp.server.scm.service.SupplierCredentialService;
import com.erp.server.scm.service.SupplierService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 供应商表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierServiceImpl extends SuperServiceImpl<SupplierMapper, SupplierEntity> implements SupplierService {


    @Resource
    private SupplierAccountService supplierAccountService;

    @Resource
    private SupplierContactService supplierContactService;

    @Resource
    private SupplierCredentialService supplierCredentialService;


    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 保存供应商信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-17 15:12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierEntity addSupplier(SupplierDTO.AddDTO dto) {

        checkName(null, dto.getName());

        //供应商联系信息
        List<SupplierContactDTO.AddDTO> contactList = dto.getContactList();
        //检查联系人默认是否多个
        supplierContactService.checkIsDefault(contactList);

        //供应商id
        String supplierId = IdWorker.getIdStr();
        SupplierEntity addEntity = new SupplierEntity();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(supplierId);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.GYS, BusinessNoTypeEnum.CODE_XQ.getCode()));
        addEntity.setCode(code);
        Boolean result = this.save(addEntity);
        //保存成功
        if (result) {

            //供应商账号信息
            List<SupplierAccountDTO.AddDTO> bankAccountList = dto.getBankAccountList();
            supplierAccountService.saveBatchBankAccount(supplierId, bankAccountList);
            supplierContactService.saveBatchContact(supplierId, contactList);
            //供应商资质信息
            List<SupplierCredentialDTO.AddDTO> credentialList = dto.getCredentialList();
            supplierCredentialService.saveBatchCredential(supplierId, credentialList);

            return addEntity;
        }

        return null;
    }

    /**
     * 保存并提交审核供应商
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 9:13
     */
    @Override
    public Boolean addAndSubmit(SupplierDTO.AddDTO dto) {
        SupplierEntity supplier = this.addSupplier(dto);
        if (supplier != null) {
            //这里还要启动流程
            return updateSubmitApproveStatus(supplier, ApproveStatusEnum.APPROVE_ING.getStatus());
        }
        return false;
    }


    /**
     * 供应商详情
     *
     * @param supplierId
     * @return com.erp.model.scm.dto.SupplierDTO.updateDTO
     * @author yl
     * @date 2023-03-20 10:00
     */
    @Override
    public SupplierDTO.UpdateDTO view(String supplierId) {
        SupplierDTO.UpdateDTO result = new SupplierDTO.UpdateDTO();
        SupplierEntity supplier = this.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        BeanMapper.copy(supplier, result);
        //根据供应商id 查询 联系人信息
        List<SupplierContactDTO.UpdateDTO> contactList = supplierContactService.getBySupplierId(supplierId);
        result.setContactList(contactList);

        //根据供应商id 查询账户信息
        List<SupplierAccountDTO.UpdateDTO> bankAccountList = supplierAccountService.getBySupplierId(supplierId);
        result.setBankAccountList(bankAccountList);
        //根据供应商id 获取资质信息
        List<SupplierCredentialDTO.UpdateDTO> credentialList = supplierCredentialService.getBySupplierId(supplierId);
        result.setCredentialList(credentialList);
        return result;
    }


    /**
     * 修改供应商信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 10:56
     */
    @Override
    public Boolean updateSupplier(SupplierDTO.UpdateDTO dto) {
        //供应商id
        String supplierId = dto.getId();
        SupplierEntity supplier = this.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        String code = supplier.getCode();
        //检查供应商名称
        checkName(supplierId, dto.getName());
        //联系人信息
        List<SupplierContactDTO.UpdateDTO> contactList = dto.getContactList();
        long count = contactList.stream().filter(c -> c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_98003);
        }
        BeanMapper.copy(supplier, dto);
        supplier.setCode(code);
        Boolean result = this.updateById(supplier);
        //修改成功
        if (result) {
            supplierContactService.updateSupplierContact(contactList, supplierId);
            //账户信息
            List<SupplierAccountDTO.UpdateDTO> bankAccountList = dto.getBankAccountList();
            supplierAccountService.updateAccount(bankAccountList, supplierId);
            //资质信息
            List<SupplierCredentialDTO.UpdateDTO> credentialList = dto.getCredentialList();
            supplierCredentialService.updateCredential(credentialList,supplierId);

        }

        return result;
    }


    /**
     * 启动流程
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-03-20 9:39
     */
    private void startProcess() {

    }

    /**
     * 更改 供应商审核状态
     *
     * @param supplier      供应商
     * @param approveStatus 状态
     */
    private Boolean updateSubmitApproveStatus(SupplierEntity supplier, String approveStatus) {
        if (supplier != null) {
            supplier.setApproveStatus(approveStatus);
            return this.updateById(supplier);
        }
        return true;
    }

    /**
     * 检查名称不能重复
     *
     * @param id
     * @param name
     * @return void
     * @author yl
     * @date 2023-03-17 16:48
     */
    private void checkName(String id, String name) {
        LambdaQueryWrapper<SupplierEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(SupplierEntity::getId, id);
        }
        queryWrapper.eq(SupplierEntity::getName, name);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_DUPLICATION_NAME);
        }
    }
}
