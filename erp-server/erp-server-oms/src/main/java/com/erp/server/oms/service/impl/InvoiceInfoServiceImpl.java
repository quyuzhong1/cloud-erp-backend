package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.CfgVatInvoiceEntity;
import com.erp.model.oms.entity.InvoiceInfoEntity;
import com.erp.model.oms.enums.InvoiceInfoInvoiceTypeEnum;
import com.erp.model.oms.enums.InvoiceInfoStatusEnum;
import com.erp.model.oms.enums.InvoiceInfoTemplateTypeEnum;
import com.erp.model.oms.enums.InvoiceInfoUploadStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.mapper.InvoiceInfoMapper;
import com.erp.server.oms.service.CfgVatInvoiceService;
import com.erp.server.oms.service.InvoiceDetailService;
import com.erp.server.oms.service.InvoiceInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.InvoiceInfoDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 上传记录 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
@Slf4j
@Service
public class InvoiceInfoServiceImpl extends SuperServiceImpl<InvoiceInfoMapper, InvoiceInfoEntity> implements InvoiceInfoService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private InvoiceDetailService invoiceDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private CfgVatInvoiceService cfgVatInvoiceService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InvoiceInfoDTO.AddDTO addDTO) {
        InvoiceInfoEntity invoiceInfoEntity = new InvoiceInfoEntity();
        BeanMapperUtils.copy(addDTO, invoiceInfoEntity);

        // 数据处理
        handleData(invoiceInfoEntity);

        log.info("开始新增上传记录");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_INV);
        invoiceInfoEntity.setCode(code);
        boolean save = super.save(invoiceInfoEntity);
        if(!save) {
            throw new ServiceException("上传记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "上传记录" , invoiceInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INVOICE_INFO.getCode(), invoiceInfoEntity.getId(), "新增操作");
        // 新增明细（如果有明细的话）
        invoiceDetailService.batchAdd(invoiceInfoEntity.getId(), addDTO.getDetailList());
        return new BaseResultDTO.AddDTO(invoiceInfoEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InvoiceInfoDTO.UpdateDTO addOrUpdateDTO) {
        InvoiceInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "上传记录"));
        InvoiceInfoEntity invoiceInfoEntity =  BeanMapperUtils.map(InvoiceInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(invoiceInfoEntity);
        log.info("编辑 开始修改上传记录数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(invoiceInfoEntity);
        if(!save) {
            throw new ServiceException("上传记录保存失败");
        }
        // 修改明细数据（包含增删改）（如果有明细的话）
        invoiceDetailService.batchUpdate(invoiceInfoEntity.getId(), addOrUpdateDTO.getDetailList());
        // 记录主单操作日志
            log.info("编辑 开始记录上传记录日志数据，单号：【{}】", invoiceInfoEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), invoiceInfoEntity.getCode(), "上传记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, invoiceInfoEntity, null, invoiceInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<InvoiceInfoDTO.PagingViewDTO> paging(PagingDTO<InvoiceInfoDTO.PagingParamDTO> dto) {
        InvoiceInfoDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<InvoiceInfoDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<InvoiceInfoDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportInvoicePdf(List<String> ids, HttpServletResponse response) {
        List<InvoiceInfoEntity> entityList = this.listByIds(ids);
        List<String> cfgIds = entityList.stream().map(InvoiceInfoEntity::getCfgId).distinct().collect(Collectors.toList());
        List<CfgVatInvoiceEntity> cfgList = cfgVatInvoiceService.listByIds(cfgIds);

    }



    private void fillList(List<InvoiceInfoDTO.PagingViewDTO> records) {
        if (CollUtil.isEmpty(records)){
            return;
        }
        List<String> skuIds = records.stream().map(InvoiceInfoDTO.PagingViewDTO::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        records.forEach(pagingViewDTO -> {
            SkuVO skuVO = skuVOS.stream().filter(e -> CharSequenceUtil.isNotBlank(pagingViewDTO.getSkuId()) && pagingViewDTO.getSkuId().equals(e.getSkuId())).findFirst().orElse(null);
            pagingViewDTO.setProductName(Objects.nonNull(skuVO) ? skuVO.getSkuName() : CharSequenceUtil.EMPTY);
            pagingViewDTO.setInvoiceTypeName(InvoiceInfoInvoiceTypeEnum.getName(pagingViewDTO.getCode()));
            pagingViewDTO.setTemplateTypeName(InvoiceInfoTemplateTypeEnum.getName(pagingViewDTO.getTemplateType()));
            pagingViewDTO.setStatusName(InvoiceInfoStatusEnum.getName(pagingViewDTO.getStatus()));
            pagingViewDTO.setUploadStatusName(InvoiceInfoUploadStatusEnum.getName(pagingViewDTO.getUploadStatus()));
        });
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(InvoiceInfoEntity invoiceInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
