package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.dto.ThirdWarehouseTaskDTO;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OverseasDeliveryPlanDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.mapper.OverseasProviderMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
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
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    @Resource
    private RedisUtil redisUtil;

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
        //修改明细数据
        overseasProviderWarehouseService.update(updateDTO, overseasProviderEntity.getId());
        // 记录主单操作日志
            log.info("编辑 开始记录海外物流商日志数据，单号：【{}】", overseasProviderEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasProviderEntity.getCode(), "海外物流商");
        operateLogService.addModuleOperateLogByObj(old, overseasProviderEntity, ModuleTypeEnum.OVERSEAS_PROVIDER.getCode(), overseasProviderEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(OverseasProviderEntity overseasProviderEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public PagingVO<OverseasProviderDTO.ListDTO> paging(PagingDTO<OverseasProviderDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<OverseasProviderDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());

        return new PagingVO(pageData);
    }

    private void fillList(List<OverseasProviderDTO.ListDTO> list) {
        for (OverseasProviderDTO.ListDTO listDTO : list) {
            listDTO.setAuthStatusName(AuthStatusEnum.getName(listDTO.getAuthStatus()));
        }
    }

    @Override
    public OverseasProviderDTO.ViewDTO view(String id) {
        OverseasProviderEntity entity = this.getById(id);
        OverseasProviderDTO.ViewDTO viewDTO = new OverseasProviderDTO.ViewDTO();
        BeanMapperUtils.copy(viewDTO, entity);
        viewDTO.setAuthStatusName(AuthStatusEnum.getName(viewDTO.getAuthStatus()));
        List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntities = overseasProviderWarehouseService.listByMainIds(Arrays.asList(id));
        List<OverseasProviderWarehouseDTO.ViewDTO> warehouseList = BeanMapper.copyList(overseasProviderWarehouseEntities, OverseasProviderWarehouseDTO.ViewDTO.class);
        viewDTO.setDetailList(warehouseList);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean authorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        ThirdWarehouseService thirdWarehouseService = thirdWarehouseRegistry.getHandler(getPlatFormCodeById(dto.getId()));
        boolean result = thirdWarehouseService.authorize(dto);
        if(result){
            OverseasProviderEntity entity = this.getById(dto.getId());
            OverseasProviderDTO.UpdateDTO updateDTO = new OverseasProviderDTO.UpdateDTO();
            entity.setId(dto.getId());
            entity.setAuthTime(LocalDateTime.now());
            entity.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
            entity.setAuthJson(dto.getAuthJson());
            this.update(updateDTO);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean cancelAuthorize(String id) {
        //清空授权信息
        OverseasProviderDTO.UpdateDTO updateDTO = new OverseasProviderDTO.UpdateDTO();
        updateDTO.setId(id);
        updateDTO.setAuthTime(null);
        updateDTO.setAuthStatus(AuthStatusEnum.CANCEL.getCode());
        this.update(updateDTO);
        //删除数据同步任务
        String platformCode = this.getPlatFormCodeById(id);
        dmpTaskFeign.removePlatformTask(new PlatformTaskDTO.AddDTO(id,null, platformCode));
        return true;
    }

    public String getPlatFormCodeById(String id){
        OverseasProviderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到物流商信息"));
        return entity.getCode();
    }
}
