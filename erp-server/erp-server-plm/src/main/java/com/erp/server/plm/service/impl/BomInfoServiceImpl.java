package com.erp.server.plm.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
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
import com.erp.model.workflow.dto.BusinessTableDTO;
import com.erp.model.workflow.dto.ProcessPassDTO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.constant.BomOperateContent;
import com.erp.server.plm.listener.BomInfoExcelListener;
import com.erp.server.plm.mapper.BomInfoMapper;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeBomInfoService;
import com.erp.server.plm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_BOM;

/**
 * bom 信息表(BomInfo)表服务实现类
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
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

    /**
     * 添加bom
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-09 12:18
     */
    @Override
    @Transactional
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
        String submitAudit = BomConstant.SUBMIT_AUDIT;
        boolean isSubmitAudit = submitAudit.equals(dto.getSubmitType());
        if (isSubmitAudit) {
            bom.setState(BomStateEnum.WAIT_AUDIT.getState());
            checkAuditor(dto.getSkuList());
        }
        Boolean saveResult = this.save(bom);
        //保存成功
        if (saveResult) {
            //保存历史bom信息
            //TODO 2020330暂时取消审核流程，只修改状态
/*            //但是待审核的时候
            if (isSubmitAudit) {
                //这里要发起一个bom流程
                startBomProcess(bomId, dto.getSkuList());
            }*/
            //添加 bom 与sku 关系
            bomSkuService.saveBomSku(bomId, bomSkuList);
            //添加 bom的操作日志
            String operateContent = String.format(BomOperateContent.ADD, serialNumber);
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.ADD.getType(), operateContent);
            //更新父sku物流属性
            List<String> parentSkuIds = bomSkuList.stream().map(BomSkuDTO::getSkuId).distinct().collect(Collectors.toList());
            productLogisticsService.saveOrUpdateParentPropertyId(parentSkuIds);
        }
        return bomId;
    }


    /**
     * 检查审核人
     *
     * @param skuList
     * @return void
     * @author yl
     * @date 2023-02-03 12:13
     */
    @Override
    public List<String> getSkuIdList(List<BomSkuDTO> skuList) {

        List<String> skuIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skuList)) {
            for (BomSkuDTO item : skuList) {
                getSkuIdList(skuIdList, item);
            }
        }
        return skuIdList;

    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        BomInfoExcelListener excelListenerUtil = new BomInfoExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), BomInfoExcelDTO.class, excelListenerUtil).sheet(0).doRead();
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
            StringBuffer sb = new StringBuffer();
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
    public PagingVO<List<BomSkuPageDTO.ListDTO>> skuPaging(PagingDTO<BomSkuPageDTO.PagingParamDTO> dto) {
        BomSkuPageDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<BomSkuPageDTO.ListDTO> pageData = baseMapper.skuPaging(query, params);
        List<BomSkuPageDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
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

        Integer index = MathUtil.ONE;
        for (BomSkuPageDTO.ListDTO listDTO : records) {
            //付款条件
            if (CollectionUtils.isNotEmpty(supplierList)) {
                String paymentCondition = supplierList.stream().filter(obj -> obj.getId().equals(listDTO.getSupplierId()) && StringUtils.isNotBlank(obj.getPaymentCondition())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getPaymentCondition())).orElse("");
                listDTO.setPaymentCondition(paymentCondition);
            }
            //状态名称
            listDTO.setStatusName(ProductDetailStatusEnum.getName(listDTO.getStatus()));
            listDTO.setIndex(index);
            index++;
            //子件
            List<BomSkuPageDTO.ChildDTO> childDTOList = childList.stream().filter(obj -> obj.getBomId().equals(listDTO.getBomId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(childDTOList)) {
                for (BomSkuPageDTO.ChildDTO childDTO: childDTOList) {
                    //付款条件
                    if (CollectionUtils.isNotEmpty(supplierList)) {
                        String paymentCondition = supplierList.stream().filter(obj -> obj.getId().equals(childDTO.getSupplierId()) && StringUtils.isNotBlank(obj.getPaymentCondition())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getPaymentCondition())).orElse("");
                        childDTO.setPaymentCondition(paymentCondition);
                    }
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

        return new PagingVO(pageData);
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

    /**
     * 获取到skuId
     *
     * @param item
     * @return void
     * @author yl
     * @date 2023-02-03 12:18
     */
    private void getSkuIdList(List<String> skuIdList, BomSkuDTO item) {
        skuIdList.add(item.getSkuId());
        List<BomChildrenSkuDTO> childrenList = item.getChildren();
        if (CollectionUtils.isNotEmpty(childrenList)) {
            List<String> childrenSkuIds = childrenList.stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toList());
            skuIdList.addAll(childrenSkuIds);
        }
    }


    /**
     * 启动一个流程
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-01-31 14:44
     */
    //TODO 2020330暂时取消审核流程，只修改状态
    /*public void startBomProcess(String bomId, List<BomSkuDTO> skuList) {
        FindProcessDTO findProcess = new FindProcessDTO();
        String userId = UserContext.getDefaultLoginUser().getUid();
        String businessType = WorkflowBusinessEnum.BOM_AUDIT.getBusinessType();
        String platform = WorkflowBusinessEnum.BOM_AUDIT.getPlatform();
        findProcess.setBusinessType(businessType);
        findProcess.setPlatform(platform);
        //获取到业务的信息
        BusinessInfoDTO business = workflowFeign.getBusiness(findProcess);
        if (business != null) {
            StartProcessDTO startProcess = new StartProcessDTO();
            startProcess.setUserId(userId);
            startProcess.setProcessDefinitionKey(business.getProcessDefinitionKey());
            startProcess.setBusinessKey(business.getBusinessKey());
            Map<String, Object> parameterMap = new HashMap<>();

            //skuId
            List<String> skuIdList = getSkuIdList(skuList);

            //产品经理
            //Arrays.asList("1")
            List<String> productManagerList = productDetailService.getManagerBySkuIds(skuIdList);
            if (CollectionUtils.isEmpty(productManagerList)) {
                throw new ServiceException(ApiError.ERROR_9030);
            }
            //产品经理
            parameterMap.put("productManagerList", productManagerList);
            //Arrays.asList("2");
            List<String> productManagerSupervisorList = productDetailService.getApproveLead(SkuApproveConfigureEnum.SECOND_APPROVE.getDesc());
            if (CollectionUtils.isEmpty(productManagerSupervisorList)) {
                throw new ServiceException(ApiError.ERROR_9031);
            }
            //产品经理上级
            parameterMap.put("productManagerSupervisorList", productManagerSupervisorList);
            //Arrays.asList("3");
            List<String> departmentHeadList = productDetailService.getApproveLead(SkuApproveConfigureEnum.FIVE_APPROVE.getDesc());
            //
            if (CollectionUtils.isEmpty(departmentHeadList)) {
                throw new ServiceException(ApiError.ERROR_9032);
            }
            //产品部负责人
            parameterMap.put("departmentHead", departmentHeadList.get(0));
            startProcess.setParameterMap(parameterMap);
            //启动流程
            ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
            //流程id
            String processId = processResult.getProcessId();
            if (StringUtils.isNotBlank(processId)) {
                WorkflowBusinessProcessDTO businessProcess = new WorkflowBusinessProcessDTO();
                businessProcess.setBusinessId(business.getId());
                businessProcess.setCreateTime(LocalDateTime.now());
                businessProcess.setBusinessTableId(bomId);
                businessProcess.setCreateUserId(userId);
                businessProcess.setProcessId(processId);
                //保存业务与流程的信息
                workflowFeign.saveBusinessProcess(businessProcess);
            }

        }

    }*/


    /**
     * 检查审核人不能为空
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-02-01 18:20
     */
    @Override
    public void checkAuditor(List<BomSkuDTO> skuList) {
        //TODO 暂时取消流程
/*        //skuId
        List<String> skuIdList = getSkuIdList(skuList);
        //产品经理
        List<String> productManagerList = productDetailService.getManagerBySkuIds(skuIdList);
        if (CollectionUtils.isEmpty(productManagerList)) {
            throw new ServiceException(ApiError.ERROR_9030);
        }
        //产品部负责人
        List<String> productManagerSupervisorList = productDetailService.getApproveLead(SkuApproveConfigureEnum.SECOND_APPROVE.getDesc());
        if (CollectionUtils.isEmpty(productManagerSupervisorList)) {
            throw new ServiceException(ApiError.ERROR_9031);
        }
        //研发中心负责人
        List<String> departmentHeadList = productDetailService.getApproveLead(SkuApproveConfigureEnum.FIVE_APPROVE.getDesc());
        if (CollectionUtils.isEmpty(departmentHeadList)) {
            throw new ServiceException(ApiError.ERROR_9032);
        }*/
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
    public PagingVO<List<BomPagingVO>> paging(PagingDTO<SearchPagingDTO> dto) {
        SearchPagingDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        String searchKeyword = params.getSearchKeyword();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        List<String> bomIdList = new ArrayList<>();
        //当这个不为空的时候 表示可能要搜索 sku 或者 sku名称 或者bom 编号
        List<String> skuIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(searchKeyword)) {
            skuIdList = productChangeService.getChangeSearchCondition(searchKeyword);
            if (CollectionUtils.isEmpty(skuIdList)) {
                IPage pageData = new Page();
                return new PagingVO(pageData);
            }
        }
        IPage pageData = baseMapper.paging(query, params, bomIdList, skuIdList);
        List<BomPagingVO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
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
        return new PagingVO(pageData);
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

    @Override
    public String getExcelUpdateContent( List<BomSkuEntity> newBomList,  List<BomSkuEntity> oldBomList) {
        List<String> contentList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(oldBomList) && CollectionUtils.isNotEmpty(newBomList)) {
            String oldSkuNo = oldBomList.get(0).getParentSkuNo();
            String newSkuNo = newBomList.get(0).getParentSkuNo();
            if (!oldSkuNo.equals(newSkuNo)) {
                String parentContent = "父物料" + oldSkuNo + " 变更到" + newSkuNo;
                contentList.add(parentContent);
            }
            if (!newBomList.equals(oldBomList)) {
                List<BomChildrenSkuDTO> oldList = BeanMapperUtils.copyList(BomChildrenSkuDTO.class, oldBomList);
                List<BomChildrenSkuDTO> newList = BeanMapperUtils.copyList(BomChildrenSkuDTO.class, newBomList);
                getChildrenUpdateContent(oldList, newList, contentList);
            }
        }

        return String.join(";", contentList);
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
    public Boolean submitAudit(String bomId) {
        BomInfoEntity bom = this.getById(bomId);
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        Integer state = bom.getState();
        if (!BomStateEnum.WAIT_SUBMIT_AUDIT.getState().equals(state)) {
            throw new ServiceException(ApiError.ERROR_95098);
        }
        bom.setState(BomStateEnum.WAIT_AUDIT.getState());
        List<BomSkuDTO> skuList = bomSkuService.getByBomId(bomId);
        checkAuditor(skuList);
        /**
         * 这里要发起一个流程
         */
        Boolean result = this.updateById(bom);
        if (result) {
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.WAIT_SUBMIT_AUDIT.getName(), BomStateEnum.WAIT_AUDIT.getName());
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
            //发起bom 流程
            //TODO 2020330暂时取消审核流程，只修改状态
            /*startBomProcess(bomId, skuList);*/
        }
        return result;
    }


    /**
     * 重启流程
     *
     * @param bomId
     * @return boolean
     * @author yl
     * @date 2023-01-13 16:16
     */
    @Override
    @Transactional
    public Boolean restartAudit(String bomId) {
        BomInfoEntity bom = this.getById(bomId);
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        Integer state = bom.getState();
        if (!BomStateEnum.AUDIT_NO_PASS.getState().equals(state)) {
            throw new ServiceException(ApiError.ERROR_95099);
        }
        bom.setState(BomStateEnum.WAIT_AUDIT.getState());

        List<BomSkuDTO> list = bomSkuService.getByBomId(bomId);
        checkAuditor(list);
        /**
         * 这里要发起一个流程
         */
        Boolean result = this.updateById(bom);
        if (result) {
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.AUDIT_NO_PASS.getName(), BomStateEnum.WAIT_AUDIT.getName());
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);

            //发起bom 流程
            //TODO 2020330暂时取消审核流程，只修改状态
            /*startBomProcess(bomId, list);*/
        }
        return result;

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
            String code = subcontractList.stream().map(SubcontractOrderDTO.ListDTO::getCode).distinct()
                    .collect(Collectors.joining(","));
            throw new ServiceException("该bom已被委外加工单引用,无法解除归档");
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
        BomDTO bom = JSONObject.parseObject(detailsJson, BomDTO.class);
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
        LambdaUpdateWrapper<BomInfoEntity> updateWrapper = new LambdaUpdateWrapper();
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
     * 审核通过
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-01-29 18:55
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void approvalPass(AuditParamDTO dto) {
        BomInfoEntity bom = this.getById(dto.getId());
        //意见
        String comment = dto.getComment();
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }

        //是不是 第一次审核
        Boolean isFirstAudit = BomStateEnum.WAIT_AUDIT.getState().equals(bom.getState());
        bom.setState(BomStateEnum.AUDIT_PASS.getState());
        bom.setRemark(dto.getComment());
        Boolean result = this.updateById(bom);

        //TODO 2020330暂时取消审核流程，只修改状态
/*        String userId = UserContext.getDefaultLoginUser().getUid();
        BusinessTableDTO tableDTO = new BusinessTableDTO();
        tableDTO.setBusinessTableId(bom.getId());
        tableDTO.setUserId(userId);
        //获取到用户该业务表的待办任务
        MyToDoTaskVO processTask = workflowFeign.getByBusinessTableId(tableDTO);
        if (Objects.isNull(processTask)) {
            throw new ServiceException(ApiError.ERROR_94005);
        }

        //审核
        ApproveProcessDTO approveProcess = new ApproveProcessDTO();
        approveProcess.setTaskId(processTask.getTaskId());
        approveProcess.setProcessInstanceId(processTask.getProcessInstanceId());
        approveProcess.setUserId(userId);
        approveProcess.setComment(comment);
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("agree", true);
        approveProcess.setParameterMap(parameterMap);
        ProcessNodeDTO node = workflowFeign.taskPass(approveProcess);*/
//        if (node != null) {
        if (result && isFirstAudit) {
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.WAIT_AUDIT.getName(), BomStateEnum.AUDIT_PASS.getName() + "  审核意见：" + comment);
            //操作记录
            bomOperateLogService.saveOperate(bom.getId(), BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
            //保存bom 审核通过的的历史数据
            productBomHistoryService.saveBomApprovalHistory(bom);
        }

        //发送金蝶
        sendPushTask(Arrays.asList(bom),SyncOperateEnum.OPERATE_APPROVE.getCode());
    }


    /**
     * 当bom 流程审核通过后
     * 改变bom 状态
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-01-30 8:54
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void bomProcessPass(ProcessPassDTO dto) {
        String bomId = dto.getBusinessTableId();
        BomInfoEntity bom = this.getById(bomId);
        if (bom != null) {
            bom.setState(BomStateEnum.AUDIT_PASS.getState());
            this.updateById(bom);

            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.AUDIT_ING.getName(), BomStateEnum.AUDIT_PASS.getName());
            //操作记录
            bomOperateLogService.saveOperate(bom.getId(), BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);

            //发送金蝶
            sendPushTask(Arrays.asList(bom),SyncOperateEnum.OPERATE_APPROVE.getCode());
        }
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

    /**
     * bom 审核不通过
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-01-29 18:53
     */
    @Override
    public void approvalNoPass(AuditParamDTO dto) {
        BomInfoEntity bom = this.getById(dto.getId());
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }

        //是不是 第一次审核
        Boolean isFirstAudit = BomStateEnum.WAIT_AUDIT.getState().equals(bom.getState());


        bom.setState(BomStateEnum.AUDIT_NO_PASS.getState());
        bom.setRemark(dto.getComment());
        String userId = UserContext.getDefaultLoginUser().getUid();
        BusinessTableDTO tableDTO = new BusinessTableDTO();
        tableDTO.setBusinessTableId(bom.getId());
        tableDTO.setUserId(userId);
        //获取到用户该业务表的待办任务
        //TODO 2020330暂时取消审核流程，只修改状态
/*        MyToDoTaskVO processTask = workflowFeign.getByBusinessTableId(tableDTO);
        if (Objects.isNull(processTask)) {
            throw new ServiceException(ApiError.ERROR_94005);
        }*/

        //流程需要关闭吗
        Boolean result = this.updateById(bom);

       /* ApproveProcessDTO process = new ApproveProcessDTO();
        process.setComment(dto.getComment());
        process.setProcessInstanceId(processTask.getProcessInstanceId());
        process.setUserId(userId);
        process.setTaskId(processTask.getTaskId());

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("agree", false);
        process.setParameterMap(parameterMap);
        workflowFeign.taskNoPass(process);*/

        if (result) {
            String statusName = BomStateEnum.AUDIT_ING.getName();
            if (isFirstAudit) {
                statusName = BomStateEnum.WAIT_AUDIT.getName();
            }
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, statusName, BomStateEnum.AUDIT_NO_PASS.getName() + "  审核意见：" + dto.getComment());
            //操作记录
            bomOperateLogService.saveOperate(bom.getId(), BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
        }


    }


    /**
     * bom 发起变更
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-14 15:06
     */
    @Override
    public Boolean startChange(UpdateBomDTO dto) {
        BomInfoEntity bom = this.getById(dto.getId());
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        Integer state = bom.getState();
        //只有归档才能申请变更
        if (!BomStateEnum.AUDIT_PASS.getState().equals(state)) {
            throw new ServiceException(ApiError.ERROR_95104);
        }
        AddChangeDTO change = new AddChangeDTO();
        change.setSourceId(dto.getId());
        change.setDetailsJson(JSONObject.toJSONString(dto.getSkuList()));
        Boolean changeResult = productChangeService.add(change);
        //当成功后改变bom 的状态为待审核
        if (changeResult) {
            bom.setState(BomStateEnum.WAIT_AUDIT.getState());
            return this.updateById(bom);
        }
        return false;
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
            stateList.add(BomStateEnum.WAIT_AUDIT.getState());
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
                    if (ObjectUtils.isNotEmpty(bomInfoEntity)) {
                        if (!BomStateEnum.WAIT_SUBMIT_AUDIT.getState().equals(bomInfoEntity.getState()) && !BomStateEnum.AUDIT_NO_PASS.getState().equals(bomInfoEntity.getState())) {
                            errorMsgList.add("仅待提交审核和审核不通过BOM支持更新");
                        }
                    }
                }
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
            if (ObjectUtils.isEmpty(parentSkuVO)) {
                throw new ServiceException(ApiError.ERROR_95166);
            }
            BomInfoEntity bomInfoEntity = bomInfoList.stream().filter(obj -> obj.getParentSkuId().equals(parentSkuVO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(bomInfoEntity)) {
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
            if (ObjectUtils.isEmpty(childSkuVO)) {
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
