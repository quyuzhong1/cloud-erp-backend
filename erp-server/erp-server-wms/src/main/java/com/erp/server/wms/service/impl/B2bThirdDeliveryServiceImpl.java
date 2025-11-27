package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.server.wms.convert.B2bThirdDeliveryConverter;
import com.erp.server.wms.service.B2bThirdDeliveryDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.server.wms.mapper.B2bThirdDeliveryMapper;
import com.erp.server.wms.service.B2bThirdDeliveryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * B2B三方发货单 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
 */
@Slf4j
@Service
public class B2bThirdDeliveryServiceImpl extends SuperServiceImpl<B2bThirdDeliveryMapper, B2bThirdDeliveryEntity> implements B2bThirdDeliveryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private SoInfoFeign soInfoFeign;
    @Resource
    private B2bThirdDeliveryDetailService b2bThirdDeliveryDetailService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(B2bThirdDeliveryDTO.AddDTO addDTO) {
        B2bThirdDeliveryEntity b2bThirdDeliveryEntity = new B2bThirdDeliveryEntity();
        BeanMapperUtils.copy(addDTO, b2bThirdDeliveryEntity);

        // 数据处理
        handleData(b2bThirdDeliveryEntity);

        log.info("开始新增B2B三方发货单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        b2bThirdDeliveryEntity.setCode(code);
        boolean save = super.save(b2bThirdDeliveryEntity);
        if(!save) {
            throw new ServiceException("B2B三方发货单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2B三方发货单" , b2bThirdDeliveryEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, b2bThirdDeliveryEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(b2bThirdDeliveryEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(B2bThirdDeliveryDTO.UpdateDTO addOrUpdateDTO) {
        B2bThirdDeliveryEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2B三方发货单"));
        B2bThirdDeliveryEntity b2bThirdDeliveryEntity =  BeanMapperUtils.map(B2bThirdDeliveryEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(b2bThirdDeliveryEntity);
        log.info("编辑 开始修改B2B三方发货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(b2bThirdDeliveryEntity);
        if(!save) {
            throw new ServiceException("B2B三方发货单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录B2B三方发货单日志数据，单号：【{}】", b2bThirdDeliveryEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), b2bThirdDeliveryEntity.getCode(), "B2B三方发货单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, b2bThirdDeliveryEntity, null, b2bThirdDeliveryEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<B2bThirdDeliveryDTO.TabListDTO> tabList() {
        return null;
    }

    @Override
    public PagingVO<B2bThirdDeliveryDTO.PagingViewDTO> paging(PagingDTO<B2bThirdDeliveryDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public void export(B2bThirdDeliveryDTO.PagingParamDTO dto) {

    }

    @Override
    public B2bThirdDeliveryDTO.ViewDTO view(String id, String soId) {
        if (CharSequenceUtil.isBlank(id)){
            return soInfoFeign.getB2bThirdDeliveryView(soId);
        }else {
            B2bThirdDeliveryEntity entity = this.getById(id);
            if (Objects.isNull(entity)){
                throw new ServiceException(ApiError.NOT_EXIST,"b2b三方发货单");
            }
            List<B2bThirdDeliveryDetailEntity> detailEntityList = b2bThirdDeliveryDetailService.listByMainIds(Collections.singletonList(id));
            return B2bThirdDeliveryConverter.INSTANCE.toB2bThirdDeliveryViewDTO(entity, detailEntityList);
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(B2bThirdDeliveryEntity b2bThirdDeliveryEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
