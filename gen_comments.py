import subprocess
import glob
import sys

java_files = glob.glob("core/**/*.java", recursive=True)

for java_file in java_files:
    print(f"Processing {java_file} ...")
    with subprocess.Popen(
        [
            "python", "-m", "doc_comments_ai",
            "--sustech-deepseek",
            java_file
        ],
        stdout=sys.stdout,   # 直接转发到父进程的标准输出
        stderr=sys.stderr,   # 错误输出也转发
        text=True,
        bufsize=1
    ) as proc:
        proc.wait()
