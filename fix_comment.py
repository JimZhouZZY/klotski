import re
import glob

def fix_broken_doc_comments(java_code):
    # 匹配 /** ... */ 注释块
    def replacer(match):
        comment = match.group(0)
        idx = comment.rfind('/**')
        if idx > 0:
            # 只保留最后一个 /** 后面的内容
            new_content = comment[idx + 3:-2].strip()
            return '/** ' + new_content + ' */'
        return comment
    # 用正则替换所有 /** ... */ 注释
    return re.sub(r'/\*\*[\s\S]*?\*/', replacer, java_code)

java_files = glob.glob("core/**/*.java", recursive=True)

for java_file in java_files:
    with open(java_file, 'r', encoding='utf-8') as f:
        code = f.read()
    fixed_code = fix_broken_doc_comments(code)
    with open(java_file, 'w', encoding='utf-8') as f:
        f.write(fixed_code)
