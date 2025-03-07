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
import com.erp.model.oms.dto.CfgVatInvoiceDTO;
import com.erp.model.oms.entity.InvoiceUploadEntity;
import com.erp.model.oms.enums.InvoiceUploadInvoiceTypeEnum;
import com.erp.model.oms.enums.InvoiceUploadStatusEnum;
import com.erp.model.oms.enums.InvoiceUploadTemplateTypeEnum;
import com.erp.model.oms.enums.InvoiceUploadUploadStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.mapper.InvoiceUploadMapper;
import com.erp.server.oms.service.InvoiceUploadDetailService;
import com.erp.server.oms.service.InvoiceUploadService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.InvoiceUploadDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

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
public class InvoiceUploadServiceImpl extends SuperServiceImpl<InvoiceUploadMapper, InvoiceUploadEntity> implements InvoiceUploadService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private InvoiceUploadDetailService invoiceUploadDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InvoiceUploadDTO.AddDTO addDTO) {
        InvoiceUploadEntity invoiceUploadEntity = new InvoiceUploadEntity();
        BeanMapperUtils.copy(addDTO, invoiceUploadEntity);

        // 数据处理
        handleData(invoiceUploadEntity);

        log.info("开始新增上传记录");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_INV);
        invoiceUploadEntity.setCode(code);
        boolean save = super.save(invoiceUploadEntity);
        if(!save) {
            throw new ServiceException("上传记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "上传记录" , invoiceUploadEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INVOICE_UPLOAD.getCode(), invoiceUploadEntity.getId(), "新增操作");
        // 新增明细（如果有明细的话）
        invoiceUploadDetailService.batchAdd(invoiceUploadEntity.getId(), addDTO.getDetailList());
        return new BaseResultDTO.AddDTO(invoiceUploadEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InvoiceUploadDTO.UpdateDTO addOrUpdateDTO) {
        InvoiceUploadEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "上传记录"));
        InvoiceUploadEntity invoiceUploadEntity =  BeanMapperUtils.map(InvoiceUploadEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(invoiceUploadEntity);
        log.info("编辑 开始修改上传记录数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(invoiceUploadEntity);
        if(!save) {
            throw new ServiceException("上传记录保存失败");
        }
        // 修改明细数据（包含增删改）（如果有明细的话）
        invoiceUploadDetailService.batchUpdate(invoiceUploadEntity.getId(), addOrUpdateDTO.getDetailList());
        // 记录主单操作日志
            log.info("编辑 开始记录上传记录日志数据，单号：【{}】", invoiceUploadEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), invoiceUploadEntity.getCode(), "上传记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, invoiceUploadEntity, null, invoiceUploadEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<InvoiceUploadDTO.PagingViewDTO> paging(PagingDTO<InvoiceUploadDTO.PagingParamDTO> dto) {
        InvoiceUploadDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<InvoiceUploadDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<InvoiceUploadDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<InvoiceUploadDTO.PagingViewDTO> records) {
        if (CollUtil.isEmpty(records)){
            return;
        }
        List<String> skuIds = records.stream().map(InvoiceUploadDTO.PagingViewDTO::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        records.forEach(pagingViewDTO -> {
            SkuVO skuVO = skuVOS.stream().filter(e -> CharSequenceUtil.isNotBlank(pagingViewDTO.getSkuId()) && pagingViewDTO.getSkuId().equals(e.getSkuId())).findFirst().orElse(null);
            pagingViewDTO.setProductName(Objects.nonNull(skuVO) ? skuVO.getSkuName() : CharSequenceUtil.EMPTY);
            pagingViewDTO.setInvoiceTypeName(InvoiceUploadInvoiceTypeEnum.getName(pagingViewDTO.getCode()));
            pagingViewDTO.setTemplateTypeName(InvoiceUploadTemplateTypeEnum.getName(pagingViewDTO.getTemplateType()));
            pagingViewDTO.setStatusName(InvoiceUploadStatusEnum.getName(pagingViewDTO.getStatus()));
            pagingViewDTO.setUploadStatusName(InvoiceUploadUploadStatusEnum.getName(pagingViewDTO.getUploadStatus()));
        });
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(InvoiceUploadEntity invoiceUploadEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
