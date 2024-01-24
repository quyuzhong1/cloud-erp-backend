package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BooleanEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.enums.ConfirmStatusEnum;
import com.erp.model.srm.enums.PoReconciliationDetailEnum;
import com.erp.model.srm.enums.PoReconciliationEnum;
import com.erp.model.sys.enums.SysDictBasicEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.ScmDictFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.srm.mapper.PoReconciliationDetailMapper;
import com.erp.server.srm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购对账单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@Service
public class PoReconciliationDetailScmServiceImpl extends SuperServiceImpl<PoReconciliationDetailMapper, PoReconciliationDetailEntity> implements PoReconciliationDetailScmService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private PoReconciliationScmService poReconciliationScmService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private ScmDictFeign scmDictFeign;

    @Autowired
    private SysDictFeign sysDictFeign;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PoReconciliationDetailDTO.AddDTO addDTO) {
        PoReconciliationDetailEntity poReconciliationDetailEntity = new PoReconciliationDetailEntity();
        BeanMapperUtils.copy(addDTO, poReconciliationDetailEntity);


        log.info("开始新增采购对账单明细");
        boolean save = super.save(poReconciliationDetailEntity);
        if(!save) {
            throw new ServiceException("采购对账单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "采购对账单明细" , poReconciliationDetailEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PO_RECONCILIATION.getCode(), poReconciliationDetailEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(poReconciliationDetailEntity.getId(), poReconciliationDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<PoReconciliationDetailDTO.ScmUpdateDTO> detailList,String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.TRUE;
        }
        List<PoReconciliationDetailEntity> list =  BeanMapperUtils.copyList(PoReconciliationDetailEntity.class, detailList);

        handleUpdateData (list,mainId);

        log.info("编辑 开始修改采购对账单数据，id：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("采购对账单明细保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generatePoReconciliation(PoReconciliationDetailDTO.GeneratePoReconciliationDTO dto) {

        List<PoReconciliationDetailEntity> poReconciliationDetailList = this.listByIds(dto.getDetailIdList());
        if (CollectionUtils.isEmpty(poReconciliationDetailList)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
        }
        String confirmSourceCodes = poReconciliationDetailList.stream().filter(obj -> !StrUtil.equals(obj.getBusinessStatus(), ConfirmStatusEnum.CONFIRM.getCode()))
                .map(PoReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(confirmSourceCodes)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_GENERATE,confirmSourceCodes);
        }

        String generateSourceCodes = poReconciliationDetailList.stream().filter(obj -> StrUtil.isNotBlank(obj.getMainId()))
                .map(PoReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(generateSourceCodes)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_HAS_GENERATE,generateSourceCodes);
        }
        if (PoReconciliationEnum.GenerateTypeEnum.CREATE_NEW.getCode().equals(dto.getGenerateType())) {
            //新生成对账单
            addNewPoReconciliation(dto,poReconciliationDetailList);
        } else {
            //选择已有对账单
            updateOldPoReconciliation(dto,poReconciliationDetailList);
        }
        return this.updateBatchById(poReconciliationDetailList);
    }


    @Override
    public PagingVO<PoReconciliationDetailDTO.ListDTO> paging(PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<PoReconciliationDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(PoReconciliationDetailDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<PoReconciliationDetailDTO.ListDTO> list = this.baseMapper.listExport(dto);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/poReconciliationDetail.xlsx";
        String name = "对账明细导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public List<PoReconciliationDetailDTO.ViewDTO> viewDetail(PoReconciliationDetailDTO.PagingParamDTO dto) {
        List<PoReconciliationDetailDTO.ListDTO> list = this.baseMapper.listDetail(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        //数据处理
        fillList(list);
        List<PoReconciliationDetailDTO.ViewDTO> resultList = BeanMapperUtils.copyList(PoReconciliationDetailDTO.ViewDTO.class, list);
        return resultList;
    }


    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<PoReconciliationDetailDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //产品信息
        List<String> skuIdList = list.stream().map(PoReconciliationDetailDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //结算方式
        List<DictBasicDTO> settleDictList = scmDictFeign.listDictByKey(DictBasicEnum.SUPPLIER_PAY_MODE.getType());

        //付款条件
        List<com.erp.model.sys.dto.DictBasicDTO.ViewDTO> paymentConditionList =  sysDictFeign.getByType(SysDictBasicEnum.PAYMENT_CONDITION.getCode());

        for (PoReconciliationDetailDTO.ListDTO listDTO : list) {
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            listDTO.setBusinessStatusName(ConfirmStatusEnum.getNameByCode(listDTO.getBusinessStatus()));
            listDTO.setTaxRateStr(StrUtil.format("{}%",listDTO.getTaxRate().stripTrailingZeros().toPlainString()));
            listDTO.setIsAddAccountStr(listDTO.getIsAddAccount() ? BooleanEnum.TRUE.getName() : BooleanEnum.FALSE.getName());
            //产品名称
            String productName = skuList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), listDTO.getSkuId())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            listDTO.setProductName(productName);

            //结算方式
            String settleDictName = settleDictList.stream().filter(obj -> StrUtil.equals(obj.getValue(), listDTO.getSettleDict())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setSettleDictName(settleDictName);

            //付款条件名称
            String paymentConditionName = paymentConditionList.stream().filter(obj -> StrUtil.equals(obj.getValue(), listDTO.getPaymentCondition())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setPaymentConditionName(paymentConditionName);
        }
    }

    /**
     * 新生成对账单
     */
    private void addNewPoReconciliation (PoReconciliationDetailDTO.GeneratePoReconciliationDTO dto,List<PoReconciliationDetailEntity> poReconciliationDetailList) {
        if (CollectionUtils.isEmpty(dto.getReconciliationDateList()) || dto.getReconciliationDateList().size() == 1) {
            throw new ServiceException("新生成对账单对账周期不能为空");
        }
        log.info("新生成对账单>>>>>>>> detailIdList = {}",dto.getDetailIdList());
        Map<String, List<PoReconciliationDetailEntity>> map = poReconciliationDetailList.stream().collect(Collectors.groupingBy(obj -> obj.getSupplierId().concat(obj.getSettleOrgId())));
        for (Map.Entry<String, List<PoReconciliationDetailEntity>> entry : map.entrySet()) {
            List<PoReconciliationDetailEntity> value = entry.getValue();
            PoReconciliationDTO.AddDTO addDTO = new PoReconciliationDTO.AddDTO();
            List<String> detailIdList = value.stream().map(PoReconciliationDetailEntity::getId).collect(Collectors.toList());
            addDTO.setDetailIdList(detailIdList);
            addDTO.setStartDate(dto.getReconciliationDateList().get(0));
            addDTO.setEndDate(dto.getReconciliationDateList().get(1));
            BaseResultDTO.AddDTO add = poReconciliationScmService.add(addDTO);
            String id = add.getId();
            //更新对账明细mainId
            value.stream().forEach(obj-> {
                obj.setMainId(id);
                obj.setIsAddAccount(Boolean.TRUE);
            });
        }
    }

    /**
     * 选择已有对账单
     */
    private void updateOldPoReconciliation (PoReconciliationDetailDTO.GeneratePoReconciliationDTO dto,List<PoReconciliationDetailEntity> poReconciliationDetailList) {
        //选择已有对账单
        if (StrUtil.isBlank(dto.getId())) {
            throw new ServiceException("选择已有对账单时对账单数据不能为空");
        }
        PoReconciliationEntity poReconciliationEntity = poReconciliationScmService.getById(dto.getId());
        if (ObjectUtils.isEmpty(poReconciliationEntity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //校验
        long count = poReconciliationDetailList.stream().filter(obj -> !StrUtil.equals(obj.getSupplierId(),poReconciliationEntity.getSupplierId())
                        || !StrUtil.equals(obj.getSettleOrgId(),poReconciliationEntity.getSettleOrgId()))
                .map(PoReconciliationDetailEntity::getSupplierId).distinct().count();
        if (count > MathUtil.ONE) {
            throw new ServiceException("单据单号【{}】与选择对账单供应商或结算组织不一致");
        }
        //更新对账明细
        poReconciliationDetailList.stream().forEach(obj ->{
            obj.setMainId(poReconciliationEntity.getId());
            obj.setIsAddAccount(Boolean.TRUE);
        });
    }

    /**
     * @description: 修改处理
     * @author Will
     * @date: 2024/1/20 16:55
     * @param list
     * @param mainId
     */
    private void handleUpdateData (List<PoReconciliationDetailEntity> list,String mainId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //已存在对应明细
        List<String> detailIdList = list.stream().map(PoReconciliationDetailEntity::getId).collect(Collectors.toList());
        List<PoReconciliationDetailEntity> poReconciliationDetailList = this.listByIds(detailIdList);
        //对账单
        PoReconciliationEntity poReconciliationEntity = poReconciliationScmService.getById(mainId);
        if (ObjectUtils.isEmpty(poReconciliationEntity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        for (PoReconciliationDetailEntity entity : list) {
            //添加日志
            PoReconciliationDetailEntity old = poReconciliationDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), entity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(old)) {
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
            }
            //供应商、结算组织验证
            if (!StrUtil.equals(poReconciliationEntity.getSupplierId(),old.getSupplierId())
                    || !StrUtil.equals(poReconciliationEntity.getSettleOrgId(),old.getSettleOrgId())) {
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_ADD_DETAIL,poReconciliationEntity.getCode(),poReconciliationEntity.getSupplierName(),poReconciliationEntity.getSettleOrgName());
            }

            if (StrUtil.isBlank(old.getMainId())) {
                String content = StrUtil.format("新增了一条SKU【{}】", old.getSkuNo());
                operateLogService.addModuleOperateLog(content, ModuleTypeEnum.PO_RECONCILIATION.getCode(),mainId, "编辑操作");
            } else {
                operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PO_RECONCILIATION.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
            entity.setMainId(mainId);
        }

        //新增不需要添加新增SKU的日志
        List<PoReconciliationDetailEntity> addList = list.stream().filter(obj -> StrUtil.isBlank(obj.getMainId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.PO_RECONCILIATION.getCode(), addPairList, "编辑操作");
        }
    }
}
