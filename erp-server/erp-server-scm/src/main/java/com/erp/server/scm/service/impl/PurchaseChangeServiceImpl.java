package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.PurchaseChangeDTO;
import com.erp.model.scm.entity.PurchaseChangeEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.PurchaseChangeMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchaseChangeDetailService;
import com.erp.server.scm.service.PurchaseChangeService;
import com.erp.server.scm.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 销售需求明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Slf4j
@Service
public class PurchaseChangeServiceImpl extends SuperServiceImpl<PurchaseChangeMapper, PurchaseChangeEntity> implements PurchaseChangeService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SupplierService supplierService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseChangeDetailService purchaseChangeDetailService;

    @Override
    public PagingVO<PurchaseChangeDTO.ListDTO> paging(PagingDTO<PurchaseChangeDTO.SearchParamDTO> dto) {
        return null;
    }

    @Override
    public Boolean add(PurchaseChangeDTO.AddDTO dto) {
        PurchaseChangeEntity entity = new PurchaseChangeEntity();
        BeanMapperUtils.copy(dto,entity);
        log.info("采购变更单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.POC, BusinessNoTypeEnum.CODE_POC.getCode()));
        entity.setCode(code);
        //处理数据id
        doOpHandleDataId(dto.getChangeUserId(),dto.getChangeDeptId(),dto.getPurchaseOrgId(),dto.getSupplierId(),entity);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个采购变更单【%s】",code), ModuleTypeEnum.PURCHASE_CHANGE.getCode(),entity.getId(),"新增操作");
            //新增明细
            purchaseChangeDetailService.add(dto.getDetails(),entity.getId());
        }


        return null;
    }

    @Override
    public Boolean update(PurchaseChangeDTO.UpdateDTO dto) {
        PurchaseChangeEntity entity = new PurchaseChangeEntity();
        BeanMapperUtils.copy(dto,entity);
        //处理数据id
        doOpHandleDataId(dto.getChangeUserId(),dto.getChangeDeptId(),dto.getPurchaseOrgId(),dto.getSupplierId(),entity);
        log.info("采购变更单修改，id=【{}】", dto.getId());




        //操作日志
        PurchaseChangeEntity old = this.getById(dto.getId());
        moduleOperateLogService.addModuleOperateLogByObj(old,entity,ModuleTypeEnum.PURCHASE_CHANGE.getCode(),entity.getId(),"","");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        //purchaseChangeDetailService.update(dto.getDetails(),entity.getId());
        return null;
    }

    @Override
    public PurchaseChangeDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean invalid(List<String> ids) {
        return null;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {

    }

    @Override
    public Boolean exportExcel(PurchaseChangeDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(PurchaseChangeDTO.AddDTO dto) {
        return null;
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId (String changeUserId, String changeDeptId, String purchaseOrgId,String supplierId, PurchaseChangeEntity entity) {
        //申请人
        if (StringUtils.isNotBlank(changeUserId)) {
            FindUserDTO purchaseUser = sysUserFeign.getUserByUserId(changeUserId);
            if (ObjectUtils.isEmpty(purchaseUser)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setChangeUserName(purchaseUser.getUserName());
        }
        //申请部门
        if (StringUtils.isNotBlank(changeDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(changeDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setChangeDeptName(depart.getName());
        }
        //采购组织
        if (StringUtils.isNotBlank(purchaseOrgId)) {
            List<BaseIdDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(purchaseOrgId));
            if (CollectionUtils.isEmpty(accountingCompanyList)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setPurchaseOrgName(accountingCompanyList.get(0).getName());
        }
        //供应商id
        if (StringUtils.isNotBlank(supplierId)) {
            SupplierEntity supplierEntity = supplierService.getById(supplierId);
            if (ObjectUtils.isEmpty(supplierEntity)) {
                throw new ServiceException(ApiError.ERROR_98031);
            }
            entity.setSupplierName(supplierEntity.getName());
        }

    }

}
