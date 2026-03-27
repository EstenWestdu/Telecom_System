import requests
import time
from urllib.parse import quote

# 配置项
URL = "http://localhost:8080/user/menu"
ITERATIONS = 100

# 如果你的 Cookie 里有中文（比如：userName=张三），请手动处理
# 建议直接从浏览器 F12 复制最原始的 Cookie 字符串，通常浏览器已经帮你编码过了
raw_cookie = "7B03F48BC2ABF7DBEF813ADA924E160D" 

HEADERS = {
    # 使用 quote 确保所有字符都能被 latin-1 处理
    "Cookie": raw_cookie.encode('utf-8').decode('latin-1'),
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
}
def run_test(test_name):
    print(f"--- 正在开始测试: {test_name} ---")
    latencies = []
    
    for i in range(ITERATIONS):
        start_time = time.perf_counter()
        try:
            response = requests.get(URL, headers=HEADERS)
            # 确保请求成功
            if response.status_code != 200:
                print(f"请求失败！状态码: {response.status_code}")
                break
        except Exception as e:
            print(f"连接异常: {e}")
            break
            
        end_time = time.perf_counter()
        # 计算毫秒数
        latency = (end_time - start_time) * 1000
        latencies.append(latency)
        
        if (i + 1) % 20 == 0:
            print(f"已完成 {i + 1} 次请求...")

    if latencies:
        avg_latency = sum(latencies) / len(latencies)
        min_latency = min(latencies)
        max_latency = max(latencies)
        print(f"\n结果统计 ({test_name}):")
        print(f"  平均耗时: {avg_latency:.2f} ms")
        print(f"  最小耗时: {min_latency:.2f} ms")
        print(f"  最大耗时: {max_latency:.2f} ms")
        print("-" * 30)

if __name__ == "__main__":
    run_test("电信系统 /user/menu 接口性能测试")