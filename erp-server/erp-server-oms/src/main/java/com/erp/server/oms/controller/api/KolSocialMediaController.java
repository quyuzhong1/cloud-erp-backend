package com.erp.server.oms.controller.api;


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
import com.common.business.vo.PagingVO;
import com.common.business.annotation.WebAdvanceQuery;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.KolSocialMediaService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.KolSocialMediaDTO;
import com.erp.model.oms.entity.KolSocialMediaEntity;
import cn.hutool.core.util.ObjectUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 达人社媒数据表
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@RestController
@LogSystemModule("达人社媒数据表")
@RequestMapping("/kolSocialMedia")
public class KolSocialMediaController extends BaseController {

    @Resource
    private KolSocialMediaService kolSocialMediaService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "达人社媒数据表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolSocialMediaDTO.AddDTO dto) {
        return success(kolSocialMediaService.add(dto));
    }

    /**
    * 批量新增
    * @author wuhaotian
    * @date:  2025-12-03
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "达人社媒数据表批量新增")
    public ApiResult<?> batchAdd(@RequestBody @Validated KolSocialMediaDTO.BatchAddDTO dto) {
        List<KolSocialMediaDTO.AddDTO> list = dto.getList();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());

        for (KolSocialMediaDTO.AddDTO addDTO : list) {
            BatchResultDTO addResult;
            try {
                BaseResultDTO.AddDTO result = kolSocialMediaService.add(addDTO);
                addResult = BatchResultDTO.success(result.getId(), result.getCode(), "新增成功");
            } catch (Exception e) {
                log.error("达人社媒数据表批量新增失败", e);
                addResult = BatchResultDTO.fail("", "", e.getMessage());
            }
            resultDTOS.add(addResult);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "达人社媒数据表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolSocialMedia:update",
        serviceClass = KolSocialMediaService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolSocialMediaDTO.UpdateDTO dto) {
        kolSocialMediaService.update(dto);
        return success();
    }

    /**
    * 批量修改
    * @author wuhaotian
    * @date:  2025-12-03
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/batchUpdate")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "达人社媒数据表批量修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolSocialMedia:update",
        serviceClass = KolSocialMediaService.class,
        keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> batchUpdate(@RequestBody @Validated KolSocialMediaDTO.BatchUpdateDTO dto) {
        List<KolSocialMediaDTO.UpdateDTO> dtoList = dto.getList();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        
        List<String> ids = dtoList.stream().map(KolSocialMediaDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<KolSocialMediaEntity> list = kolSocialMediaService.lambdaQuery().in(KolSocialMediaEntity::getId, ids).list();
        Map<String, KolSocialMediaEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolSocialMediaEntity::getId, e -> e));
        
        for (KolSocialMediaDTO.UpdateDTO updateDTO : dtoList) {
            BatchResultDTO updateResult;
            try {
                kolSocialMediaService.update(updateDTO);
                KolSocialMediaEntity entity = idEntityMap.get(updateDTO.getId());
                String code = entity != null ? entity.getId() : updateDTO.getId();
                updateResult = BatchResultDTO.success(updateDTO.getId(), code, "修改成功");
            } catch (Exception e) {
                log.error("达人社媒数据表批量修改失败", e);
                KolSocialMediaEntity entity = idEntityMap.get(updateDTO.getId());
                if (entity == null) {
                    updateResult = BatchResultDTO.fail(updateDTO.getId(), updateDTO.getId(), "达人社媒数据不存在，修改失败");
                } else {
                    updateResult = BatchResultDTO.fail(updateDTO.getId(), entity.getId(), e.getMessage());
                }
            }
            resultDTOS.add(updateResult);
        }
        
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 分页查询
     * @author wuhaotian
     * @date:  2025-12-04
     * @param dto
     * @return ApiResult<PagingVO<KolSocialMediaDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolSocialMedia:paging",
            tableAlias = "ksm")
    @WebAdvanceQuery
    public ApiResult<PagingVO<KolSocialMediaDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<KolSocialMediaDTO.ParamDTO> dto) {
        return success(kolSocialMediaService.paging(dto));
    }

    /**
     * 批量删除
     * @author wuhaotian
     * @date:  2025-12-04
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/batchDelete")
    @LogAction(value = LogActionEnum.DELETE, desc = "达人社媒数据表批量删除")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolSocialMedia:batchDelete",
            serviceClass = KolSocialMediaService.class,
            keyIdName = "ids")
    public ApiResult<?> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();

        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());

        List<KolSocialMediaEntity> list = kolSocialMediaService.lambdaQuery().in(KolSocialMediaEntity::getId, ids).list();

        Map<String, KolSocialMediaEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolSocialMediaEntity::getId, w -> w));

        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = kolSocialMediaService.delete(id);
            } catch (Exception e) {
                log.error("达人社媒数据表删除失败", e);
                KolSocialMediaEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "达人社媒数据不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     * @author wuhaotian
     * @date:  2025-12-04
     * @param dto
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "达人社媒数据表导出")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolSocialMedia:export",
            tableAlias = "ksm")
    public ApiResult<Boolean> export(@RequestBody @Validated PagingDTO<KolSocialMediaDTO.ParamDTO> dto) {
        return success(kolSocialMediaService.export(dto));
    }

}
