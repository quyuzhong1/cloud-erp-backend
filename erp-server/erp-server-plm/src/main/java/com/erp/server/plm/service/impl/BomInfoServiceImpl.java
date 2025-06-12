package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.*;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.BomInfoExcelDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.BomSkuEntity;
import com.erp.model.plm.enums.BomOperationTypeEnum;
import com.erp.model.plm.enums.BomStateEnum;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.model.plm.vo.BomPagingVO;
import com.erp.model.plm.vo.BomVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.SubcontractOrderDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.constant.BomOperateContent;
import com.erp.server.plm.listener.BomInfoExcelListener;
import com.erp.server.plm.mapper.BomInfoMapper;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeBomInfoService;
import com.erp.server.plm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alibaba.excel.EasyExcelFactory.read;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_BOM;

/**
 * bom 信息表(BomInfo)表服务实现类
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@Slf4j
@Service
public class BomInfoServiceImpl extends ServiceImpl<BomInfoMapper, BomInfoEntity> implements BomInfoService {


    @Resource
    private BomSkuService bomSkuService;

    @Resource
    private BomOperateLogService bomOperateLogService;

    @Resource
    private WorkflowFeign workflowFeign;


    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private CommonService commonService;

    @Resource
    private ProductBomHistoryService productBomHistoryService;



    @Resource
    private ProductChangeService productChangeService;

    @Resource
    private SysCodeService sysCodeService;

    @Resource
    private SyncKingdeeBomInfoService syncKingdeeBomInfoService;

    @Resource
    private ScmTaskFeign scmTaskFeign;


    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private ProductLogisticsService productLogisticsService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Autowired
    private SysUserFeign sysUserFeign;

    /**
     * 添加bom
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-09 12:18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String insert(AddBomDTO dto) {
        //sku信息
        List<BomSkuDTO> bomSkuList = dto.getSkuList();
        if (CollectionUtils.isEmpty(bomSkuList)) {
            throw new ServiceException(ApiError.ERROR_95094);
        }
        //数据验证
        checkRepeatBomSku(BeanMapperUtils.map(UpdateBomDTO.class,dto));
        //获取到 编号
        String serialNumber = sysCodeService.getBusinessNo(BusinessNoConstant.BOM, BusinessNoTypeEnum.Bom_NO);
        BomInfoEntity bom = new BomInfoEntity();
        String bomId = IdWorker.getIdStr();
        bom.setType(dto.getType());
        bom.setBomVersion(dto.getVersion());
        bom.setId(bomId);
        bom.setSerialNumber(serialNumber);
        bom.setSourceType(dto.getSourceType());
        Boolean saveResult = this.save(bom);
        if (!saveResult) {
            throw new ServiceException(ApiError.ERROR_1002);
        }
        //添加 bom 与sku 关系
        bomSkuService.saveBomSku(bomId, bomSkuList);
        //添加 bom的操作日志
        String operateContent = String.format(BomOperateContent.ADD, serialNumber);
        bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.ADD.getType(), operateContent);
        //更新父sku物流属性
        List<String> parentSkuIds = bomSkuList.stream().map(BomSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        productLogisticsService.saveOrUpdateParentPropertyId(parentSkuIds);
        //提交
        String submitAudit = BomConstant.SUBMIT_AUDIT;
        boolean isSubmitAudit = submitAudit.equals(dto.getSubmitType());
        if (isSubmitAudit) {
            this.submitAudit(bom.getId(),Boolean.TRUE);
        }
        return bomId;
    }


    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        BomInfoExcelListener excelListenerUtil = new BomInfoExcelListener();
        try {
            read(excelFile.getInputStream(), BomInfoExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！",e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！",e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<BomInfoExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<BomInfoExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<BomInfoExcelDTO> successList = excelListenerUtil.getSuccessList();

        //处理验证成功数据
        handleImportSuccessList(successList,errorList);

        if (errorList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            String excelPath = "excel/bomInfoError.xlsx";
            String name = "bomInfo";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(BomInfoEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), BomInfoEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    @Override
    public PagingVO<BomSkuPageDTO.ListDTO> skuPaging(PagingDTO<BomSkuPageDTO.PagingParamDTO> dto) {
        BomSkuPageDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<BomSkuPageDTO.PagingParamDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<BomSkuPageDTO.ListDTO> pageData = baseMapper.skuPaging(query, params);
        List<BomSkuPageDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO<>();
        }
        List<String> bomIds = records.stream().map(BomSkuPageDTO.ListDTO::getBomId).collect(Collectors.toList());
        List<BomSkuPageDTO.ChildDTO> childList = baseMapper.listBomSkuByBomIds(bomIds);
        if (CollectionUtils.isEmpty(childList)) {
            throw new ServiceException(ApiError.ERROR_95166);
        }

        List<String> supplierIds = records.stream().filter(obj -> StringUtils.isNotBlank(obj.getSupplierId())).map(BomSkuPageDTO.ListDTO::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(supplierIds)) {
            supplierList = scmTaskFeign.getSupplierByIdList(supplierIds);
        }
        //分页数据处理
        handleBomPaging(records,supplierList,childList);

        return new PagingVO<>(pageData);
    }

    /**
     * 分页数据处理
     * @author will
     * @date 2024/11/15 15:25
     * @param records
     * @param supplierList
     * @param childList
     */
    private void handleBomPaging (List<BomSkuPageDTO.ListDTO> records,List<SupplierEntity> supplierList,List<BomSkuPageDTO.ChildDTO> childList) {

        Integer index = MathUtil.ONE;
        for (BomSkuPageDTO.ListDTO listDTO : records) {
            //付款条件
            String paymentCondition = supplierList.stream().filter(obj -> obj.getId().equals(listDTO.getSupplierId()) && StringUtils.isNotBlank(obj.getPaymentCondition())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getPaymentCondition())).orElse("");
            listDTO.setPaymentCondition(paymentCondition);
            //状态名称
            listDTO.setStatusName(ProductDetailStatusEnum.getName(listDTO.getStatus()));
            listDTO.setIndex(index);
            index++;
            //子件
            List<BomSkuPageDTO.ChildDTO> childDTOList = childList.stream().filter(obj -> obj.getBomId().equals(listDTO.getBomId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(childDTOList)) {
                for (BomSkuPageDTO.ChildDTO childDTO: childDTOList) {
                    //付款条件
                    String childPaymentCondition = supplierList.stream().filter(obj -> obj.getId().equals(childDTO.getSupplierId()) && StringUtils.isNotBlank(obj.getPaymentCondition())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getPaymentCondition())).orElse("");
                    childDTO.setPaymentCondition(childPaymentCondition);
                    if (ObjectUtils.isNotEmpty(childDTO.getStatus())) {
                        //状态名称
                        childDTO.setStatusName(ProductDetailStatusEnum.getName(childDTO.getStatus()));
                    }
                    childDTO.setIndex(index);
                    index++;
                }
            }
            listDTO.setChildList(childDTOList);
        }
    }

    @Override
    public PagingVO<BomExportExcelVO> exportBom(PagingDTO<SearchPagingDTO> dto) {
        String searchKeyword = dto.getParams().getSearchKeyword();
        //当这个不为空的时候 表示可能要搜索 sku 或者 sku名称 或者bom 编号
        List<String> skuIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(searchKeyword)) {
            skuIdList = productChangeService.getChangeSearchCondition(searchKeyword);
        }
        //待审核
        List<String> bomIdList = new ArrayList<>();

        List<FindUserDTO> userList = commonService.getAllUser();

        Page<BomPagingVO> page = baseMapper.getAllBom(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams(), bomIdList, skuIdList);
        List<BomPagingVO> list = page.getRecords();
        //对应sku集合
        List<String> skuNoList = list.stream().map(BomPagingVO::getSkuNo).collect(Collectors.toList());
        List<String> parentSkuNoList = list.stream().map(BomPagingVO::getParentSkuNo).collect(Collectors.toList());
        skuNoList.addAll(parentSkuNoList);
        List<SkuVO> skuList = productDetailService.getSkuBySkuNos(skuNoList);
        for (BomPagingVO item : list) {
            String skuNo = item.getSkuNo();
            String parentSkuNo = item.getParentSkuNo();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);

            SkuVO parentSkuVO = skuList.stream().filter(s -> s.getSkuNo().equals(parentSkuNo)).findFirst().orElse(null);

            FindUserDTO createUser = userList.stream().filter(u -> u.getUserId().equals(item.getCreateUserId())).findFirst().orElse(null);
            if (createUser != null) {
                item.setCreateUserName(createUser.getUserName());
            }
            Integer state = item.getState();
            item.setStateName(BomStateEnum.getName(state));
            String type = item.getType();
            item.setTypeName(BomTypeEnum.getName(type));
            if (skuVO != null) {
                item.setSkuName(skuVO.getSkuName());
                item.setSpuNo(skuVO.getSpuNo());
                item.setSpuName(skuVO.getSpuName());
            }
            if (parentSkuVO != null) {
                item.setParentSkuName(parentSkuVO.getSkuName());
            }
        }
        List<BomExportExcelVO> excelList = BeanMapperUtils.copyList(BomExportExcelVO.class, list);
        return new PagingVO<>(excelList,(int) page.getTotal(),dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        BomInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getState(), BomStateEnum.AUDIT_ING.getState())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        //操作记录
        String operateContent = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getSerialNumber(), "BOM信息", approveType.getName(), dto.getComment());
        bomOperateLogService.saveOperate(entity.getId(), BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getSerialNumber(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     * @param entity
     * @param dto
     */
    private void approveProcess(BomInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PRODUCT_BOM_INFO.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    /**
     * variablesMap值赋值
     * @author will
     * @date 2025/5/21 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(BomInfoEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getCreateUserId());
        if (ObjectUtil.isNotEmpty(findUserDTO)) {
            //总计数量
            variablesMap.put("createDeptId", findUserDTO.getDepartmentId());
        }
        List<BomSkuEntity> bomList = bomSkuService.listBomSkuByBomId(entity.getId());
        if (CollUtil.isEmpty(bomList)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        String parentSkuNo = bomList.stream().map(BomSkuEntity::getParentSkuNo).collect(Collectors.joining(","));
        variablesMap.put("parentSkuNo", parentSkuNo);
        String skuNo = bomList.stream().map(BomSkuEntity::getSkuNo).collect(Collectors.joining(","));
        variablesMap.put("skuNo", skuNo);
        return variablesMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, BomInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        Integer approveStatus;
        if (dto.getType().equals(ApproveTypeEnum.PASS.getStatus())) {
            //审核通过
            approveStatus = BomStateEnum.AUDIT_PASS.getState();
        } else if (dto.getType().equals(ApproveTypeEnum.CANCEL.getStatus())) {
            //待提交
            approveStatus = BomStateEnum.WAIT_SUBMIT_AUDIT.getState();
        } else {
            //审核不通过
            approveStatus = BomStateEnum.AUDIT_NO_PASS.getState();
        }
        //更新审核状态
        updateForApprove(entity.getId(), approveStatus,dto.getComment());

        if (dto.getType().equals(ApproveType.PASS)) {
            //保存bom 审核通过的的历史数据
            productBomHistoryService.saveBomApprovalHistory(entity);
            //发送金蝶
            sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
        }
        return Boolean.TRUE;
    }

    @Override
    public BatchResultDTO cancelProcess(String id) {
        BomInfoEntity entity = this.getById(id);
        if (ObjectUtil.isNotEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getState(), BomStateEnum.AUDIT_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        updateForApprove(id, BomStateEnum.AUDIT_ING.getState(),"");
        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getSerialNumber(), "BOM信息");
        bomOperateLogService.saveOperate(entity.getId(), BomOperationTypeEnum.STATE_CHANGE.getType(),msg );
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.PRODUCT_BOM_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getSerialNumber(), OperationTypeEnum.CANCEL_PROCESS);
    }

    /**
     * 审核更新审核信息
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, Integer approveStatus,String comment) {
        //当前登录人
        this.lambdaUpdate().eq(BomInfoEntity::getId, id)
                .set(BomInfoEntity::getState, approveStatus)
                .set(ObjectUtil.isNotEmpty(comment),BomInfoEntity::getRemark, comment)
                .update();
    }


    /**
     * 分页获取bom 列表
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO<java.util.List < com.erp.model.plm.vo.BomPagingVO>>
     * @author yl
     * @date 2023-01-10 17:30
     */
    @Override
    public PagingVO<BomPagingVO> paging(PagingDTO<SearchPagingDTO> dto) {
        SearchPagingDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        String searchKeyword = params.getSearchKeyword();
        Page<SearchPagingDTO> query = new Page<SearchPagingDTO>(dto.getCurrPage(), dto.getPageSize());
        List<String> bomIdList = new ArrayList<>();
        //当这个不为空的时候 表示可能要搜索 sku 或者 sku名称 或者bom 编号
        List<String> skuIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(searchKeyword)) {
            skuIdList = productChangeService.getChangeSearchCondition(searchKeyword);
            if (CollectionUtils.isEmpty(skuIdList)) {
                IPage<BomPagingVO> pageData = new Page<BomPagingVO>();
                return new PagingVO<>(pageData);
            }
        }
        IPage<BomPagingVO> pageData = baseMapper.paging(query, params, bomIdList, skuIdList);
        List<BomPagingVO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> bomIds = list.stream().map(BomPagingVO::getId).collect(Collectors.toList());

        List<String> changeIngSourceIds = productChangeService.getBySourceId(bomIds);
        List<FindUserDTO> userList = commonService.getAllUser();
        //对应sku集合
        List<String> skuNoList = list.stream().map(BomPagingVO::getSkuNo).collect(Collectors.toList());
        List<String> parentSkuList = list.stream().map(BomPagingVO::getParentSkuNo).collect(Collectors.toList());
        skuNoList.addAll(parentSkuList);
        List<SkuVO> skuList = productDetailService.getSkuBySkuNos(skuNoList);
        for (BomPagingVO item : list) {
            String skuNo = item.getSkuNo();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);
            SkuVO parentSkuVO = skuList.stream().filter(s -> s.getSkuNo().equals(item.getParentSkuNo())).findFirst().orElse(null);

            FindUserDTO createUser = userList.stream().filter(u -> u.getUserId().equals(item.getCreateUserId())).findFirst().orElse(null);
            if (createUser != null) {
                item.setCreateUserName(createUser.getUserName());
            }
            Boolean isChangeIng = changeIngSourceIds.contains(item.getId());
            item.setIsChangeIng(isChangeIng);
            Integer state = item.getState();
            item.setStateName(BomStateEnum.getName(state));
            String type = item.getType();
            item.setTypeName(BomTypeEnum.getName(type));
            if (skuVO != null) {
                item.setSkuName(skuVO.getSkuName());
                item.setSpuNo(skuVO.getSpuNo());
                item.setSpuName(skuVO.getSpuName());
            }
            if (parentSkuVO != null) {
                item.setParentSkuName(parentSkuVO.getSkuName());
            }
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public BomDTO getBomDetails(String id) {
        BomDTO result = new BomDTO();
        BomInfoEntity bom = this.getById(id);
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        BeanMapper.copy(bom, result);
        result.setVersion(bom.getBomVersion());
        String createUserName = commonService.getNameById(result.getCreateUserId());
        result.setCreateUserName(createUserName);
        List<BomSkuDTO> skuList = bomSkuService.getByBomId(id);
        result.setSkuList(skuList);
        return result;
    }


    /**
     * 编辑Bom
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-11 18:17
     */
    @Override
    public Boolean edit(UpdateBomDTO dto) {
        String id = dto.getId();
        BomInfoEntity bom = this.getById(id);
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        //数据验证
        checkRepeatBomSku(dto);
        checkBomCanUpdate(bom.getState(), BomConstant.EDIT);
        List<BomSkuDTO> oldBomList = bomSkuService.getByBomId(id);
        Boolean result = this.updateById(bom);
        List<BomSkuDTO> bomSkuList = dto.getSkuList();
        if (result) {
            //添加 bom 与sku 关系
            bomSkuService.updateBomSku(id, bomSkuList);
            String operateContent = getUpdateContent(oldBomList, bomSkuList);
            if (StringUtils.isNotBlank(operateContent)) {
                bomOperateLogService.saveOperate(id, BomOperationTypeEnum.UPDATE.getType(), operateContent);
            }
            //更新父sku物流属性
            List<String> parentSkuIds = bomSkuList.stream().map(BomSkuDTO::getSkuId).distinct().collect(Collectors.toList());
            productLogisticsService.saveOrUpdateParentPropertyId(parentSkuIds);
        }
        return result;
    }

    /**
     * 获取 修改的信息
     *
     * @param oldBomList
     * @param newBomList
     * @return java.lang.String
     * @author yl
     * @date 2023-02-07 17:07
     */
    @Override
    public String getUpdateContent(List<BomSkuDTO> oldBomList, List<BomSkuDTO> newBomList) {
        List<String> contentList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(oldBomList) && CollectionUtils.isNotEmpty(newBomList)) {
            BomSkuDTO oldParent = oldBomList.get(0);
            BomSkuDTO newParent = newBomList.get(0);
            if (!oldParent.getSkuNo().equals(newParent.getSkuNo())) {
                String parentContent = "父物料" + oldParent.getSkuNo() + " 变更到" + newParent.getSkuNo();
                contentList.add(parentContent);
            }
            if (!oldParent.getChildren().equals(newParent.getChildren())) {
                getChildrenUpdateContent(oldParent.getChildren(), newParent.getChildren(), contentList);
            }
        }

        return String.join(";", contentList);
    }

    /**
     * 获取到子集
     *
     * @param
     * @param contentList
     * @return void
     * @author yl
     * @date 2023-02-07 17:11
     */
    private void getChildrenUpdateContent(List<BomChildrenSkuDTO> OldChildrenList, List<BomChildrenSkuDTO> newChildrenList, List<String> contentList) {
        int oldSize = OldChildrenList.size();
        int newSize = newChildrenList.size();
        for (int i = 0; i < newSize; i++) {
            BomChildrenSkuDTO newBom = newChildrenList.get(i);
            BomChildrenSkuDTO oldBom = OldChildrenList.stream().filter(o -> o.getSkuId().equals(newBom.getSkuId())).
                    findFirst().orElse(null);
            if (oldBom != null) {
                if (!oldBom.getSkuNo().equals(newBom.getSkuNo())) {
                    String childrenContent = "子物料" + oldBom.getSkuNo() +
                            "变更到" + newBom.getSkuNo() + " 用量为" + newBom.getQuantity();
                    contentList.add(childrenContent);
                }
                if (oldBom.getSkuNo().equals(newBom.getSkuNo())
                        && (!oldBom.getQuantity().
                        equals(newBom.getQuantity()))) {
                    String childrenQuantityContent = "子物料" + oldBom.getSkuNo() + "用量" + oldBom.getQuantity() + "变更到" + newBom.getQuantity();
                    contentList.add(childrenQuantityContent);
                }
            }
            if (oldSize <= i) {
                String addContent = "子物料添加" + newBom.getSkuNo() + ", 子物料添加用量" + newBom.getQuantity();
                contentList.add(addContent);
            }
        }
        //删除
        if (oldSize >= newSize) {
            List<String> skuNoList = newChildrenList.stream().map(BomChildrenSkuDTO::getSkuNo).collect(Collectors.toList());
            List<String> dbSkuNoList = OldChildrenList.stream().map(BomChildrenSkuDTO::getSkuNo).collect(Collectors.toList());
            String removeSkuNo = dbSkuNoList.stream().filter(d -> !skuNoList.contains(d)).collect(Collectors.joining(","));
            if (StringUtils.isNotBlank(removeSkuNo)) {
                String removeContent = "删除了" + removeSkuNo + " 子物料";
                contentList.add(removeContent);
            }
        }


    }


    /**
     * 删除bom
     *
     * @param bomId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-13 8:56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean deleteById(String bomId) {
        BomInfoEntity bomInfoEntity = this.getById(bomId);
        if (ObjectUtils.isEmpty(bomInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        //更新金蝶
        List<DmpPushTaskEntity> pushTaskList = syncKingdeeBomInfoService.syncDataToKingdee(bomInfoEntity, SyncOperateEnum.OPERATE_DELETE.getCode());
        syncKingdeeBomInfoService.syncDataToSdy(bomInfoEntity, SyncOperateEnum.OPERATE_DELETE.getCode());
        boolean flag = this.removeById(bomId);
        if (flag) {
            bomSkuService.deleteByBomId(bomId);
            productBomHistoryService.deleteByBomId(bomId);
            String operateContent = BomOperateContent.DELETE;
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.DELETE.getType(), operateContent);
        }
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                if (CollectionUtils.isNotEmpty(pushTaskList)) {
                    dmpMqFeign.sendTask(pushTaskList);
                }
            }
        });
        return flag;
    }

    /**
     * 提交审核
     *
     * @param bomId
     * @return boolean
     * @author yl
     * @date 2023-01-13 9:58
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO submitAudit(String bomId,Boolean isStartProcess) {
        BomInfoEntity bom = this.getById(bomId);
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        Integer state = bom.getState();
        if (!BomStateEnum.WAIT_SUBMIT_AUDIT.getState().equals(state) && !BomStateEnum.AUDIT_NO_PASS.getState().equals(state)) {
            throw new ServiceException(ApiError.ERROR_95098);
        }
        bom.setState(BomStateEnum.AUDIT_ING.getState());
        //提交流程
        if (isStartProcess) {
            startProcess(bom);
        }
        /**
         * 这里要发起一个流程
         */
        Boolean result = this.updateById(bom);
        if (result) {
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.WAIT_SUBMIT_AUDIT.getName(), BomStateEnum.AUDIT_ING.getName());
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
        }
        return BatchResultDTO.success(bom.getId(), bom.getSerialNumber(), OperationTypeEnum.SUBMIT);
    }

    /**
     * 启动流程
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/
    public void startProcess(BomInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getSerialNumber());
        startDTO.setBusinessKey(SourceTypeEnum.PRODUCT_BOM_INFO.getCode());
        startDTO.setBusinessName(entity.getSerialNumber());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * 冻结boom
     *
     * @param bomId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-13 16:22
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean freeze(String bomId) {
        BomInfoEntity bom = this.getById(bomId);
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        Integer state = bom.getState();
        if (!BomStateEnum.AUDIT_PASS.getState().equals(state)) {
            throw new ServiceException(ApiError.ERROR_95100);
        }
        List<String> bomIds = Arrays.asList(bomId);
        List<String> changeIngSourceIds = productChangeService.getBySourceId(bomIds);
        if (changeIngSourceIds.contains(bomId)) {
            throw new ServiceException(ApiError.ERROR_95114);
        }
        bom.setState(BomStateEnum.FREEZE.getState());
        Boolean result = this.updateById(bom);
        if (result) {
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.AUDIT_PASS.getName(), BomStateEnum.FREEZE.getName());
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
        }
        return result;
    }

    /**
     * 解冻
     *
     * @param bomId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-13 16:29
     */
    @Override
    public Boolean defrost(String bomId) {
        BomInfoEntity bom = this.getById(bomId);
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        Integer state = bom.getState();
        if (!BomStateEnum.FREEZE.getState().equals(state)) {
            throw new ServiceException(ApiError.ERROR_95101);
        }
        bom.setState(BomStateEnum.AUDIT_PASS.getState());
        Boolean result = this.updateById(bom);
        if (result) {
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.FREEZE.getName(), BomStateEnum.AUDIT_PASS.getName());
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
        }
        return result;
    }

    /**
     * 报废boom
     *
     * @param bomId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-13 16:33
     */
    @Override
    public Boolean scrap(String bomId) {
        BomInfoEntity bom = this.getById(bomId);
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        Integer state = bom.getState();
        List<Integer> stateList = new ArrayList<>(2);
        stateList.add(BomStateEnum.AUDIT_PASS.getState());
        stateList.add(BomStateEnum.FREEZE.getState());
        if (!stateList.contains(state)) {
            throw new ServiceException(ApiError.ERROR_95102);
        }
        bom.setState(BomStateEnum.SCRAP.getState());
        Boolean result = this.updateById(bom);
        if (result) {
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.getName(state), BomStateEnum.SCRAP.getName());
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
        }
        return result;
    }

    /**
     * 恢复bom
     *
     * @param bomId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-13 17:05
     */
    @Override
    public Boolean recover(String bomId) {
        BomInfoEntity bom = this.getById(bomId);
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        Integer state = bom.getState();
        if (!BomStateEnum.SCRAP.getState().equals(state)) {
            throw new ServiceException(ApiError.ERROR_95103);
        }
        bom.setState(BomStateEnum.AUDIT_PASS.getState());

        Boolean result = this.updateById(bom);
        if (result) {
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.SCRAP.getName(), BomStateEnum.AUDIT_PASS.getName());
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
        }
        return result;

    }


    /**
     * 解除归档
     *
     * @param bomId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-29 16:43
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean removeArchive(String bomId) {
        BomInfoEntity bom = this.getById(bomId);
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        Integer state = bom.getState();
        if (!BomStateEnum.AUDIT_PASS.getState().equals(state)) {
            throw new ServiceException(ApiError.ERROR_95108);
        }
        List<String> bomIds = Arrays.asList(bomId);
        List<String> changeIngSourceIds = productChangeService.getBySourceId(bomIds);
        if (changeIngSourceIds.contains(bomId)) {
            throw new ServiceException(ApiError.ERROR_95114);
        }
        //检查能否反审核
        checkRemoveArchive(bomId);

        bom.setState(BomStateEnum.WAIT_SUBMIT_AUDIT.getState());
        Boolean result = this.updateById(bom);
        if (result) {
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.AUDIT_PASS.getName(), BomStateEnum.WAIT_SUBMIT_AUDIT.getName());
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);

            //发送金蝶
            sendPushTask(Arrays.asList(bom),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        }
        return result;
    }


    /**
     * 根据bomid 检查 能否反审核
     *
     * @param bomId
     * @return void
     * @author yl
     * @date 2023-10-11 19:26
     */
    private void checkRemoveArchive(String bomId) {
        List<BomSkuEntity> bomSkuList = bomSkuService.listBomSkuByBomId(bomId);
        if (CollectionUtils.isEmpty(bomSkuList)) {
            return;
        }
        //这个是父级的sku no
        String parentSkuNo = bomSkuList.get(0).getParentSkuNo();
        //这个是父级的sku id
        String parentSkuId = bomSkuList.get(0).getParentSkuId();
        MachineInfoDTO.FindInfoBySkuDTO dto = new MachineInfoDTO.FindInfoBySkuDTO();
        dto.setSkuNo(parentSkuNo);
        dto.setSkuId(parentSkuId);
        //加工单的
        List<MachineInfoDTO.ListDTO> machineList = wmsTaskFeign.listBySku(dto);
        if (CollectionUtils.isNotEmpty(machineList)) {
            throw new ServiceException("该bom已被加工单引用,无法解除归档");
        }
        //委外加工单
        List<SubcontractOrderDTO.ListDTO> subcontractList = scmTaskFeign.listByBomSku(parentSkuId);
        if (CollectionUtils.isNotEmpty(subcontractList)) {
            throw new ServiceException("该bom已被委外订单引用,无法解除归档");
        }
    }


    /**
     * 检查bom 能否变更
     *
     * @param sourceId
     * @return void
     * @author yl
     * @date 2023-01-29 17:04
     */
    @Override
    public void checkIfChange(String sourceId, String detailsJson) {
        BomInfoEntity infoEntity = this.getById(sourceId);
        if (Objects.isNull(infoEntity)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        Integer state = infoEntity.getState();
        //只有归档才能变更
        if (!BomStateEnum.AUDIT_PASS.getState().equals(state)) {
            throw new ServiceException(ApiError.ERROR_95104);
        }
        List<String> changeIngSourceIds = productChangeService.getBySourceId(Arrays.asList(sourceId));
        if (CollectionUtils.isNotEmpty(changeIngSourceIds)) {
            throw new ServiceException(ApiError.ERROR_95113);
        }
        BomDTO bom = JSON.parseObject(detailsJson, BomDTO.class);
        UpdateBomDTO updateBom = new UpdateBomDTO();
        updateBom.setId(bom.getId());
        updateBom.setSkuList(bom.getSkuList());
        checkRepeatBomSku(updateBom);


    }


    /**
     * 修改状态
     *
     * @param sourceId
     * @param state
     * @return void
     * @author yl
     * @date 2023-01-29 17:13
     */
    @Override
    public void updateState(String sourceId, Integer state) {
        LambdaUpdateWrapper<BomInfoEntity> updateWrapper = new LambdaUpdateWrapper<BomInfoEntity>();
        updateWrapper.set(BomInfoEntity::getState, state);
        updateWrapper.eq(BomInfoEntity::getId, sourceId);
        this.update(updateWrapper);
    }


    /**
     * 导出数据
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-01-29 17:32
     */
    @Override
    public void exportExcel(SearchPagingDTO dto) {
        downloadTaskFeign.saveDownloadTask("BOM数据", EXPORT_PLM_BOM.getCode(), dto);
    }

    /**
     * 变更bom
     *
     * @param bom
     * @return void
     * @author yl
     * @date 2023-01-30 16:52
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void changeBom(BomDTO bom) {
        String bomId = bom.getId();
        BomInfoEntity bomEntity = this.getById(bomId);
        if (bomEntity != null) {
            String bomVersion = bomEntity.getBomVersion();
            bomEntity.setBomVersion(MathUtil.add(MathUtil.valueOf(bomVersion), BigDecimal.ONE).toString());
            bomEntity.setType(bom.getType());
            List<BomSkuDTO> oldBomList = bomSkuService.getByBomId(bomId);
            List<BomSkuDTO> bomSkuList = bom.getSkuList();
            Boolean result = this.updateById(bomEntity);
            if (result) {
                //保存历史bom信息
                productBomHistoryService.insert(bomEntity, bomSkuList);
                //添加 bom 与sku 关系
                bomSkuService.updateBomSku(bomId, bomSkuList);

                String operateContent = getUpdateContent(oldBomList, bomSkuList);
                bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.UPDATE.getType(), operateContent);

                //发送金蝶
                sendPushTask(Arrays.asList(bomEntity),SyncOperateEnum.OPERATE_APPROVE.getCode());
            }
        }
    }

    /**
     * 审核情况
     *
     * @param bomId
     * @return void
     * @author yl
     * @date 2023-02-08 9:00
     */
    @Override
    public List<ApproveNodeRecordVO> auditInfo(String bomId) {
        if (StringUtils.isNotBlank(bomId)) {
            List<ApproveNodeRecordVO> list = workflowFeign.getHistoryTaskByBusinessTableId(bomId);
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public List<BomVO> getByIds(List<String> bomIdList) {
        if (CollectionUtils.isEmpty(bomIdList)) {
            return new ArrayList<>();
        }
        return baseMapper.getByIds(bomIdList);
    }


    /**
     * 这个是在变更申请的时候 获取到bom 列表
     * 只要审核通过的
     *
     * @param
     * @return java.util.List<com.erp.common.dto.base.BaseIdDTO>
     * @author yl
     * @date 2023-01-28 17:08
     */
    @Override
    public List<ChangeInfoDTO> getBomInfo(String searchKeyword) {
        return baseMapper.getBomInfo(BomStateEnum.AUDIT_PASS.getState(), searchKeyword);
    }


    /**
     * 检查bom 能否修改
     *
     * @param state
     * @param updateFlag
     * @return void
     * @author yl
     * @date 2023-01-12 18:20
     */
    private void checkBomCanUpdate(Integer state, String updateFlag) {
        //如果是编辑  在待提交审核/待审核状态/审核不通过 可点击编辑
        if (BomConstant.EDIT.equals(updateFlag)) {
            List<Integer> stateList = new ArrayList<>(5);
            stateList.add(BomStateEnum.WAIT_SUBMIT_AUDIT.getState());
            stateList.add(BomStateEnum.AUDIT_NO_PASS.getState());
            if (!stateList.contains(state)) {
                throw new ServiceException(ApiError.ERROR_95096);
            }
        }
        //如果是变更申请 只有审核通过 就是归档 才能申请
        if (BomConstant.CHANGE_REQUEST.equals(updateFlag)) {
            List<Integer> stateList = new ArrayList<>(2);
            stateList.add(BomStateEnum.AUDIT_PASS.getState());
            if (!stateList.contains(state)) {
                throw new ServiceException(ApiError.ERROR_95096);
            }
        }
    }

    /**
     * @description: bom数据验证
     * @author Will
     * @date: 2023/8/16 18:14
     * @param dto
     */
    private void checkRepeatBomSku(UpdateBomDTO dto) {
        List<BomSkuDTO> skuList = dto.getSkuList();
        List<String> parentSkuNoList = skuList.stream().map(BomSkuDTO::getSkuNo).collect(Collectors.toList());
        List<String> childrenSkuIdList = new ArrayList<>(10);
        for (BomSkuDTO item : skuList) {
            List<String> childrenSkuIds = item.getChildren().stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toList());
            childrenSkuIdList.addAll(childrenSkuIds);
        }
        checkChildrenIsParent(parentSkuNoList, childrenSkuIdList);
        List<String> skuIds = skuList.stream().map(BomSkuDTO::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> childList = bomSkuService.listAllBomChildBySkuIds(skuIds);
        for (BomSkuDTO bomSkuDTO : skuList) {
            //这个是父级的sku id
            String parentSkuId = bomSkuDTO.getSkuId();
            long count = childList.stream().filter(obj -> !obj.getBomId().equals(dto.getId()) && obj.getParentSkuId().equals(parentSkuId)).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_BOM_PARENT_SKU_REPEAT, bomSkuDTO.getSkuNo());
            }

        }
    }

    /**
     * @return
     * @parms 检查子集是否有父级的sku
     * @author yl
     * @date 2023-11-24
     */
    private void checkChildrenIsParent(List<String> parentSkuNoList, List<String> skuIdList) {
        List<BomDTO.BomSku> bomSkuList = bomSkuService.listBySkuIds(skuIdList);
        for (String skuNo : parentSkuNoList) {
            String bomCode = bomSkuList.stream().filter(b -> skuNo.equals(b.getSkuNo())).
                    map(BomDTO.BomSku::getSerialNumber).
                    collect(Collectors.joining(","));
            /**
             * 这个表示 bomSkuDTO 的子的sku  为bomm 的父级sku 而该Bom 的子sku 有为bomSkuDTO 的父级
             * 这样就会有问题
             */
            if (StringUtils.isNotEmpty(bomCode)) {
                throw new ServiceException(ApiError.ERROR_BOM_CONTAIN, bomCode, skuNo);
            }
        }
        List<String> skuNoList = bomSkuList.stream().map(BomDTO.BomSku::getParentSkuNo).collect(Collectors.toList());
        List<String> newSkuNOList=new ArrayList<>(10);
        newSkuNOList.addAll(parentSkuNoList);
        newSkuNOList.addAll(skuNoList);
        for (BomDTO.BomSku item : bomSkuList) {
            String childrenSkuId = item.getSkuId();
            checkChildrenIsParent(newSkuNOList, Arrays.asList(childrenSkuId));
        }

    }


    /**
     * @description: 处理导入数据
     * @author Will
     * @date: 2023/8/30 15:10
     * @param successList
     * @param errorList
     */
    private void handleImportSuccessList (List<BomInfoExcelDTO> successList, List<BomInfoExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        List<String> skuNos = successList.stream().flatMap(obj -> Stream.of(obj.getChildSku(),obj.getParentSku())).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = productDetailService.getSkuBySkuNos(skuNos);

        List<String> parentSkuNos = successList.stream().map(BomInfoExcelDTO::getParentSku).distinct().collect(Collectors.toList());
        List<BomInfoEntity> bomInfoList = bomSkuService.listAllBomByParentSkuNos(parentSkuNos);
        Map<String, List<BomInfoExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(BomInfoExcelDTO::getParentSku));
        for (Map.Entry<String, List<BomInfoExcelDTO>> entry :  map.entrySet()) {
            List<BomInfoExcelDTO> value = entry.getValue();
            Boolean isError = Boolean.FALSE;
            for (BomInfoExcelDTO addDTO : value) {
                List<String> errorMsgList = checkImport(addDTO, skuList, bomInfoList);

                //存在错误信息则
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    isError = Boolean.TRUE;
                    addDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    break;
                }
            }
            //更新错误数据
            if (isError) {
                errorList.addAll(value);
                continue;
            }
            //数据新增或修改
            String key = entry.getKey();
            //父级SKU
            SkuVO parentSkuVO = skuList.stream().filter(obj -> obj.getSkuNo().equals(key)).findFirst().orElse(null);
            if (parentSkuVO == null) {
                throw new ServiceException(ApiError.ERROR_95166);
            }
            BomInfoEntity bomInfoEntity = bomInfoList.stream().filter(obj -> obj.getParentSkuId().equals(parentSkuVO.getSkuId())).findFirst().orElse(null);
            if (bomInfoEntity == null) {
                //新增
                AddBomDTO addBomDTO = new AddBomDTO();
                BomInfoExcelDTO excelDTO = value.get(0);
                addBomDTO.setType(BomTypeEnum.getType(excelDTO.getTypeName()));
                addBomDTO.setVersion(StringPool.ONE);
                addBomDTO.setSubmitType(BomTypeEnum.CREATE.getType());
                //获取sku信息
                List<BomSkuDTO> parentSkuList = getBomSkuList(parentSkuVO,value,skuList);
                addBomDTO.setSkuList(parentSkuList);
                this.insert(addBomDTO);
            } else {
                //修改
                UpdateBomDTO updateBomDTO = new UpdateBomDTO();
                updateBomDTO.setId(bomInfoEntity.getId());
                //获取sku信息
                List<BomSkuDTO> parentSkuList = getBomSkuList(parentSkuVO,value,skuList);
                updateBomDTO.setSkuList(parentSkuList);
                this.edit(updateBomDTO);
            }
        }
    }

    private List<String> checkImport (BomInfoExcelDTO addDTO,List<SkuVO> skuList,List<BomInfoEntity> bomInfoList) {
        List<String> errorMsgList = new ArrayList<>();

        if (StringUtils.equals(addDTO.getParentSku(),addDTO.getChildSku())) {
            errorMsgList.add("父级sku和子级sku不能重复");
        }
        //父级sku是否审核
        SkuVO parentSkuVO = skuList.stream().filter(obj -> obj.getSkuNo().equals(addDTO.getParentSku())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(parentSkuVO)) {
            errorMsgList.add(ApiError.ERROR_95152.msg);
        }
        //子级sku是否审核
        SkuVO childSkuVO = skuList.stream().filter(obj -> obj.getSkuNo().equals(addDTO.getChildSku())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(childSkuVO)) {
            errorMsgList.add(ApiError.ERROR_95153.msg);
        }
        //仅待提交或者审核不通过数据修改
        if (CollectionUtils.isNotEmpty(bomInfoList)) {
            BomInfoEntity bomInfoEntity = bomInfoList.stream().filter(obj -> obj.getParentSkuId().equals(parentSkuVO.getSkuId())).findFirst().orElse(null);
            if (bomInfoEntity != null && (!BomStateEnum.WAIT_SUBMIT_AUDIT.getState().equals(bomInfoEntity.getState()) && !BomStateEnum.AUDIT_NO_PASS.getState().equals(bomInfoEntity.getState()))) {
                errorMsgList.add("仅待提交审核和审核不通过BOM支持更新");
            }
        }
       return errorMsgList;
    }

    /**
     * @description: 格式化sku信息
     * @author Will
     * @date: 2023/8/30 15:10
     * @param parentSkuVO
     * @param value
     * @param skuList
     * @return List<BomSkuDTO>
     */
    private List<BomSkuDTO> getBomSkuList (SkuVO parentSkuVO,List<BomInfoExcelDTO> value,List<SkuVO> skuList) {
        List<BomSkuDTO> parentSkuList = new ArrayList<>();
        BomSkuDTO parentDTO = new BomSkuDTO();
        parentDTO.setSkuId(parentSkuVO.getSkuId());
        parentDTO.setSkuNo(parentSkuVO.getSkuNo());
        parentDTO.setProductId(parentSkuVO.getProductId());

        List<BomChildrenSkuDTO> childSkuList = new ArrayList<>();
        for (BomInfoExcelDTO bomInfoExcelDTO : value) {
            BomChildrenSkuDTO childDTO = new BomChildrenSkuDTO();
            //子级SKU
            SkuVO childSkuVO = skuList.stream().filter(obj -> obj.getSkuNo().equals(bomInfoExcelDTO.getChildSku())).findFirst().orElse(null);
            if (childSkuVO == null) {
                throw new ServiceException(ApiError.ERROR_95166);
            }
            childDTO.setSkuId(childSkuVO.getSkuId());
            childDTO.setSkuNo(childSkuVO.getSkuNo());
            childDTO.setSkuName(childSkuVO.getSkuName());
            childDTO.setParentSkuId(parentSkuVO.getSkuId());
            childDTO.setBomVersion(StringPool.ONE);
            childDTO.setQuantity(Integer.valueOf(bomInfoExcelDTO.getQuantityStr()));
            childDTO.setProductId(childSkuVO.getProductId());
            childSkuList.add(childDTO);
        }
        parentDTO.setChildren(childSkuList);
        parentSkuList.add(parentDTO);
        return parentSkuList;
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<BomInfoEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            List<DmpPushTaskEntity> pushTaskEntityList = syncKingdeeBomInfoService.syncDataToKingdee(obj, operate);
            syncKingdeeBomInfoService.syncDataToSdy(obj, operate);
            resultList.addAll(pushTaskEntityList);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }
}
