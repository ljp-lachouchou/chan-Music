from fastapi import FastAPI, HTTPException, BackgroundTasks, Body
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from contextlib import asynccontextmanager
import uvicorn
import uuid
import os
import multiprocessing
import logging
import time
import warnings
import asyncio
from typing import Dict

# 本地模块
from musicgen import AudioGenerator
task_status: Dict[str, dict] = {}
# 配置常量
MODEL_PATH = "C:/Users/卢俊澎/.cache/huggingface/hub/models--facebook--musicgen-small"
TEMP_DIR = os.path.abspath("./temp_audio")
os.makedirs(TEMP_DIR, exist_ok=True)

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("AudioGenAPI")

# Windows多进程配置
if os.name == 'nt':
    try:
        multiprocessing.set_start_method('spawn')
    except RuntimeError:
        pass
mp_ctx = multiprocessing.get_context('spawn')

# 进程间通信队列
TASK_QUEUE = mp_ctx.Queue(maxsize=100)
RESULT_QUEUE = mp_ctx.Queue(maxsize=100)
task_events: Dict[str, asyncio.Event] = {}
result_queue_listener = None

class ModelWorker:
    """增强版模型工作器"""

    def __init__(self, model_path, temp_dir):
        self.model_path = model_path
        self.temp_dir = temp_dir
        self._model = None

    def initialize(self):
        """显式初始化模型"""
        logger.info(f"🔧 初始化模型工作器 | PID: {os.getpid()}")
        self._model = AudioGenerator(self.model_path, self.temp_dir)
        # 触发模型加载
        _ = self._model.synthesiser
        logger.info(f"✅ 模型加载完成 | 内存: {self.get_memory_usage()} MB")

    def get_memory_usage(self):
        """获取进程内存使用"""
        import psutil
        process = psutil.Process(os.getpid())
        return process.memory_info().rss / 1024**2

    def process_task(self, task: dict):
        """处理生成任务"""
        try:
            logger.info(f"🎵 开始生成: {task['task_id']}")
            result = self._model.generate(task["text"])
            self._model.save_audio(task["filename"], result)
            return {"status": "success", "filename": task["filename"]}
        except Exception as e:
            logger.error(f"生成失败: {str(e)}")
            return {"status": "failed", "error": str(e)}


def worker_process(task_queue, result_queue, model_path, temp_dir):
    """增加调试日志的工作进程"""
    logger = logging.getLogger("WorkerProcess")
    try:
        logger.info(f"🔄 工作进程 {os.getpid()} 启动")

        # 初始化阶段
        logger.info("⏳ 创建生成器实例...")
        worker = ModelWorker(model_path, temp_dir)

        logger.info("🔥 开始初始化模型...")
        worker.initialize()  # 显式初始化

        logger.info("🚀 进入任务处理循环")
        while True:
            task = task_queue.get()
            if task is None:
                logger.info("🛑 收到终止信号")
                break

            logger.info(f"📥 收到任务: {task['task_id']}")
            result = worker.process_task(task)

            logger.info(f"📤 提交结果: {task['task_id']}")
            result_queue.put({
                "task_id": task["task_id"],
                "status": result["status"],
                "filename": task["filename"]
            })

    except Exception as e:
        logger.error(f"💥 工作进程崩溃: {str(e)}", exc_info=True)
        result_queue.put({
            "task_id": "SYSTEM_ERROR",
            "status": "failed",
            "error": f"Worker Crash: {str(e)}"
        })


# 新增队列监听协程
async def listen_result_queue():
    """跨进程队列监听器（关键实现）"""
    while True:
        try:
            # 使用线程池处理阻塞操作
            result = await asyncio.get_event_loop().run_in_executor(
                None,
                lambda: RESULT_QUEUE.get(timeout=0.1)
            )

            task_id = result["task_id"]
            if task_id in task_events:
                task_events[task_id].set()
                del task_events[task_id]

        except multiprocessing.queues.Empty:
            await asyncio.sleep(0.1)
        except Exception as e:
            logger.error(f"队列监听错误: {str(e)}")
@asynccontextmanager
async def lifespan(app: FastAPI):
    """增强的生命周期管理"""
    num_workers = min(4, os.cpu_count() or 1)
    processes = []

    try:
        logger.info(f"🚀 启动 {num_workers} 个工作进程...")

        # 创建工作进程
        for i in range(num_workers):
            p = mp_ctx.Process(
                target=worker_process,
                args=(TASK_QUEUE, RESULT_QUEUE, MODEL_PATH, TEMP_DIR)
            )
            p.start()
            processes.append(p)
            logger.info(f"🔄 启动工作进程 {i + 1}/{num_workers}")
            time.sleep(10)  # 确保顺序初始化

        # 验证模型加载
        logger.info("🔍 验证模型加载状态...")
        TASK_QUEUE.put({
            "task_id": "health_check",
            "text": "test sound",
            "filename": "health_check.wav",
            "temp_dir": TEMP_DIR  # 添加缺失的必须字段
        })

        # 等待验证结果
        start_time = time.time()
        while time.time() - start_time < 120:  # 2分钟超时
            if not RESULT_QUEUE.empty():
                result = RESULT_QUEUE.get()
                if result["task_id"] == "health_check":
                    if result.get("status") == "success":  # 直接访问status字段
                        logger.info("✅ 所有模型加载验证通过")
                        break
                    else:
                        logger.error("❌ 模型加载验证失败")
                        raise RuntimeError("模型加载失败")
            await asyncio.sleep(2)
        else:
            raise TimeoutError("模型加载验证超时")

        global result_queue_listener
        result_queue_listener = asyncio.create_task(listen_result_queue())

        yield

        # 清理监听任务
        result_queue_listener.cancel()
        try:
            await result_queue_listener
        except asyncio.CancelledError:
            pass

    finally:
        logger.info("🛑 清理资源...")
        # 发送终止信号
        for _ in range(num_workers):
            TASK_QUEUE.put(None)
        # 等待进程退出
        for p in processes:
            p.join(timeout=30)
            if p.exitcode is None:
                p.terminate()


app = FastAPI(
    title="AudioGen API - Stable",
    lifespan=lifespan
)

# 跨域配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# 修改生成端点
@app.post("/generate")
async def generate_audio(text: str = Body(..., embed=True)):
    task_id = str(uuid.uuid4())
    filename = f"{task_id}.wav"

    # 创建异步事件
    completion_event = asyncio.Event()
    task_events[task_id] = completion_event

    # 提交任务到队列
    TASK_QUEUE.put({
        "task_id": task_id,
        "text": text,
        "filename": filename,
        "temp_dir": TEMP_DIR
    })

    # 等待任务完成通知
    await completion_event.wait()

    # 返回最终响应
    return {
        "task_id": task_id,
        "audio_url": f"http://myredisapi.com/audio/{filename}"
    }


@app.get("/tasks/{task_id}")
async def get_task_status(task_id: str):
    file_path = os.path.join(TEMP_DIR, f"{task_id}.wav")
    status = {
        "status": "not_found",
        "filename": task_id + ".wav"
    }
    if os.path.exists(file_path):
        status.update({
            "status": "completed",
            "audio_url": f"http://myredisapi.com/audio/{task_id}.wav"
        })
    elif task_id in task_status:
        status = task_status[task_id]

    return status


@app.get("/health")
async def health_check():
    return {
        "status": "ready",
        "workers": len(multiprocessing.active_children()),
        "queue_size": TASK_QUEUE.qsize()
    }
# 静态文件路由
app.mount("/audio", StaticFiles(directory=TEMP_DIR), name="audio")

if __name__ == "__main__":
    multiprocessing.freeze_support()
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        workers=1,
        log_level="info"
    )