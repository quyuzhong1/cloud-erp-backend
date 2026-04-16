import time

def snow_next_id(our_epoch=1288834974657, shard_id=5):
    """
    模拟 pgsql 函数 snow_next_id 的逻辑计算 Snowflake ID。
    
    参数:
    - our_epoch: 系统起始时间戳 (毫秒)
    - shard_id: 分片 ID / 机器 ID
    
    算法: ((now_millis - our_epoch) << 22) | (shard_id << 12) | 1
    """
    now_millis = int(time.time() * 1000)
    # 确保时间不早于 epoch
    if now_millis < our_epoch:
        raise ValueError("Current time is earlier than our_epoch.")
        
    id_val = ((now_millis - our_epoch) << 22) | (shard_id << 12) | 1
    return id_val

if __name__ == "__main__":
    start_id = snow_next_id()
    print(start_id)
