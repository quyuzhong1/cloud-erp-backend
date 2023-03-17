package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.model.scm.entity.SalesDemandDetailEntity;
import com.erp.model.scm.entity.SalesDemandEntity;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.SalesDemandMapper;
import com.erp.server.scm.service.CommonService;
import com.erp.server.scm.service.SalesDemandDetailService;
import com.erp.server.scm.service.SalesDemandService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 销售需求主表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Service
public class SalesDemandServiceImpl extends SuperServiceImpl<SalesDemandMapper, SalesDemandEntity> implements SalesDemandService {

    @Resource
    private SalesDemandDetailService salesDemandDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Override
    public PagingVO<SalesDemandDTO.ListDTO> paging(PagingDTO<SalesDemandDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<SalesDemandDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //清空明细数据
        List<SalesDemandDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setId(null);
                    obj.setCode(null);
                    obj.setShopName(null);
                    obj.setApproveStatusName(null);
                    obj.setInvalidStatusName(null);
                    obj.setIsFirstMassProduct(null);
                    obj.setStockReason(null);
                    obj.setApproveStatusName(null);
                    obj.setCreateUserName(null);
                    return;
                }
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SalesDemandDTO.AddDTO dto) {
        SalesDemandEntity entity = new SalesDemandEntity();
        BeanMapperUtils.copy(dto,entity);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XQ, BusinessNoTypeEnum.CODE_XQ.getCode()));
        entity.setCode(code);
        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(),dto.getApplyDeptId(),dto.getShopId(),entity);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志 TODO
            //新增明细
            salesDemandDetailService.add(dto.getDetails(),entity.getId());
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean update(SalesDemandDTO.UpdateDTO dto) {
        SalesDemandEntity entity = new SalesDemandEntity();
        BeanMapperUtils.copy(dto,entity);
        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(),dto.getApplyDeptId(),dto.getShopId(),entity);
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        salesDemandDetailService.update(dto.getDetails(),entity.getId());
        return Boolean.TRUE;
    }

    @Override
    public SalesDemandDTO.ViewDTO view(String id) {
        SalesDemandDTO.ViewDTO dto = new SalesDemandDTO.ViewDTO();

        //主表信息
        SalesDemandEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98001);
        }
        BeanMapperUtils.copy(entity,dto);

        //明细信息
        List<SalesDemandDetailEntity> entityDetails = salesDemandDetailService.listBySalesDemandId(id);
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98002);
        }
        List<SalesDemandDetailDTO.UpdateDTO> details = BeanMapperUtils.copyList(SalesDemandDetailDTO.UpdateDTO.class, entityDetails);
        dto.setDetails(details);
        return dto;
    }

    @Override
    public Boolean invalid(List<String> ids,String reason) {



        return null;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {

    }

    @Override
    public Boolean cancelProcess(String id) {
        return null;
    }

    @Override
    public Boolean exportExcel(SalesDemandDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean disApprove(List<String> ids) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(SalesDemandDTO.AddDTO dto) {
        return null;
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId (String applyUserId,String applyDeptId,String shopId,SalesDemandEntity entity) {
        //申请人
        if (StringUtils.isNotBlank(applyUserId)) {
            FindUserDTO applyUser = sysUserFeign.getUserByUserId(applyUserId);
            if (ObjectUtils.isEmpty(applyUser)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setApplyUserName(applyUser.getUserName());
        }
        //申请部门
        if (StringUtils.isNotBlank(applyDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(applyDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setApplyDeptName(depart.getName());
        }
        //店铺
        if (StringUtils.isNotBlank(shopId)) {
            DmpShopInfoDTO dmpShopInfoDTO = dmpTaskFeign.getShopById(shopId);
            if (ObjectUtils.isEmpty(dmpShopInfoDTO)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setShopName(dmpShopInfoDTO.getName());
        }
    }

}
