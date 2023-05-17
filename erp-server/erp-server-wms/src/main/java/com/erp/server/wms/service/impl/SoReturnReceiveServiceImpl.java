package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.oms.enums.SOReturnChangeListTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoReturnNoticeEntity;
import com.erp.model.wms.entity.SoReturnReceiveEntity;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.SoReturnReceiveMapper;
import com.erp.server.wms.service.SoReturnReceiveDetailService;
import com.erp.server.wms.service.SoReturnReceiveService;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 采购退货签收单
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnReceiveServiceImpl extends SuperServiceImpl<SoReturnReceiveMapper, SoReturnReceiveEntity> implements SoReturnReceiveService {
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoReturnReceiveDetailService soReturnReceiveDetailService;

    @Override
    public PagingVO<SoReturnReceiveDTO.PagingView> paging(PagingDTO<SoReturnReceiveDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoReturnReceiveDTO.PagingView> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        //明细数据
        List<SoReturnReceiveDTO.PagingView> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(SoReturnReceiveDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //详情表id集合
        List<String> detailIds = records.stream().map(SoReturnReceiveDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setSourceCode(null);
                    obj.setCustomerName(null);
                    obj.setInventoryOrgName(null);
                    obj.setApproveStatus(null);
                    obj.setApproveStatusName(null);
                    obj.setInvalidStatus(null);
                    obj.setInvalidStatusName(null);
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                obj.setReturnTypeDictName(ReturnTypeEnum.getName(obj.getReturnTypeDict()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95107);
                }
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                obj.setSalesQty(soDetailEntity.getQty());
                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                obj.setCustomerName(customerInfoEntity.getName());
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReturnReceiveDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        SOReturnChangeListTypeEnum[] values = SOReturnChangeListTypeEnum.values();
        List<SoReturnReceiveDTO.StatusCountDTO> list = new ArrayList<>();
        for (SOReturnChangeListTypeEnum item : values) {
            SoReturnReceiveDTO.PagingParam pagingParam = new SoReturnReceiveDTO.PagingParam();
            pagingParam.setParam(dto.getParam());
            SoReturnReceiveDTO.StatusCountDTO resultDTO = new SoReturnReceiveDTO.StatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (SOReturnChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (SOReturnChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (SOReturnChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public String add(SoReturnReceiveDTO.Add dto) {
        //获取退货单信息
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(dto.getSourceId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getInventoryOrgId());
        //获取用户信息
        FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getWarehouseKeeperId());
        SoReturnReceiveEntity entity = new SoReturnReceiveEntity();
        BeanMapperUtils.copy(soInfoEntity, entity);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        entity.setCustomerName(customerInfoEntity.getName());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.THTZ, BusinessNoTypeEnum.CODE_THTZ.getCode()));
        entity.setCode(code);
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(soReturnEntity.getCode());
        entity.setSourceType(soReturnEntity.getSourceType());
        entity.setInventoryOrgId(dto.getInventoryOrgId());
        entity.setInventoryOrgName(sysAccountingCompanyEntity.getCompanyName());
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
        }
        entity.setWarehouseKeeperName(userDTO.getUserName());
        this.save(entity);
        soReturnReceiveDetailService.add(dto, entity.getId());
        return soReturnEntity.getId();
    }

    @Override
    public Boolean update(SoReturnReceiveDTO.Update dto) {
        return null;
    }

    @Override
    public SoReturnReceiveDTO.View view(String id) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(SoReturnReceiveDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(SoReturnReceiveDTO.Update dto) {
        return null;
    }

    @Override
    public Boolean approve(BaseApproveParamDTO baseApproveParamDTO) {
        return null;
    }

    @Override
    public Boolean disApprove(List<String> ids) {
        return null;
    }

    @Override
    public Boolean cancelProcess(List<String> ids) {
        return null;
    }

    @Override
    public Boolean invalid(List<String> ids, String remark) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean exportExcel(SoReturnReceiveDTO.PagingParam dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean generateSoReturnReceiveSave(List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> list) {
        return null;
    }

    @Override
    public List<SoReturnReceiveDTO.GenerateSoReturnInstockView> generateSoReturnInstockView(List<String> ids) {
        return null;
    }

    @Override
    public List<SoReturnReceiveEntity> listBySourceIds(List<String> ids) {
        return lambdaQuery().in(SoReturnReceiveEntity::getSourceId, ids).list();
    }
}
