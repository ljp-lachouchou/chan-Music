# import requests
# from pathlib import Path
# import time
#
# # 配置参数
# API_BASE_URL = "http://localhost:8000"  # 替换为实际API地址
# AUDIO_SAVE_DIR = Path("./downloaded_audio")
# AUDIO_SAVE_DIR.mkdir(exist_ok=True)
#
#
# def generate_audio(prompt: str):
#     """调用音频生成API并下载结果"""
#     try:
#         # 1. 发送生成请求
#         response = requests.post(
#             url="http://localhost:8000/generate",
#             params={"text": prompt}  # 关键点[5,7](@ref)
#         )
#
#         # 2. 处理响应（网页4的错误处理方案）
#         if response.status_code == 200:
#             data = response.json()
#             print(f"✅ 生成成功 | 音频地址: {data['audio_url']}")
#
#             # 3. 下载音频文件（网页6的文件下载扩展）
#             download_response = requests.get(data["audio_url"])
#             if download_response.status_code == 200:
#                 filename = f"audio_{int(time.time())}.wav"
#                 save_path = AUDIO_SAVE_DIR / filename
#
#                 with open(save_path, "wb") as f:
#                     f.write(download_response.content)
#                 print(f"💾 保存成功 | 路径: {save_path}")
#                 return save_path
#             else:
#                 print(f"❌ 下载失败 | 状态码: {download_response.status_code}")
#         else:
#             print(f"❌ 生成失败 | 状态码: {response.status_code} | 详情: {response.text}")
#
#     except requests.exceptions.RequestException as e:
#         print(f"🚨 网络错误: {str(e)}")
#     except Exception as e:
#         print(f"⚠️ 未知错误: {str(e)}")
#
#
# # 使用示例
# if __name__ == "__main__":
#     prompt = "pop music with a soothing melody"  # 替换实际提示词
#     saved_file = generate_audio(prompt)
#
#     if saved_file:
#         print(f"🎧 请查看生成结果: {saved_file.resolve()}")

# test_client.py
# test_client.py
# test_client.py
import requests


def test_generate():
    url = "http://myredisapi.com/generate"
    headers = {"Content-Type": "application/json"}
    data = {"text": "lo-fo music with a soothing melody"}

    response = requests.post(url, json=data)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}")


if __name__ == "__main__":
    test_generate()