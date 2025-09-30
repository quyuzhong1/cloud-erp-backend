package com.erp.server.srm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.srm.dto.PayableDetailDTO;
import com.erp.model.srm.entity.PayableDetailEntity;
import com.erp.server.srm.mapper.PayableDetailMapper;
import com.erp.server.srm.service.PayableDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2025-09-24
 */
@Slf4j
@Service
public class PayableDetailServiceImpl extends SuperServiceImpl<PayableDetailMapper, PayableDetailEntity> implements PayableDetailService {


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO batchAdd(List<PayableDetailDTO.AddDTO> detailList,String mainId) {
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"应付单明细");
        }
        List<PayableDetailEntity> list = BeanUtil.copyToList(detailList, PayableDetailEntity.class);

        log.info("开始新增");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        return new BaseResultDTO.AddDTO(mainId, mainId);
    }

    @Override
    public List<PayableDetailEntity> listMainIdList(List<String> mainIdList) {
        if(CollUtil.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(PayableDetailEntity::getMainId,mainIdList).list();
    }

    @Override
    public Boolean removeByMainId(String mainId) {
        return lambdaUpdate().eq(PayableDetailEntity::getMainId,mainId).remove();
    }

    @Override
    public void updateKingdeeDetailId(JSONArray list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Object obj : list) {
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            String detailId = (String) jsonObject.get("detailId");
            String kingdeeDetailId = (String) jsonObject.get("kingdeeDetailId");
            this.lambdaUpdate()
                    .set(PayableDetailEntity::getThirdPayableDetailId, kingdeeDetailId)
                    .eq(PayableDetailEntity::getId, detailId)
                    .update();
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PayableDetailEntity payableDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
