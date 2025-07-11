package com.erp.server.oms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CfgInvoiceInvalidEntity;
import com.erp.model.oms.entity.CfgInvoiceSettingEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.mapper.CfgInvoiceInvalidMapper;
import com.erp.server.oms.sdk.invoice.NfeInvoiceService;
import com.erp.server.oms.service.CfgInvoiceInvalidService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.CfgInvoiceSettingService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.sdk.third.tf.dto.NfeInvoiceDTO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.CfgInvoiceInvalidDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_INVOICE_INFO;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_INVOICE_INVALID;

/**
 * <p>
 * 作废发票号 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-04-14
 */
@Slf4j
@Service
public class CfgInvoiceInvalidServiceImpl extends SuperServiceImpl<CfgInvoiceInvalidMapper, CfgInvoiceInvalidEntity> implements CfgInvoiceInvalidService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private CfgInvoiceSettingService cfgInvoiceSettingService;
    @Resource
    @Lazy
    private NfeInvoiceService nfeInvoiceService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgInvoiceInvalidDTO.AddDTO addDTO) {
        CfgInvoiceSettingEntity settingEntity = cfgInvoiceSettingService.getById(addDTO.getCfgInvoiceSettingId());
        if(Objects.isNull(settingEntity) || CharSequenceUtil.isBlank(settingEntity.getToken())) {
            throw new ServiceException("发票授权信息不存在");
        }
        NfeInvoiceDTO.NfeVoidedDTO nfeVoidedDTO = new NfeInvoiceDTO.NfeVoidedDTO();
        nfeVoidedDTO.setTokenEmpresa(settingEntity.getToken());
        nfeVoidedDTO.setJustificativa(addDTO.getReason());
        nfeVoidedDTO.setNumeroFinal(addDTO.getEndInvoiceNo());
        nfeVoidedDTO.setNumeroInicial(addDTO.getStartInvoiceNo());
        nfeVoidedDTO.setSerie(addDTO.getNo());
        //调用第三方接口
        nfeInvoiceService.voidedInvoice(nfeVoidedDTO);

        CfgInvoiceInvalidEntity cfgInvoiceInvalidEntity = new CfgInvoiceInvalidEntity();
        BeanMapperUtils.copy(addDTO, cfgInvoiceInvalidEntity);


        log.info("开始新增作废发票号");
        boolean save = super.save(cfgInvoiceInvalidEntity);
        if(!save) {
            throw new ServiceException("作废发票号保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "作废发票号" , cfgInvoiceInvalidEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INVOICE_INVALID.getCode(), cfgInvoiceInvalidEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(cfgInvoiceInvalidEntity.getId(), cfgInvoiceInvalidEntity.getId());
    }

    @Override
    public PagingVO<CfgInvoiceInvalidDTO.PagingViewDTO> paging(PagingDTO<CfgInvoiceInvalidDTO.PagingParamDTO> dto) {
        CfgInvoiceInvalidDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<CfgInvoiceSettingDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        //分页数据
        IPage<CfgInvoiceInvalidDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }

    @Override
    public Boolean export(CustomerDTO.@Valid ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("作废发票号", EXPORT_INVOICE_INVALID.getCode(), dto);
        return null;
    }

    @Override
    public List<CfgInvoiceInvalidDTO.DropDownDTO> getCompanyName() {
        return cfgInvoiceSettingService.getCompanyName();
    }

}
