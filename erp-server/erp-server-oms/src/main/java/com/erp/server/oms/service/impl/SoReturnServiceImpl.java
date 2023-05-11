package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.SoReturnMapper;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import com.erp.server.oms.service.SoReturnService;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnServiceImpl extends SuperServiceImpl<SoReturnMapper, SoReturnEntity> implements SoReturnService {
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public PagingVO<SoReturnDTO.PagingView> paging(PagingDTO<SoReturnDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoReturnDTO.PagingView> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        //明细数据
        List<SoReturnDTO.PagingView> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(SoReturnDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = records.stream().map(SoReturnDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(orderDetailIds);
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setSourceCode(null);
                    obj.setType(null);
                    obj.setApproveStatusName(null);
                    obj.setApproveStatus(null);
                    obj.setInvalidStatusName(null);
                    obj.setInvalidStatus(null);
                    obj.setCustomerName(null);
                    obj.setSalesOrgName(null);
                    obj.setSellerName(null);
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                obj.setTypeName(BillTypeEnum.getName(obj.getType()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95107);
                }
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(soDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_99006);
                }
                obj.setSalesQty(soDetailEntity.getQty());
                obj.setProductName(productDetailEntity.getName());
                obj.setUnit(productDetailEntity.getUnitName());
                obj.setSalesAmount(soDetailEntity.getAmount());
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    /**
     *
     * @Author Luo_WG
     * @Date 2023/5/11 18:28
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SoReturnDTO.StatusCountDTO>
     **/
    @Override
    public List<SoReturnDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<SoReturnDTO.StatusCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item : values) {
            SoReturnDTO.PagingParam pagingParam = new SoReturnDTO.PagingParam();
            pagingParam.setParam(dto.getParam());
            SoReturnDTO.StatusCountDTO resultDTO = new SoReturnDTO.StatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (PurchaseChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (PurchaseChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (PurchaseChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
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
    public String add(SoReturnDTO.Add dto) {
        SoInfoEntity soInfoEntity = soInfoService.getById(dto.getSourceId());
        SoReturnEntity soReturnEntity = new SoReturnEntity();
        BeanMapperUtils.copy(soInfoEntity, soReturnEntity);
        
        return null;
    }

    @Override
    public Boolean update(SoReturnDTO.Update dto) {
        return null;
    }

    @Override
    public SoReturnDTO.View view(String id) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(SoReturnDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(SoReturnDTO.Update dto) {
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
    public Boolean exportExcel(SoReturnDTO.PagingParam dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<SoReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeView(List<String> ids) {
        return null;
    }
}
