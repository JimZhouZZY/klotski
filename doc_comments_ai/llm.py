import os
import re
import subprocess
import sys
from enum import Enum

import inquirer
import requests
from langchain.chains import LLMChain
from langchain.chat_models import ChatLiteLLM
from langchain.llms import LlamaCpp, Ollama
from langchain.prompts import PromptTemplate

from doc_comments_ai import utils


class GptModel(Enum):
    GPT_35 = "gpt-3.5-turbo"
    GPT_35_16K = "gpt-3.5-turbo-16k"
    GPT_4 = "gpt-4"


class LLM:
    def __init__(
        self,
        model: GptModel = GptModel.GPT_35,
        local_model: "str | None" = None,
        azure_deployment: "str | None" = None,
        ollama: "tuple[str,str] | None" = None,
    ):
        max_tokens = 2048 if model == GptModel.GPT_35 else 4096
        if local_model is not None:
            self.install_llama_cpp()
            self.llm = LlamaCpp(
                model_path=local_model,
                temperature=0.8,
                max_tokens=max_tokens,
                verbose=False,
            )
        elif azure_deployment is not None:
            self.llm = ChatLiteLLM(
                temperature=0.8,
                max_tokens=max_tokens,
                model=f"azure/{azure_deployment}",
            )
        elif ollama is not None:
            self.llm = Ollama(
                base_url=ollama[0],
                model=ollama[1],
                temperature=0.8,
                num_ctx=max_tokens,
            )
        elif model == "sustech-deepseek":
            self.llm = DeepseekLLM(api_key="61a60218408332449485a97419e88184d327ac58a8a38a3698082c48a1fc370a")
            self.is_deepseek = True
        else:
            self.llm = ChatLiteLLM(
                temperature=0.8, max_tokens=max_tokens, model=model.value
            )
            self.is_deepseek = False

        self.template = (
            "Add a detailed doc comment to the following {language} method:\n{code}\n"
            "The doc comment should describe what the method does. "
            "{inline_comments} "
            "Return the method implementaion with the doc comment as a single markdown code block. "
            "Don't include any explanations {haskell_missing_signature}in your response."
        )
        if not getattr(self, "is_deepseek", False):
            self.prompt = PromptTemplate(
                template=self.template,
                input_variables=[
                    "language",
                    "code",
                    "inline_comments",
                    "Haskell_missing_signature",
                ],
            )
            self.chain = LLMChain(llm=self.llm, prompt=self.prompt)

    def generate_doc_comment(self, language, code, inline=False):
        """
        Generates a doc comment for the given method
        """
        if inline:
            inline_comments = (
                "Add inline comments to the method body where it makes sense."
            )
        else:
            inline_comments = ""

        if language == "haskell":
            haskell_missing_signature = "and missing type signatures "
        else:
            haskell_missing_signature = ""

        input = {
            "language": language,
            "code": code,
            "inline_comments": inline_comments,
            "haskell_missing_signature": haskell_missing_signature,
        }

        if getattr(self, "is_deepseek", False):
            return self.llm.run(input)
        else:
            documented_code = self.chain.run(input)
            return documented_code

    def install_llama_cpp(self):
        try:
            from llama_cpp import Llama
        except:  # noqa: E722
            question = [
                inquirer.Confirm(
                    "confirm",
                    message=f"Local LLM interface package not found. Install {utils.get_bold_text('llama-cpp-python')}?",
                    default=True,
                ),
            ]

            answers = inquirer.prompt(question)
            if answers and answers["confirm"]:
                import platform

                def check_command(command):
                    try:
                        subprocess.run(
                            command,
                            check=True,
                            stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE,
                        )
                        return True
                    except subprocess.CalledProcessError:
                        return False
                    except FileNotFoundError:
                        return False

                def install_llama(backend):
                    env_vars = {"FORCE_CMAKE": "1"}

                    if backend == "cuBLAS":
                        env_vars["CMAKE_ARGS"] = "-DLLAMA_CUBLAS=on"
                    elif backend == "hipBLAS":
                        env_vars["CMAKE_ARGS"] = "-DLLAMA_HIPBLAS=on"
                    elif backend == "Metal":
                        env_vars["CMAKE_ARGS"] = "-DLLAMA_METAL=on"
                    else:  # Default to OpenBLAS
                        env_vars[
                            "CMAKE_ARGS"
                        ] = "-DLLAMA_BLAS=ON -DLLAMA_BLAS_VENDOR=OpenBLAS"

                    try:
                        subprocess.run(
                            [
                                sys.executable,
                                "-m",
                                "pip",
                                "install",
                                "llama-cpp-python",
                            ],
                            env={**os.environ, **env_vars},
                            check=True,
                        )
                    except subprocess.CalledProcessError as e:
                        print(f"Error during installation with {backend}: {e}")

                def supports_metal():
                    # Check for macOS version
                    if platform.system() == "Darwin":
                        mac_version = tuple(map(int, platform.mac_ver()[0].split(".")))
                        # Metal requires macOS 10.11 or later
                        if mac_version >= (10, 11):
                            return True
                    return False

                # Check system capabilities
                if check_command(["nvidia-smi"]):
                    install_llama("cuBLAS")
                elif check_command(["rocminfo"]):
                    install_llama("hipBLAS")
                elif supports_metal():
                    install_llama("Metal")
                else:
                    install_llama("OpenBLAS")

                print("Finished downloading `Code-Llama` interface.")

                # Check if on macOS
                if platform.system() == "Darwin":
                    # Check if it's Apple Silicon
                    if platform.machine() != "arm64":
                        print(
                            "Warning: You are using Apple Silicon (M1/M2) Mac but your Python is not of 'arm64' architecture."
                        )
                        print(
                            "The llama.ccp x86 version will be 10x slower on Apple Silicon (M1/M2) Mac."
                        )
                        print(
                            "\nTo install the correct version of Python that supports 'arm64' architecture visit:"
                            "https://github.com/conda-forge/miniforge"
                        )

            else:
                print("", "Installation cancelled. Exiting.", "")
                return None


class DeepseekLLM:
    """
    A wrapper for the DeepSeek LLM API to generate doc comments.
    """
    def __init__(self, api_key: str, endpoint: str = "http://172.18.36.55:5001/api/v1/chat/completions"):
        self.api_key = api_key
        self.endpoint = endpoint

    def run(self, input):
        import sys
        prompt = (
            "Add a detailed doc comment to the following {language} method:\n{code}\n"
            "The doc comment should describe what the method does. "
            "{inline_comments} "
            "Only output the doc comment for the following {language} method, wrapped with /** ... */. \n"
            "Do not output the method code or any explanation.\n"
            "Don't include any explanations {haskell_missing_signature}in your response."
        ).format(
            language=input["language"],
            code=input["code"],
            inline_comments=input["inline_comments"],
            haskell_missing_signature=input["haskell_missing_signature"],
        )
        payload = {
            "model": "deepseek-r1-standard",
            "messages": [
                {"role": "user", "content": prompt}
            ],
            "temperature": 0.8,
            "stream": False
        }
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
            "Accept": "application/json"
        }
        print(">>> Sending request to Deepseek with headers:", headers)
        sys.stdout.flush()
        try:
            response = requests.post(self.endpoint, json=payload, headers=headers, timeout=120)
            print("Deepseek raw response:", response.text)
            sys.stdout.flush()
            response.raise_for_status()
        except Exception as e:
            print(f"Deepseek request failed: {e}")
            return ""

        # 处理 SSE 格式，提取 data: 后的内容
        lines = response.text.splitlines()
        content_lines = []
        for line in lines:
            if line.startswith("data: "):
                content = line[len("data: "):]
                if content.strip() == "[h_newline]":
                    content_lines.append("\n")
                elif content.strip() and content.strip() not in ["[DONE]", "[newline]"]:
                    content_lines.append(content)
        merged = "".join(content_lines) if content_lines else response.text

        # 只提取 markdown 代码块
        code_block = self._extract_code_block(merged)
        return code_block if code_block else merged

    @staticmethod
    def _extract_code_block(text):
        # 匹配 ```java ... ``` 或 ```...```
        match = re.search(r"```(\w+)?\n?([\s\S]+?)```", text)
        if match:
            lang = match.group(1) or "java"
            code = match.group(2)
        else:
            # 兼容没有 ``` 包裹但有类似 java/**... 的情况
            match2 = re.match(r"(\w+)\s*(/[*]{2}[\s\S]+)", text)
            if match2:
                lang = match2.group(1)
                code = match2.group(2)
            else:
                # fallback
                lang = "java"
                code = text

        # 替换 [h_newline] 为换行
        code = code.replace("[h_newline]", "\n")
        # 去除多余的 data: 前缀
        code = re.sub(r"^data:\s*", "", code, flags=re.MULTILINE)
        # 去除开头多余空白
        code = code.lstrip()
        return f"```{lang}\n{code}\n```"


def extract_doc_comment(text):
    # 提取所有 /** ... */ 注释，返回最后一个
    matches = list(re.finditer(r"/\*\*[\s\S]*?\*/", text))
    if matches:
        return matches[-1].group(0)
    else:
        print("Warning: DeepSeek output is not a standard doc comment. Attempting to extract...")
        # 尝试截取最后一个 /** 开始到最近的 */ 结束
        start = text.rfind("/**")
        end = text.find("*/", start)
        if start != -1 and end != -1:
            return text[start:end+2]
        # fallback: 返回全部
        return text

def insert_doc_comment(method_code, doc_comment):
    # 获取方法体的缩进
    lines = method_code.splitlines()
    for line in lines:
        if line.strip():  # 找到第一个非空行
            indent = re.match(r"\s*", line).group(0)
            break
    else:
        indent = ""
    # 给注释每一行加缩进
    doc_comment_indented = "\n".join(
        indent + line if line.strip() else line
        for line in doc_comment.splitlines()
    )
    # 插入到方法前
    return doc_comment_indented + "\n" + method_code
