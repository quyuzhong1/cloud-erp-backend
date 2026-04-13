import os
import re
import sys

# 路径配置
API_ERROR_PATH = "src/main/java/com/common/core/enums/ApiError.java"
I18N_ZH_PATH = "src/main/resources/i18n/messages_zh_CN.properties"

class ExceptionManager:
    def __init__(self):
        # 匹配枚举行格式: ENUM_NAME(ID, "code")
        self.error_pattern = re.compile(r'([A-Z0-9_]+)\((\d+),\s*"([^"]+)"\)')

    def check_duplication(self, enum_name, code, message):
        """去重防线：强制检查枚举名、Code和语义语义"""
        if not os.path.exists(API_ERROR_PATH):
            return f"🚨 错误: 未找到文件 {API_ERROR_PATH}"

        with open(API_ERROR_PATH, 'r', encoding='utf-8') as f:
            content = f.read()
            if enum_name in content:
                return f"🚨 失败: 枚举名 [{enum_name}] 已存在，严禁重复生成！"
            if f'"{code}"' in content:
                return f"🚨 失败: Code [{code}] 已存在！"

        if os.path.exists(I18N_ZH_PATH):
            with open(I18N_ZH_PATH, 'r', encoding='utf-8') as f:
                if message in f.read():
                    return f"🚨 警告: 发现语义重复的消息 [{message}]，请优先复用！"
        return None

    def add_exception(self, prefix, enum_name, code, message):
        """执行就近原则插入与多文件同步"""
        dup_msg = self.check_duplication(enum_name, code, message)
        if dup_msg:
            print(dup_msg)
            return

        # 1. 修改 ApiError.java
        with open(API_ERROR_PATH, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        insert_pos = -1
        last_id = 0
        # 寻找业务域块的最后一行（就近原则）
        for i, line in enumerate(lines):
            if prefix in line:
                insert_pos = i
                match = self.error_pattern.search(line)
                if match:
                    last_id = int(match.group(2))

        if insert_pos == -1:
            print(f"🚨 未找到业务域前缀 [{prefix}]，请先手动创建该业务区间。")
            return

        new_id = last_id + 1
        new_enum_line = f'    {enum_name}({new_id}, "{code}"),\n'
        lines.insert(insert_pos + 1, new_enum_line)

        with open(API_ERROR_PATH, 'w', encoding='utf-8') as f:
            f.writelines(lines)

        # 2. 修改消息文件 (I18n 自动化映射)
        with open(I18N_ZH_PATH, 'a', encoding='utf-8') as f:
            f.write(f"\n{code}={message}")

        print(f"✅ 成功! 已在 [{prefix}] 块下新增 ID:{new_id} | {enum_name}")

if __name__ == "__main__":
    if len(sys.argv) < 5:
        print("用法: python exception_manager.py [前缀] [枚举名] [驼峰Code] [中文消息]")
    else:
        mgr = ExceptionManager()
        mgr.add_exception(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])