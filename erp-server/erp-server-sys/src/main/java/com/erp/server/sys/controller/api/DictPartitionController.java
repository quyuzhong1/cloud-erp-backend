package com.erp.server.sys.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.plm.vo.SkuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.DictPartitionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.DictPartitionDTO;

import java.util.List;

/**
 * 分区表
 *
 * @author lrp
 * @since 2025-01-03
 */
@Slf4j
@RestController
@LogSystemModule("分区表")
@RequestMapping("/dictPartition")
public class DictPartitionController extends BaseController {

    @Resource
    private DictPartitionService dictPartitionService;

    /**
    * 新增
    * @author lrp
    * @date:  2025-01-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "分区表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DictPartitionDTO.AddDTO dto) {
        return success(dictPartitionService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2025-01-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "分区表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:dictPartition:update",
        serviceClass = DictPartitionService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DictPartitionDTO.UpdateDTO dto) {
        dictPartitionService.update(dto);
        return success();
    }

    /**
     * 获取分区下拉框-分页查询
     *
     * @return ApiResult<List < DictPartitionDTO.DictDTO>>
     * @author zdy
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<DictPartitionDTO.DictDTO>> pagingSelect(@RequestBody @Validated PagingDTO<DictPartitionDTO.SelectDTO> dto) {
        PagingVO<DictPartitionDTO.DictDTO> list = dictPartitionService.pagingSelect(dto);
        return success(list);
    }
    /**
     * 获取分区下拉框-全量
     *
     * @return ApiResult<List < DictPartitionDTO.DictDTO>>
     * @author hyj
     */
    @PostMapping("/drop/down")
    public ApiResult<List<DictPartitionDTO.DictDTO>> dropDown(@RequestBody @Validated DictPartitionDTO.SelectDTO dto) {
        List<DictPartitionDTO.DictDTO> list = dictPartitionService.dropDown(dto);
        return success(list);
    }

}
