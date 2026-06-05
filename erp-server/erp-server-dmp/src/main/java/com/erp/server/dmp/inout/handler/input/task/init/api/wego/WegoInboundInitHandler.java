package com.erp.server.dmp.inout.handler.input.task.init.api.wego;

import cn.hutool.core.collection.CollUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * dmp 输入 init 任务基础处理器：WEGO 海外仓入库单 api 拉取实现。
 * <p>
 * 与 {@code JiFengInboundInitHandler} 对齐的链路：
 * <ol>
 *   <li>按 {@link DmpInputInitHandler#dmpInputTaskEntity} 的 {@code nextLevelId} 取对应 {@code overseas_provider} 授权；</li>
 *   <li>调用 WEGO 入库回执查询接口，按 {@code receivingCode} 维度落库后再走 dmp/mongo/finish 链路；</li>
 *   <li>遇到 token 失效时刷新 token 后重试一次。</li>
 * </ol>
 * <p>
 * 当前限制：WEGO 开放接口（{@code com.sdk.wms.wego.service.WegoOpenApiService}）暂未提供"入库单回执查询"
 * 等价能力（仅有 {@code inorder.save} 用于推送入库单）。在 SDK 补充之前，此 Handler 仅完成授权校验，
 * 数据拉取返回空列表，dmp 链路在 INIT 阶段直接结束，不会触发后续 mongo/dmp/mq 处理。
 */
@Slf4j
@Service
@Scope("prototype")
public class WegoInboundInitHandler extends DmpInputInitHandler {

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.WE_GO.getCode())
                .list();
        if (CollUtil.isEmpty(overseasProviderEntityList)) {
            throw new ServiceException("WEGO授权信息不存在");
        }
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if (null == overseasProviderEntity) {
            throw new ServiceException(OmsPlatformEnum.WE_GO.getCode() + "对应授权ID信息不存在,nextId:"
                    + dmpInputTaskEntity.getNextLevelId());
        }

        log.warn("[WEGO入库INIT] 当前 WEGO SDK 暂未提供入库单回执查询接口，跳过拉取。authId={}",
                overseasProviderEntity.getId());
        return Collections.emptyList();
    }
}
