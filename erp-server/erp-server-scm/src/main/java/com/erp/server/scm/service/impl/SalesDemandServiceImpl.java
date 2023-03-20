package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.model.scm.dto.excel.SalesDemandExportExcelDTO;
import com.erp.model.scm.entity.SalesDemandDetailEntity;
import com.erp.model.scm.entity.SalesDemandEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
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
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    private CommonService commonService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;
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
    public String add(SalesDemandDTO.AddDTO dto) {
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
        return entity.getId();
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
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        //更新
        lambdaUpdate().in(SalesDemandEntity::getId,ids)
                .set(SalesDemandEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(SalesDemandEntity::getInvalidTime, LocalDateTime.now())
                .set(SalesDemandEntity::getRemark,reason)
                .update();
        return Boolean.TRUE;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String type = baseApproveParamDTO.getType();
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过 TODO

            //更新单据(后面有流程了可删)
            this.lambdaUpdate().in(SalesDemandEntity::getId,ids)
                    .set(SalesDemandEntity::getApproveUserId,userInfo.getUid())
                    .set(SalesDemandEntity::getApproveUserName,userInfo.getUserName())
                    .set(SalesDemandEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getStatus())
                    .set(SalesDemandEntity::getApproveTime,LocalDateTime.now())
                    .update();
        }

        //审核不通过
        if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程

            //更新单据
            this.lambdaUpdate().in(SalesDemandEntity::getId,ids)
                    .set(SalesDemandEntity::getApproveUserId,userInfo.getUid())
                    .set(SalesDemandEntity::getApproveUserName,userInfo.getUserName())
                    .set(SalesDemandEntity::getApproveStatus,ApproveStatusEnum.REJECT.getStatus())
                    .set(SalesDemandEntity::getApproveTime,LocalDateTime.now())
                    .update();
        }

    }

    @Override
    public Boolean cancelProcess(String id) {
        SalesDemandEntity salesDemandEntity = this.getById(id);
        if (ObjectUtils.isEmpty(salesDemandEntity)) {
            throw new ServiceException(ApiError.ERROR_98001);
        }
        if (ApproveStatusEnum.APPROVE_ING.getStatus().equals(salesDemandEntity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销现有流程
        workflowFeign.cancelProcess(id);

        //更新单据为待提交
        this.lambdaUpdate().eq(SalesDemandEntity::getId,id)
                .set(SalesDemandEntity::getApproveStatus,ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .update();

        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(SalesDemandDTO.SearchParamDTO dto, HttpServletResponse response) {
        List<SalesDemandExportExcelDTO> exportExcelList = baseMapper.listExportExcel(dto);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/salesDemandExport.xlsx";
        String name = "备货申请单";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(exportExcelList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //审核中和已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //取回流程 TODO

        //更新单据为待提交
        this.lambdaUpdate().in(SalesDemandEntity::getId,ids)
                .set(SalesDemandEntity::getApproveStatus,ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .update();
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //待审核允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //删除明细数据
        salesDemandDetailService.removeBySalesDemandIds(ids);
        //删除主表数据
        return  this.removeByIds(ids);
    }

    @Override
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //待提交允许提交
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        //启动流程 TODO

        //更新审核状态
        lambdaUpdate().in(SalesDemandEntity::getId,ids)
                .set(SalesDemandEntity::getApproveStatus,ApproveStatusEnum.APPROVE_ING)
                .update();
        return Boolean.TRUE;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(SalesDemandDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isNotBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        return this.submit(Arrays.asList(id));
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

    /**
     * 根据ids查询数据
     */
    private List<SalesDemandEntity>  getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<SalesDemandEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98001);
        }
        return list;
    }
}
