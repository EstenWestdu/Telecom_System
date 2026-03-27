import requests
import time

# 配置项
URL = "http://localhost:8080/user/menu"
ITERATIONS = 50  # 用户端接口逻辑较重，建议测 50-100 次
# 注意：这里必须是普通用户的 Session 或是 Token
HEADERS = {
    "Cookie": "JSESSIONID=7B03F48BC2ABF7DBEF813ADA924E160D",
    "User-Agent": "Mozilla/5.0"
}

def run_user_test():
    print(f"--- 开始测试用户主页接口性能 ---")
    latencies = []
    
    for i in range(ITERATIONS):
        start = time.perf_counter()
        try:
            res = requests.get(URL, headers=HEADERS)
            if res.status_code != 200:
                print(f"请求失败: {res.status_code}")
                continue
        except Exception as e:
            print(f"异常: {e}")
            break
            
        latency = (time.perf_counter() - start) * 1000
        latencies.append(latency)
        
    if latencies:
        print(f"\n[用户接口统计]:")
        print(f"  平均耗时: {sum(latencies)/len(latencies):.2f} ms")
        print(f"  P95 耗时: {sorted(latencies)[int(len(latencies)*0.95)]:.2f} ms (95% 的用户体验在此之下)")
        print(f"  最快: {min(latencies):.2f} ms / 最慢: {max(latencies):.2f} ms")

run_user_test()