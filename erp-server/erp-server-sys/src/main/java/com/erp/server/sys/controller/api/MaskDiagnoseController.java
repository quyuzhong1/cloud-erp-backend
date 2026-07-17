package com.erp.server.sys.controller.api;

import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.MaskDiagnoseDTO;
import com.erp.server.sys.service.MaskDiagnoseService;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏诊断 控制器（运维 / SRE 在线排查）
 *
 * <p>三个只读接口：</p>
 * <ul>
 *   <li>{@code POST /maskDiagnose/regex}：测试一条正则的 ReDoS 安全性 + 实际匹配耗时</li>
 *   <li>{@code POST /maskDiagnose/descriptor}：查看一个 POJO 类被脱敏框架解析后的字段描述符</li>
 *   <li>{@code POST /maskDiagnose/stats}：当前 MaskClassDescriptorRegistry 缓存统计</li>
 * </ul>
 *
 * <p><b>使用场景</b>：</p>
 * <ul>
 *   <li>线上某接口慢，怀疑被脱敏切面拖累 → 调 stats 看缓存是否健康、调 descriptor 看返回类有几个被脱字段</li>
 *   <li>运维要新增一条 CUSTOM 正则前 → 调 regex 用线上典型样本压测耗时，避免上线后才发现 ReDoS</li>
 *   <li>怀疑某字段没生效 → 调 descriptor 看是否进了字段描述符列表</li>
 * </ul>
 *
 * <p><b>权限</b>：本接口仅暴露给运维菜单（建议挂在 {@code sys_menu.type=2} 按钮权限码下）。
 * 测试 regex 不会让任意正则进入 {@code cfg_mask_field}，安全无副作用；但 stats / descriptor
 * 会暴露已扫描的业务类名，对外部用户屏蔽。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@RestController
@LogSystemModule("脱敏诊断")
@RequestMapping("/maskDiagnose")
class MaskDiagnoseController extends BaseController {

    @Resource
    private MaskDiagnoseService maskDiagnoseService;

    @PostMapping("/regex")
    public ApiResult<MaskDiagnoseDTO.RegexTestVO> testRegex(
            @RequestBody @Validated MaskDiagnoseDTO.RegexTestDTO dto) {
        return success(maskDiagnoseService.testRegex(dto));
    }

    @PostMapping("/descriptor")
    public ApiResult<MaskDiagnoseDTO.DescriptorVO> describe(
            @RequestBody @Validated MaskDiagnoseDTO.DescriptorSearchDTO dto) {
        return success(maskDiagnoseService.describe(dto));
    }

    @PostMapping("/stats")
    public ApiResult<MaskDiagnoseDTO.RegistryStatsVO> stats() {
        return success(maskDiagnoseService.stats());
    }
}
