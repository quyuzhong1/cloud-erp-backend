package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.dto.ProductChangeDetailDTO;
import com.erp.model.plm.entity.MoldRefSkuEntity;
import com.erp.model.plm.entity.ProductChangeDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.ProductChangeDetailService;
import com.erp.server.plm.service.ProductDetailService;
import io.seata.common.util.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.server.plm.mapper.ProductChangeMapper;
import com.erp.server.plm.service.ProductChangeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.ProductChangeDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.model.scm.enums.InvalidStatusEnum;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 产品变更信息表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
 */
@Slf4j
@Service
public class ProductChangeServiceImpl extends SuperServiceImpl<ProductChangeMapper, ProductChangeEntity> implements ProductChangeService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private ProductChangeDetailService productChangeDetailService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ProductChangeDTO.AddDTO addDTO) {
        ProductChangeEntity productChangeEntity = new ProductChangeEntity();
        BeanMapperUtils.copy(addDTO, productChangeEntity);

        // 数据处理
        handleData(productChangeEntity);

        log.info("开始新增产品变更信息单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_BG);
        productChangeEntity.setCode(code);
        boolean save = super.save(productChangeEntity);
        if(!save) {
            throw new ServiceException("产品变更信息单保存失败");
        }
        productChangeDetailService.add(productChangeEntity,addDTO.getDetailDTOList());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "产品变更信息单" , productChangeEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), productChangeEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(productChangeEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProductChangeDTO.UpdateDTO addOrUpdateDTO) {
        ProductChangeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "产品变更信息单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_UPDATE_STATUS_NOT_ALLOWED);
        }
        ProductChangeEntity productChangeEntity =  BeanMapperUtils.map(ProductChangeEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(productChangeEntity);
        boolean save = super.updateById(productChangeEntity);
        if(!save) {
            throw new ServiceException("产品变更信息单保存失败");
        }
        productChangeDetailService.update(productChangeEntity,addOrUpdateDTO.getDetailDTOList());

        // 记录主单操作日志
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), productChangeEntity.getCode(), "产品变更信息单");
        operateLogService.addSysLogByUpdate(old, productChangeEntity, String.valueOf(ProductChangeEntity.class), productChangeEntity.getId(),"", msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<ProductChangeDTO.ListDTO> paging(PagingDTO<ProductChangeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ProductChangeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<ProductChangeDTO.TabListDTO> tabList(PermissionsDTO param) {
        ProductChangeDTO.PagingParamDTO searchParam = new ProductChangeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ProductChangeDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(ProductChangeDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new ProductChangeDTO.TabListDTO(status,ApproveStatusEnum.getName(status), 0));
        }
        });
        // 计算合计数量
        list.add(new ProductChangeDTO.TabListDTO("all","全部", list.stream().mapToInt(ProductChangeDTO.TabListDTO::getCount).sum()));
        for (ProductChangeDTO.TabListDTO tabListDTO : list) {
            if(StringUtils.isBlank(tabListDTO.getTabFlagName())) {
                tabListDTO.setTabFlagName(ApproveStatusEnum.getName(tabListDTO.getTabFlag()));
            }
        }
        return list;
    }

    @Override
    public void exportList(ProductChangeDTO.PagingParamDTO param) {
        downloadTaskFeign.saveDownloadTask("产品信息变更单", FileTaskEventEnum.EXPORT_PLM_PRODUCT_CHANGE.getCode(), param);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "excel/productChangeTemplate.xlsx";
        String excelName = "产品信息变更导入模板.xlsx";
        com.common.core.utils.ExcelUtil.downloadTemplate(path, excelName, response);
    }

    @Override
    public void importExcel(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("产品信息变更导入", FileTaskEventEnum.IMPORT_FMS_ASSET_ACCEPT.getCode(), dto);
    }

    @Override
    public void importProductChange(BaseDTO.ImportDTO dto) {

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        ProductChangeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到产品变更信息单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());
        startProcess(entity);
        // 记录操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "产品变更信息单");

        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(ProductChangeDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(ProductChangeDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.WF_REJECT_COMMENT_REQUIRED);
        }
        ProductChangeEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.WF_APPROVE_ALLOWED_STATUS_ONLY);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "产品变更信息单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(ProductChangeEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PRODUCT_CHANGE.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.WF_APPROVE_FAILED);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        ProductChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到产品变更信息单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "产品变更信息单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(ProductChangeEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.BILL_REVERSE_APPROVAL_ALLOWED_APPROVED_ONLY);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        ProductChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到产品变更信息单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }

        // 删除主单数据
        super.removeById(id);
        productChangeDetailService.deleteByMainId(id);
        // 删除日志数据
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "产品变更信息单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), entity.getCode(), "删除产品变更信息单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        ProductChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到产品变更信息单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY);
        }
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "产品变更信息单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.PRODUCT_CHANGE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, ProductChangeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(ProductChangeEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.PRODUCT_CHANGE.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }


    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(ProductChangeEntity::getId, id)
            .set(ProductChangeEntity::getApproveUserId, userInfo.getUid())
            .set(ProductChangeEntity::getApproveUserName, userInfo.getUserName())
            .set(ProductChangeEntity::getApproveStatus, approveStatus)
            .set(ProductChangeEntity::getApproveTime, LocalDateTime.now())
            .update(new ProductChangeEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(ProductChangeEntity::getId, id)
            .set(ProductChangeEntity::getApproveUserId, "")
            .set(ProductChangeEntity::getApproveUserName, "")
            .set(ProductChangeEntity::getApproveStatus, approveStatus)
            .set(ProductChangeEntity::getApproveTime, null)
            .update(new ProductChangeEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(ProductChangeEntity::getId, id)
        .set(ProductChangeEntity::getApproveStatus, approveStatus)
        .update(new ProductChangeEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(ProductChangeEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(ProductChangeEntity productChangeEntity) {
        List<SkuVO> skuVOList = productDetailService.getSkuBaseByIds(Arrays.asList(productChangeEntity.getSkuId()));
        if(CollectionUtil.isEmpty(skuVOList)){
            throw new ServiceException("未找到对应的产品信息数据");
        }
        SkuVO skuVO = skuVOList.get(0);
        productChangeEntity.setSkuNo(skuVO.getSkuNo());
        productChangeEntity.setProductName(skuVO.getSkuName());
    }

    @Override
    public ProductChangeDTO.ViewDTO view(String id) {
        ProductChangeEntity productChangeEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到产品变更信息单数据"));
        ProductChangeDTO.ViewDTO data = BeanMapperUtils.map(ProductChangeDTO.ViewDTO.class, productChangeEntity);
        List<ProductChangeDetailEntity> detailList = productChangeDetailService.listByMains(Arrays.asList(id));
        List<ProductChangeDetailDTO.ViewDTO> detailDTOList = BeanMapperUtils.copyList(ProductChangeDetailDTO.ViewDTO.class, detailList);
        data.setDetailDTOList(detailDTOList);
        // 数据填充处理
        fillOne(data);
        return data;
    }

    private void fillOne(ProductChangeDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<ProductChangeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(ProductChangeDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        }
   }
}
