package com.erp.server.sys.controller.pda;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.PdaVersionDTO;
import com.erp.model.sys.entity.PdaVersionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.sys.service.PdaVersionService;
import com.common.core.controller.vo.ApiResult;


/**
 * 系统版本控制
 * @author Luo_WG
 * @since 2023-08-14
 */
@RestController
@LogSystemModule("PDA系统版本控制")
@RequestMapping("/pdaVersion")
public class PdaVersionController extends BaseController {

    @Autowired
    private PdaVersionService pdaVersionService;

    /**
     * 发版信息列表分页查询
     * @Author Luo_WG
     * @Date 2023/9/11 16:01
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.sys.dto.PdaVersionDTO.PagingDTO>>
     **/
    @PostMapping("/paging")
    public ApiResult<PagingVO<PdaVersionDTO.PagingDTO>> paging(@RequestBody @Validated PagingDTO<PdaVersionDTO.PagingParamDTO> dto) {
        PagingVO<PdaVersionDTO.PagingDTO> pagingVO = pdaVersionService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 获取pda最新版本
     * @Author Luo_WG
     * @Date 2023/8/14 16:27
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.sys.entity.PdaVersionEntity>
     **/
    @GetMapping(value = "/getPdaVersion")
    public ApiResult<PdaVersionEntity> getPdaVersion() {
        PdaVersionEntity version = pdaVersionService.getPdaVersion();
        return success(version);
    }

    /**
     * 发版
     * @Author Luo_WG
     * @Date 2023/8/14 16:27
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.sys.entity.PdaVersionEntity>
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "发版")
    @PostMapping(value = "/release")
    public ApiResult release(@RequestBody PdaVersionDTO.AddDTO dto){
        Boolean flag = pdaVersionService.release(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 用户跳过此版本升级
     * @Author Luo_WG
     * @Date 2023/9/12 12:15
     * @param versionId
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "用户跳过此版本升级:版本id={versionId}")
    @GetMapping(value = "/skipVersion")
    public ApiResult skipVersion(@RequestParam("versionId") String versionId) {
        Boolean flag = pdaVersionService.skipVersion(versionId);
        return flag == true ? success() : failure();
    }
}
