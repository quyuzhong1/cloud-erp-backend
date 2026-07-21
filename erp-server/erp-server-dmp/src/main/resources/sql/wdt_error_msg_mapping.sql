-- 旺店通推送失败错误信息映射（dict_basic）
-- 用途：告警/推送失败时，对纯英文类旺店通报错（不含中文、含英文字母）且命中字典英文关键字时做中文映射
-- 展示：命中映射 → 【中文说明】原始信息：{旺店通原文}；含中文或未命中 → 直接展示原文
-- 维护说明：
--   type = wdtErrorMsgMapping
--   remark = KEYWORD → name 为英文匹配关键字（不区分大小写），value 为中文说明
--   sort 越大越优先（同长度关键字时生效）
-- 部署后请清理字典缓存：cache:dmp:dict:type::wdtErrorMsgMapping

UPDATE dict_basic
SET is_deleted       = TRUE,
    update_user_id   = '',
    update_user_name = '',
    update_time      = now(),
    version          = version + 1
WHERE type = 'wdtErrorMsgMapping'
  AND is_deleted = FALSE;

INSERT INTO dict_basic (id, create_user_id, create_user_name, create_time, update_user_id, update_user_name,
                        update_time, version, is_deleted, remark, value, type, name, status, sort, type_name)
VALUES (snow_next_id(), '', '', now(), '', '', now(), 0, FALSE, 'KEYWORD',
        '旺店通侧数据库异常，非数大臣数据问题，请联系旺店通支持', 'wdtErrorMsgMapping', 'Unknown Database Error', TRUE,
        100, '旺店通错误信息映射'),
       (snow_next_id(), '', '', now(), '', '', now(), 0, FALSE, 'KEYWORD', '旺店通审核校验失败', 'wdtErrorMsgMapping',
        'check_fail', TRUE, 90, '旺店通错误信息映射'),
       (snow_next_id(), '', '', now(), '', '', now(), 0, FALSE, 'KEYWORD',
        '旺店通 SKU/商家编码不存在或未启用，请检查商品同步', 'wdtErrorMsgMapping', 'spec_no', TRUE, 80,
        '旺店通错误信息映射'),
       (snow_next_id(), '', '', now(), '', '', now(), 0, FALSE, 'KEYWORD', '旺店通侧单据号重复，请检查是否重复推送',
        'wdtErrorMsgMapping', 'duplicate', TRUE, 70, '旺店通错误信息映射'),
       (snow_next_id(), '', '', now(), '', '', now(), 0, FALSE, 'KEYWORD', '调用旺店通超时，请稍后重试', 'wdtErrorMsgMapping',
        'timeout', TRUE, 65, '旺店通错误信息映射'),
       (snow_next_id(), '', '', now(), '', '', now(), 0, FALSE, 'KEYWORD', '调用旺店通超时，请稍后重试', 'wdtErrorMsgMapping',
        'timed out', TRUE, 64, '旺店通错误信息映射'),
       (snow_next_id(), '', '', now(), '', '', now(), 0, FALSE, 'KEYWORD', '无法连接旺店通服务，请检查网络或旺店通服务状态',
        'wdtErrorMsgMapping', 'Connection refused', TRUE, 63, '旺店通错误信息映射'),
       (snow_next_id(), '', '', now(), '', '', now(), 0, FALSE, 'KEYWORD', '无法连接旺店通服务，请检查网络或旺店通服务状态',
        'wdtErrorMsgMapping', 'Connection reset', TRUE, 62, '旺店通错误信息映射');
