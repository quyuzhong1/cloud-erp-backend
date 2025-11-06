package com.erp.server.dmp.controller.api;


import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DictBasicDTO;
import com.erp.model.dmp.dto.ThirdPlatformDTO;
import com.erp.model.dmp.dto.ThirdShopDTO;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.sys.dto.DictBasicAllDTO;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.dmp.service.DictBasicService;
import com.erp.server.dmp.service.impl.ThirdPlatformStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 第三方系统
 *
 */
@Slf4j
@RestController
@LogSystemModule("第三方系统平台")
@RequestMapping("/thirdPlatform")
public class ThirdPlatformController extends BaseController {

    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private SysDictFeign sysDictFeign;



    /**
     * 列表查询
     *
     * @param dto
     * @return ApiResult<PagingVO < WarehouseLocationMoveDTO.ListDTO>>
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<ThirdPlatformDTO.PageSelectDTO>> pagingSelect(@RequestBody @Validated PagingDTO<ThirdShopDTO.SelectDTO> dto) {
        return success(convertPagingSelect(dto));
    }

    private PagingVO<ThirdPlatformDTO.PageSelectDTO> convertPagingSelect(PagingDTO<ThirdShopDTO.SelectDTO> dto) {
        DictBasicEntity dictBasicEntity = dictBasicService.lambdaQuery()
                .eq(DictBasicEntity::getType, "thirdPlatformSysType")
                .eq(DictBasicEntity::getValue, dto.getParams().getSysType())
                .last("Limit 1")
                .one();
        if (Objects.isNull(dictBasicEntity)) {
            throw new ServiceException("系统类型字典信息不存在");
        }
        // 平台系统信息
        Map<String, DictBasicDTO.ViewDTO> dictDasicMap = new HashMap<>();
        String database = StringUtils.isBlank(dictBasicEntity.getRemark()) ? "dmp" : dictBasicEntity.getRemark();
        // 兼容跨库查询
        PagingDTO<DictBasicAllDTO.PagingParamDTO> dtoPagingDTO = new PagingDTO<>();

        List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();

//        AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(queryField,queryConditionEnum,value, QueryDataTypeEnum.STRING);
//        advanceQueryDTOList.add();
//        PagingVO<DictBasicAllDTO.ViewDTO> paging = sysDictFeign.paging(dtoPagingDTO);
        return null;
    }


}
