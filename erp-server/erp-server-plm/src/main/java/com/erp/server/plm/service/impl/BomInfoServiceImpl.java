package com.erp.server.plm.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SkuApproveConfigureEnum;
import com.common.business.enums.WorkflowBusinessEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.BusinessNoCreateUtil;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.BomInfoExcelDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomOperationTypeEnum;
import com.erp.model.plm.enums.BomStateEnum;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.model.plm.vo.BomPagingVO;
import com.erp.model.plm.vo.BomVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.constant.BomOperateContent;
import com.erp.server.plm.constant.SearchType;
import com.erp.server.plm.listener.BomInfoExcelListener;
import com.erp.server.plm.mapper.BomInfoMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public Boolean insert(AddBomDTO dto) {
        //sku信息
        List<BomSkuDTO> bomSkuList = dto.getSkuList();
        if (CollectionUtils.isEmpty(bomSkuList)) {
            throw new ServiceException(ApiError.ERROR_95094);
        }
        //获取到最大的序号
        Integer maxSequence = getMaxSequence();
        //获取到 编号
        String serialNumber = BusinessNoCreateUtil.getBusinessNo(BomConstant.BOM, maxSequence);
        BomInfoEntity bom = new BomInfoEntity();
        String bomId = IdWorker.getIdStr();
        bom.setType(dto.getType());
        bom.setVersion(dto.getVersion());
        bom.setId(bomId);
        bom.setSerialNumber(serialNumber);
        bom.setSequence(maxSequence + 1);
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
            productBomHistoryService.insert(bom, bomSkuList);
            //但是待审核的时候
            if (isSubmitAudit) {
                //这里要发起一个bom流程
                startBomProcess(bomId, dto.getSkuList());
            }
            //添加 bom 与sku 关系
            bomSkuService.saveBomSku(bomId, bomSkuList);
            //添加 bom的操作日志
            String operateContent = String.format(BomOperateContent.ADD, serialNumber);
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.ADD.getType(), operateContent);
        }
        return saveResult;
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
        //查询审核通过的sku
        List<ProductDetailEntity> productDetailList = productDetailService.listByAuditPass();
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95154);
        }
        BomInfoExcelListener excelListenerUtil = new BomInfoExcelListener(this,productDetailService,bomSkuService,productDetailList);
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
        List<BomInfoExcelDTO> list = excelListenerUtil.getErrorList();
        if (list.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/bomInfoError.xlsx";
            String name = "bomInfo";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus) {
        return  this.lambdaUpdate()
                .eq(BomInfoEntity::getId,id)
                .set(BomInfoEntity::getSyncKingdeeStatus,syncKingdeeStatus)
                .set(BomInfoEntity::getSyncKingdeeTime, LocalDateTime.now())
                .update();
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
    public void startBomProcess(String bomId, List<BomSkuDTO> skuList) {
        FindProcessDTO findProcess = new FindProcessDTO();
        String userId = commonService.getUserInfo().getUid();
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

    }


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

        //skuId
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
        }
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
        params.setParam(dto.getParam());
        String searchKeyword = params.getSearchKeyword();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        String searchType = params.getSearchType();
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

        List<Integer> stateList = new ArrayList<>();
        //待审核
        if (SearchType.WAIT_AUDIT.equals(searchType)) {
            String userId = commonService.getUserInfo().getUid();
            //获取我的待办信息
            List<MyToDoTaskVO> myToDoTasks = workflowFeign.getMyToDoTasks(userId);
            bomIdList = myToDoTasks.stream().map(MyToDoTaskVO::getBusinessTableId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(bomIdList)) {
                IPage pageData = new Page();
                return new PagingVO(pageData);
            }
        }

        IPage pageData = baseMapper.paging(query, params, bomIdList, skuIdList, stateList);
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
        checkBomCanUpdate(bom.getState(), BomConstant.EDIT);
        Integer bomVersion = bom.getVersion();
        List<BomSkuDTO> oldBomList = bomSkuService.getByBomId(id);
        bom.setVersion(bomVersion + 1);
        Boolean result = this.updateById(bom);
        List<BomSkuDTO> bomSkuList = dto.getSkuList();
        if (result) {
            //保存历史bom信息
            productBomHistoryService.insert(bom, bomSkuList);
            //添加 bom 与sku 关系
            bomSkuService.updateBomSku(id, bomSkuList);
            String operateContent = getUpdateContent(oldBomList, bomSkuList);
            if (StringUtils.isNotBlank(operateContent)) {
                bomOperateLogService.saveOperate(id, BomOperationTypeEnum.UPDATE.getType(), operateContent);
            }
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
    private String getUpdateContent(List<BomSkuDTO> oldBomList, List<BomSkuDTO> newBomList) {
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
        if (oldSize > newSize) {
            String removeContent = "删除了" + (oldSize - newSize) + "个子物料";
            contentList.add(removeContent);
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
    public Boolean deleteById(String bomId) {
        boolean flag = this.removeById(bomId);
        if (flag) {
            bomSkuService.deleteByBomId(bomId);
            productBomHistoryService.deleteByBomId(bomId);
            String operateContent = BomOperateContent.DELETE;
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.DELETE.getType(), operateContent);
        }
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
            startBomProcess(bomId, skuList);
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
            startBomProcess(bomId, list);
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
        bom.setState(BomStateEnum.WAIT_SUBMIT_AUDIT.getState());
        Boolean result = this.updateById(bom);
        if (result) {
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.AUDIT_PASS.getName(), BomStateEnum.WAIT_SUBMIT_AUDIT.getName());
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
        }
        return result;
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
    public void checkIfChange(String sourceId) {
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
    public void exportExcel(SearchPagingDTO dto, HttpServletResponse response) {
        String fileName = "BOM数据";
        String searchType = dto.getSearchType();
        String searchKeyword = dto.getSearchKeyword();
        //当这个不为空的时候 表示可能要搜索 sku 或者 sku名称 或者bom 编号
        List<String> skuIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(searchKeyword)) {
            skuIdList = productChangeService.getChangeSearchCondition(searchKeyword);
        }
        List<Integer> stateList = new ArrayList<>();
        //待审核
        List<String> bomIdList = new ArrayList<>();
        if (SearchType.WAIT_AUDIT.equals(searchType)) {
            String userId = commonService.getUserInfo().getUid();
            //获取我的待办信息
            List<MyToDoTaskVO> myToDoTasks = workflowFeign.getMyToDoTasks(userId);
            bomIdList = myToDoTasks.stream().map(MyToDoTaskVO::getBusinessTableId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(bomIdList)) {
                ExcelUtil.export(fileName, "BOM", new ArrayList<>(), BomExportExcelVO.class, response);
                return;
            }
            stateList.add(BomStateEnum.WAIT_AUDIT.getState());
            stateList.add(BomStateEnum.AUDIT_ING.getState());
        }

        List<FindUserDTO> userList = commonService.getAllUser();

        List<BomPagingVO> list = baseMapper.getAllBom(dto, bomIdList, skuIdList, stateList);
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

        ExcelUtil.export(fileName, "BOM", excelList, BomExportExcelVO.class, response);


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
    @Transactional
    public void approvalPass(AuditParamDTO dto) {
        BomInfoEntity bom = this.getById(dto.getId());
        //意见
        String comment = dto.getComment();
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }

        //是不是 第一次审核
        Boolean isFirstAudit = BomStateEnum.WAIT_AUDIT.getState().equals(bom.getState());
        bom.setState(BomStateEnum.AUDIT_ING.getState());
        bom.setRemark(dto.getComment());
        Boolean result = this.updateById(bom);


        String userId = commonService.getUserInfo().getUid();
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
        ProcessNodeDTO node = workflowFeign.taskPass(approveProcess);
        if (node != null) {
            if (result && isFirstAudit) {
                String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.WAIT_AUDIT.getName(), BomStateEnum.AUDIT_ING.getName());
                //操作记录
                bomOperateLogService.saveOperate(bom.getId(), BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
            }
        }


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
    @Transactional
    public void bomProcessPass(ProcessPassDTO dto) {
        String bomId = dto.getBusinessTableId();
        BomInfoEntity bom = this.getById(bomId);
        if (bom != null) {
            bom.setState(BomStateEnum.AUDIT_PASS.getState());
            this.updateById(bom);

            String operateContent = String.format(BomOperateContent.STATE_CHANGE, BomStateEnum.AUDIT_ING.getName(), BomStateEnum.AUDIT_PASS.getName());
            //操作记录
            bomOperateLogService.saveOperate(bom.getId(), BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);

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
    public void changeBom(BomDTO bom) {
        String bomId = bom.getId();
        BomInfoEntity bomEntity = this.getById(bomId);
        if (bomEntity != null) {
            Integer bomVersion = bomEntity.getVersion();
            bomEntity.setVersion(bomVersion + 1);
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
        String userId = commonService.getUserInfo().getUid();
        BusinessTableDTO tableDTO = new BusinessTableDTO();
        tableDTO.setBusinessTableId(bom.getId());
        tableDTO.setUserId(userId);
        //获取到用户该业务表的待办任务
        MyToDoTaskVO processTask = workflowFeign.getByBusinessTableId(tableDTO);
        if (Objects.isNull(processTask)) {
            throw new ServiceException(ApiError.ERROR_94005);
        }

        //流程需要关闭吗
        Boolean result = this.updateById(bom);

        ApproveProcessDTO process = new ApproveProcessDTO();
        process.setComment(dto.getComment());
        process.setProcessInstanceId(processTask.getProcessInstanceId());
        process.setUserId(userId);
        process.setTaskId(processTask.getTaskId());

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("agree", false);
        process.setParameterMap(parameterMap);
        //
        workflowFeign.taskNoPass(process);

        if (result) {
            String statusName = BomStateEnum.AUDIT_ING.getName();
            if (isFirstAudit) {
                statusName = BomStateEnum.WAIT_AUDIT.getName();
            }
            String operateContent = String.format(BomOperateContent.STATE_CHANGE, statusName, BomStateEnum.AUDIT_NO_PASS.getName());
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
     * 获取到最大的编号
     *
     * @param
     * @return java.lang.Integer
     * @author yl
     * @date 2023-01-10 14:58
     */
    @Override
    public Integer getMaxSequence() {
        return baseMapper.getMaxSequence();
    }
}
