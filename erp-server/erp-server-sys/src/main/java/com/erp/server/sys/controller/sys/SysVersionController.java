package com.erp.server.sys.controller.sys;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.SysVersionDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.dto.MessageDTO;
import com.erp.server.sys.handler.SysVersionQueryHandler;
import com.erp.server.sys.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static com.common.core.controller.vo.ApiResult.error;
import static com.common.core.controller.vo.ApiResult.success;

/**
 * @Author: wtr
 * @Date: 2026/4/10 10:59
 * @Param:
 * @Return:
 * @Description: 版本更新
 **/
@Slf4j
@RestController
@RequestMapping("/sysVersion")
public class SysVersionController {

    @Resource
    private MessageService messageService;

    /**
     * 新增
     * @author wtr
     * @date: 202-04-10
     * @param dto
     * @return
     */
    @PostMapping("/release")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> release(@RequestBody @Validated SysVersionDTO.AddDTO dto) {
        return success(messageService.addSysVersion(dto));
    }

    /**
     * 列表查询
     * @author wtr
     * @date: 202-04-10
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:sysVersion:paging",
            tableAlias = "an"
    )
    @WebAdvanceQuery(handler = SysVersionQueryHandler.class)
    public ApiResult<PagingVO<SysVersionDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SysVersionDTO.PagingParamDTO> dto) {
        return success(messageService.pagingSysVersion(dto));
    }

    /**
     * 删除
     * @author wtr
     * @date:  2026-04-10
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:sysVersion:delete",
            serviceClass = MessageService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<MessageEntity> list = messageService.lambdaQuery().in(MessageEntity::getId, ids).list();
        Map<String, MessageEntity> idEntityMap = list.stream().collect(Collectors.toMap(MessageEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = messageService.deleteVersion(id);
            }catch (Exception e){
                log.error("删除失败",e);
                MessageEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), "", e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : error("",resultDTOS);
    }

    /**
     * PC端版本更新历史消息查询
     * @author wtr
     * @date: 202-04-10
     * @param dto
     * @return
     */
    @PostMapping("/pagingHistoryVersion")
    @WebAdvanceQuery(handler = SysVersionQueryHandler.class)
    public ApiResult<PagingVO<SysVersionDTO.ListHistoryVersionDTO>> pagingHistoryVersion(@RequestBody @Validated PagingDTO<SysVersionDTO.HistoryVersionPagingParamDTO> dto) {
        return success(messageService.pagingHistoryVersion(dto));
    }

    /**
     * PC端版本更新历史消息已读
     * @return
     */
    @PostMapping("/readHistoryVersion")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
    public ApiResult<?> readHistoryVersion(@RequestBody SysVersionDTO.ReadHistoryVersionDTO dto) {
        messageService.readHistoryVersion(dto);
        return success();
    }

    /**
     * 获取版本更新未读数量
     * @return
     */
    @GetMapping("/unreadCount")
    public ApiResult<Integer> getUnreadCount() {
        return success(messageService.getSysVersionUnreadCount());
    }

    /**
     * 获取最新的版本升级
     * @return
     */
    @PostMapping("/getLatestVersion")
    public ApiResult<SysVersionDTO.LatestVersionDTO > getLatestVersion() {
        return success(messageService.getLatestVersion());
    }
}
