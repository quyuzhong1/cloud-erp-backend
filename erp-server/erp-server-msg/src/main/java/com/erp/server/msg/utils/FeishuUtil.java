package com.erp.server.msg.utils;

import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.server.msg.model.FeiShuSendBaseParam;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname: FeishuUtil
 * @Description: TODO
 * @CreateTime: 2023-04-20  17:23
 * @Author: zhangchunlin
 */
public class FeishuUtil {

    public static FeiShuSendBaseParam.ContentDTO wrapTypicalCard(NoticeMsgInfoDTO noticeMsgInfo) {
        FeiShuSendBaseParam.ContentDTO contentDTO = new FeiShuSendBaseParam.ContentDTO();

        FeiShuSendBaseParam.CardDTO.ConfigDTO  config = new FeiShuSendBaseParam.CardDTO.ConfigDTO();
        config.setWideScreenMode(true);
        contentDTO.setConfig(config);

        FeiShuSendBaseParam.CardDTO.HeaderDTO header = new FeiShuSendBaseParam.CardDTO.HeaderDTO();
        header.setTitle(new FeiShuSendBaseParam.CardDTO.HeaderDTO.TitleDTO("plain_text", noticeMsgInfo.getTitle()));
        contentDTO.setHeader(header);

        List<FeiShuSendBaseParam.CardDTO.ElementsDTO> elements = new ArrayList<>();

        FeiShuSendBaseParam.CardDTO.ElementsDTO fieldElementsDTO = new FeiShuSendBaseParam.CardDTO.ElementsDTO();
        fieldElementsDTO.setTag("div");
        List<FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO> fields = new ArrayList<>();
        FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO fieldsDTO = new FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO(true,
                new FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO.TextDTO("lark_md", noticeMsgInfo.getContent()));
        fields.add(fieldsDTO);
        fieldElementsDTO.setFields(fields);
        elements.add(fieldElementsDTO);

        // 暂不填充按钮等

        contentDTO.setElements(elements);
        return contentDTO;
    }

}