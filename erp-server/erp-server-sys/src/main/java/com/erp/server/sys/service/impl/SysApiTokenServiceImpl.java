package com.erp.server.sys.service.impl;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.SysApiTokenDTO;
import com.erp.model.sys.entity.SysApiTokenEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.server.sys.mapper.SysApiTokenMapper;
import com.erp.server.sys.service.SysApiTokenService;
import com.erp.server.sys.service.SysApiTokenWhitelistService;
import com.erp.server.sys.service.SysUserInfoService;
import com.erp.server.sys.support.SysApiTokenCryptoService;
import com.erp.server.sys.support.SysApiTokenSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 个人访问令牌 服务实现类
 * </p>
 */
@Slf4j
@Service
public class SysApiTokenServiceImpl extends SuperServiceImpl<SysApiTokenMapper, SysApiTokenEntity> implements SysApiTokenService {

    private static final Integer USER_ENABLED = 1;

    /**
     * 前端有效期下拉值：0 表示永不过期，其余值表示从创建时间起延长的天数。
     */
    private static final int VALIDITY_NEVER_EXPIRES = 0;

    @Resource
    private SysApiTokenCryptoService sysApiTokenCryptoService;

    @Resource
    private SysApiTokenWhitelistService sysApiTokenWhitelistService;

    @Resource
    private SysUserInfoService sysUserInfoService;

    @Override
    public List<SysApiTokenDTO.ListDTO> listCurrentUserToken() {
        String userId = currentUserId();
        return this.lambdaQuery()
                .eq(SysApiTokenEntity::getUserId, userId)
                .orderByDesc(SysApiTokenEntity::getCreateTime)
                .list()
                .stream()
                .map(this::toListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysApiTokenDTO.TokenDTO add(SysApiTokenDTO.AddDTO dto) {
        String userId = currentUserId();
        String tokenName = normalizeTokenName(dto.getTokenName());
        LocalDateTime expiresTime = calculateExpiresTime(dto.getValidityDays());
        String token = SysApiTokenSupport.generateToken();

        SysApiTokenEntity entity = new SysApiTokenEntity();
        entity.setUserId(userId);
        entity.setTokenName(tokenName);
        entity.setTokenHash(SysApiTokenSupport.sha256Hex(token));
        entity.setEncryptedToken(sysApiTokenCryptoService.encrypt(token));
        entity.setTokenPreviewPrefix(SysApiTokenSupport.tokenPreviewPrefix(token));
        entity.setTokenPreviewSuffix(SysApiTokenSupport.tokenPreviewSuffix(token));
        entity.setExpiresTime(expiresTime);
        this.save(entity);

        return new SysApiTokenDTO.TokenDTO(token, expiresTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SysApiTokenDTO.UpdateDTO dto) {
        SysApiTokenEntity entity = getOwnedToken(dto.getId());
        entity.setTokenName(normalizeTokenName(dto.getTokenName()));
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeToken(BaseIdDTO dto) {
        SysApiTokenEntity entity = getOwnedToken(dto.getId());
        return this.removeById(entity.getId());
    }

    @Override
    public SysApiTokenDTO.TokenDTO copy(String id) {
        SysApiTokenEntity entity = getOwnedToken(id);
        if (isExpired(entity.getExpiresTime())) {
            throw new ServiceException("个人访问令牌已过期，请重新生成");
        }
        return new SysApiTokenDTO.TokenDTO(sysApiTokenCryptoService.decrypt(entity.getEncryptedToken()), entity.getExpiresTime());
    }

    @Override
    public SysApiTokenDTO.ValidateRespDTO validate(SysApiTokenDTO.ValidateReqDTO dto) {
        SysApiTokenDTO.ValidateRespDTO resp = new SysApiTokenDTO.ValidateRespDTO();
        if (dto == null || StringUtils.isBlank(dto.getTokenHash()) || StringUtils.isBlank(dto.getRequestPath())) {
            return resp;
        }

        SysApiTokenEntity tokenEntity = this.lambdaQuery()
                .eq(SysApiTokenEntity::getTokenHash, dto.getTokenHash())
                .one();
        if (tokenEntity == null) {
            return resp;
        }
        resp.setTokenId(tokenEntity.getId());
        resp.setExpiresTime(tokenEntity.getExpiresTime());
        if (isExpired(tokenEntity.getExpiresTime())) {
            return resp;
        }

        SysUserInfoEntity user = sysUserInfoService.getById(tokenEntity.getUserId());
        if (user == null || !USER_ENABLED.equals(user.getUserState())) {
            return resp;
        }

        resp.setTokenValid(true);
        resp.setPathAllowed(sysApiTokenWhitelistService.match(dto.getRequestPath()));
        if (!Boolean.TRUE.equals(resp.getPathAllowed())) {
            return resp;
        }

        resp.setUserId(user.getUid());
        resp.setUserName(user.getUserName());
        resp.setRealName(user.getRealName());
        resp.setMobile(user.getMobile());
        resp.setUserAccount(user.getUserAccount());
        resp.setSuperAdmin(user.getIsSuper());
        return resp;
    }

    private SysApiTokenDTO.ListDTO toListDTO(SysApiTokenEntity entity) {
        SysApiTokenDTO.ListDTO dto = new SysApiTokenDTO.ListDTO();
        dto.setId(entity.getId());
        dto.setTokenName(entity.getTokenName());
        dto.setMaskedToken(SysApiTokenSupport.maskToken(entity.getTokenPreviewPrefix(), entity.getTokenPreviewSuffix()));
        dto.setCreateTime(entity.getCreateTime());
        dto.setExpiresTime(entity.getExpiresTime());
        return dto;
    }

    private SysApiTokenEntity getOwnedToken(String id) {
        if (StringUtils.isBlank(id)) {
            throw new ServiceException("id不能为空");
        }
        String userId = currentUserId();
        SysApiTokenEntity entity = this.lambdaQuery()
                .eq(SysApiTokenEntity::getId, id)
                .eq(SysApiTokenEntity::getUserId, userId)
                .one();
        if (entity == null) {
            throw new ServiceException("个人访问令牌不存在");
        }
        return entity;
    }

    private String currentUserId() {
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser == null || StringUtils.isBlank(loginUser.getUid())) {
            throw new ServiceException("用户未登录");
        }
        return loginUser.getUid();
    }

    private String normalizeTokenName(String tokenName) {
        String value = StringUtils.trimToEmpty(tokenName);
        if (StringUtils.isBlank(value)) {
            throw new ServiceException("令牌名称不能为空");
        }
        if (value.length() > 50) {
            throw new ServiceException("令牌名称最大长度不能超过50位");
        }
        return value;
    }

    private LocalDateTime calculateExpiresTime(Integer validityDays) {
        if (validityDays == null) {
            throw new ServiceException("有效期不能为空");
        }
        if (validityDays == VALIDITY_NEVER_EXPIRES) {
            return null;
        }
        if (validityDays == 30 || validityDays == 90 || validityDays == 180 || validityDays == 365) {
            return LocalDateTime.now().plusDays(validityDays);
        }
        throw new ServiceException("有效期只能选择30天、90天、180天、365天或永不过期");
    }

    private boolean isExpired(LocalDateTime expiresTime) {
        return expiresTime != null && !expiresTime.isAfter(LocalDateTime.now());
    }
}
