package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.server.wms.mapper.PackageForecastMapper;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.erp.server.wms.service.PackageForecastService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.PackageForecastDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 组包预报表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
@Slf4j
@Service
public class PackageForecastServiceImpl extends SuperServiceImpl<PackageForecastMapper, PackageForecastEntity> implements PackageForecastService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private PackageForecastDetailService  packageForecastDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PackageForecastDTO.AddDTO addDTO) {
        PackageForecastEntity packageForecastEntity = new PackageForecastEntity();
        BeanMapperUtils.copy(addDTO, packageForecastEntity);
        // 数据处理
        handleData(packageForecastEntity);

        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZB);
        packageForecastEntity.setCode(code);
        boolean save = super.save(packageForecastEntity);
        if(!save) {
            throw new ServiceException("组包预报单保存失败");
        }
        String id = packageForecastEntity.getId();
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "组包预报单" , packageForecastEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKAGE_FORECAST.getCode(), id, "新增操作");
        packageForecastDetailService.add(id,addDTO.getDetailList());

        return new BaseResultDTO.AddDTO(packageForecastEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PackageForecastDTO.UpdateDTO updateDTO) {
        PackageForecastEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单"));
        PackageForecastEntity packageForecastEntity =  BeanMapperUtils.map(PackageForecastEntity.class, updateDTO);

        // 数据处理
        handleData(packageForecastEntity);
        log.info("编辑 开始修改组包预报单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(packageForecastEntity);
        if(!save) {
            throw new ServiceException("组包预报单保存失败");
        }

        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), packageForecastEntity.getCode(), "组包预报单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, packageForecastEntity, null, packageForecastEntity.getId(), msg);
        return Boolean.TRUE;
    }




    /**
    * 新增修改处理数据
    */
    private void handleData(PackageForecastEntity packageForecastEntity) {
    }
}
