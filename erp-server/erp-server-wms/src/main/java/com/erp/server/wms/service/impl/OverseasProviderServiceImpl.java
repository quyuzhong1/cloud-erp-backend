package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.mapper.OverseasProviderMapper;
import com.erp.server.wms.service.OverseasProviderService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import com.erp.server.wms.service.ThirdWarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.OverseasProviderDTO;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 海外物流商 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasProviderServiceImpl extends SuperServiceImpl<OverseasProviderMapper, OverseasProviderEntity> implements OverseasProviderService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasProviderDTO.AddDTO addDTO) {
        OverseasProviderEntity overseasProviderEntity = new OverseasProviderEntity();
        BeanMapperUtils.copy(addDTO, overseasProviderEntity);

        // 数据处理
        handleData(overseasProviderEntity);

        log.info("开始新增海外物流商");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        overseasProviderEntity.setCode(code);
        boolean save = super.save(overseasProviderEntity);
        if(!save) {
            throw new ServiceException("海外物流商保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "海外物流商" , overseasProviderEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, overseasProviderEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(overseasProviderEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasProviderDTO.UpdateDTO updateDTO) {
        OverseasProviderEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "海外物流商"));
        OverseasProviderEntity overseasProviderEntity =  BeanMapperUtils.map(OverseasProviderEntity.class, updateDTO);

        // 数据处理
        handleData(overseasProviderEntity);
        log.info("编辑 开始修改海外物流商数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(overseasProviderEntity);
        if(!save) {
            throw new ServiceException("海外物流商保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录海外物流商日志数据，单号：【{}】", overseasProviderEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasProviderEntity.getCode(), "海外物流商");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, overseasProviderEntity, null, overseasProviderEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(OverseasProviderEntity overseasProviderEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public PagingVO<OverseasProviderDTO.ListDTO> paging(PagingDTO<OverseasProviderDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public OverseasProviderDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean authorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        ThirdWarehouseService thirdWarehouseService = thirdWarehouseRegistry.getHandler(getPlatFormCodeById(dto.getId()));
        boolean result = thirdWarehouseService.authorize(dto);
        if(result){
            OverseasProviderDTO.UpdateDTO updateDTO = new OverseasProviderDTO.UpdateDTO();
            updateDTO.setId(dto.getId());
            updateDTO.setAuthTime(LocalDateTime.now());
            updateDTO.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
            updateDTO.setAuthJson(dto.getAuthJson().toString());
            this.update(updateDTO);
        }
        return result;
    }

    @Override
    public Boolean cancelAuthorize(List<String> ids) {
        return null;
    }

    public String getPlatFormCodeById(String id){
        OverseasProviderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到物流商信息"));
        return entity.getCode();
    }
}
