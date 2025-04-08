# # generators/musicgen.py
# import os
#
# from transformers import pipeline
# import scipy
# import numpy as np
#
# class AudioGenerator:
#     def __init__(self, model_path, temp_dir):
#         self.synthesiser = pipeline(
#             "text-to-audio",
#             model=model_path,
#
#             device_map=None  # 自动选择GPU/CPU
#         )
#         self.temp_dir = temp_dir
#         os.makedirs(temp_dir, exist_ok=True)
#
#     def generate(self, prompt, **kwargs):
#
#         """生成音频核心逻辑[7](@ref)"""
#         music = self.synthesiser(
#             prompt,
#             forward_params={
#                 "do_sample": True,
#                 **kwargs
#             }
#         )
#         return {
#             "sampling_rate": music["sampling_rate"],
#             "audio": music["audio"].astype(np.float32)
#         }
#
#     def save_audio(self, filename, audio_data):
#         """保存音频文件"""
#         full_path = os.path.join(self.temp_dir, filename)
#         scipy.io.wavfile.write(
#             full_path,
#             rate=audio_data["sampling_rate"],
#             data=audio_data["audio"]
#         )

# musicgen.py（优化版）
import os
import scipy
import numpy as np
from transformers import pipeline
import logging

logger = logging.getLogger("AudioGenWorker")

class AudioGenerator:
    """增强版音频生成器"""
    def __init__(self, model_path, temp_dir):
        self.model_path = model_path
        self.temp_dir = temp_dir
        self._model = None
        os.makedirs(temp_dir, exist_ok=True)
        logger.info("🎛️ 初始化音频生成器实例")

    @property
    def synthesiser(self):
        """显式加载模型"""
        if self._model is None:
            logger.info("⏳ 正在加载模型...")
            self._model = pipeline(
                "text-to-audio",
                model=self.model_path,
                device_map=None,
                torch_dtype="auto"
            )
            logger.info("✅ 模型加载完成")
        return self._model

    def generate(self, prompt, **kwargs):
        """生成音频数据"""
        logger.info(f"🎹 生成音频: {prompt[:20]}...")
        music = self.synthesiser(
            prompt,
            forward_params={
                "do_sample": True,
                "max_new_tokens": 512,
                **kwargs
            }
        )
        return self._process_audio(music)

    def _process_audio(self, music):
        """音频后处理"""
        audio = music["audio"].astype(np.float32)
        peak = np.max(np.abs(audio))
        return {
            "sampling_rate": music["sampling_rate"],
            "audio": audio / peak if peak > 0 else audio
        }

    def save_audio(self, filename, audio_data):
        """安全保存音频文件"""
        full_path = os.path.join(self.temp_dir, filename)
        scipy.io.wavfile.write(
            full_path,
            rate=audio_data["sampling_rate"],
            data=audio_data["audio"]
        )
        logger.info(f"💾 音频保存至: {full_path}")