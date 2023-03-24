package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.PurchaseOrderMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import com.erp.server.scm.service.PurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购订单表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Slf4j
@Service
public class PurchaseOrderServiceImpl extends SuperServiceImpl<PurchaseOrderMapper, PurchaseOrderEntity> implements PurchaseOrderService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Override
    public PagingVO<PurchaseOrderDTO.ListDTO> paging(PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto) {
        return null;
    }

    @Override
    public String add(PurchaseOrderDTO.AddDTO dto) {
        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        BeanMapperUtils.copy(dto,entity);
        //校验明细是否有重复sku
        checkAddDetailsRepeatSku(dto.getDetails());
        //处理数据id
        doOpHandleDataId(dto.getPurchaseUserId(),dto.getPurchaseDeptId(),dto.getPurchaseOrgId(),entity);
        log.info("采购订单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.PL, BusinessNoTypeEnum.CODE_PL.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个采购单【%s】",code), ModuleTypeEnum.PURCHASE_ORDER.getCode(),entity.getId(),"新增操作");
            //新增明细
            //purchaseOrderDetailService.add(dto.getDetails(),entity.getId());
        }
        return entity.getId();
    }

    @Override
    public Boolean update(PurchaseOrderDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public PurchaseOrderDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {

    }

    @Override
    public Boolean disApprove(List<String> ids) {
        return null;
    }

    @Override
    public Boolean cancelProcess(String id) {
        return null;
    }

    @Override
    public Boolean finishDelivery(String id) {
        return null;
    }
    @Override
    public Boolean purchaseChange(String id) {
        return null;
    }

    @Override
    public Boolean exportPurchaseContractPdf(String id) {
        return null;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean exportExcel(PurchaseOrderDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(PurchaseOrderDTO.AddDTO dto) {
        return null;
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId (String purchaseUserId,String purchaseDeptId,String purchaseOrgId,PurchaseOrderEntity entity) {
        //申请人
        if (StringUtils.isNotBlank(purchaseUserId)) {
            FindUserDTO purchaseUser = sysUserFeign.getUserByUserId(purchaseUserId);
            if (ObjectUtils.isEmpty(purchaseUser)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setPurchaseUserName(purchaseUser.getUserName());
        }
        //申请部门
        if (StringUtils.isNotBlank(purchaseDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(purchaseDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setPurchaseDeptName(depart.getName());
        }
        //采购组织
        if (StringUtils.isNotBlank(purchaseOrgId)) {
            List<BaseIdDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(purchaseOrgId));
            if (CollectionUtils.isEmpty(accountingCompanyList)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setPurchaseOrgName(accountingCompanyList.get(0).getName());
        }
    }

    /**
     * 根据ids查询数据
     */
    private List<PurchaseOrderEntity>  getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<PurchaseOrderEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        return list;
    }

    /**
     * 新增验证sku是否重复
     */
    private void checkAddDetailsRepeatSku(List<PurchaseOrderDetailDTO.AddDTO> list) {
        Map<String, List<PurchaseOrderDetailDTO.AddDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseOrderDetailDTO.AddDTO::getSkuId));
        for (Map.Entry<String, List<PurchaseOrderDetailDTO.AddDTO>> entry: map.entrySet()) {
            List<PurchaseOrderDetailDTO.AddDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }
    }
}
