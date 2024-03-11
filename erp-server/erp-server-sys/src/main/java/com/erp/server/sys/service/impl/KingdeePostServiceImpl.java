package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.erp.server.sys.mapper.KingdeePostMapper;
import com.erp.server.sys.service.KingdeePostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.KingdeePostDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 金蝶岗位表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@Service
public class KingdeePostServiceImpl extends SuperServiceImpl<KingdeePostMapper, KingdeePostEntity> implements KingdeePostService {

    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KingdeePostDTO.AddDTO addDTO) {
        KingdeePostEntity kingdeePostEntity = new KingdeePostEntity();
        BeanMapperUtils.copy(addDTO, kingdeePostEntity);

        // 数据处理
        handleData(kingdeePostEntity);

        log.info("开始新增金蝶岗位单");
        // 生成单号
        String code = docNoGenHelper.generateCode(null);
        kingdeePostEntity.setCode(code);
        boolean save = super.save(kingdeePostEntity);
        if(!save) {
            throw new ServiceException("金蝶岗位单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "金蝶岗位单" , kingdeePostEntity.getCode());


        return new BaseResultDTO.AddDTO(kingdeePostEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeePostDTO.UpdateDTO updateDTO) {
        KingdeePostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶岗位单"));
        KingdeePostEntity kingdeePostEntity =  BeanMapperUtils.map(KingdeePostEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeePostEntity);
        log.info("编辑 开始修改金蝶岗位单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kingdeePostEntity);
        if(!save) {
            throw new ServiceException("金蝶岗位单保存失败");
        }

        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KingdeePostEntity kingdeePostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
