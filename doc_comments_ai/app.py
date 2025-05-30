import argparse
import os
import sys

from yaspin import yaspin

from doc_comments_ai import llm, utils
from doc_comments_ai.llm import GptModel, extract_doc_comment, insert_doc_comment
from doc_comments_ai.treesitter import Treesitter, TreesitterMethodNode


def run():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "dir",
        nargs="?",
        default=os.getcwd(),
        help="File to parse and generate doc comments for.",
    )
    parser.add_argument(
        "--local_model",
        type=str,
        help="Path to the local model.",
    )
    parser.add_argument(
        "--inline",
        action="store_true",
        help="Adds inline comments to the code if necessary.",
    )
    parser.add_argument(
        "--gpt4",
        action="store_true",
        help="Uses GPT-4 (default GPT-3.5).",
    )
    parser.add_argument(
        "--gpt3_5-16k",
        action="store_true",
        help="Uses GPT-3.5 16k (default GPT-3.5 4k).",
    )
    parser.add_argument(
        "--guided",
        action="store_true",
        help="User will get asked to confirm the doc generation for each method.",
    )
    parser.add_argument(
        "--azure-deployment",
        type=str,
        help="Azure OpenAI deployment name.",
    )
    parser.add_argument(
        "--ollama-model",
        type=str,
        help="Ollama model for base url",
    )
    parser.add_argument(
        "--ollama-base-url",
        type=str,
        default="http://localhost:11434",
        help="Ollama base url",
    )
    parser.add_argument(
        "--sustech-deepseek",
        action="store_true",
        help="Use the sustech-deepseek LLM.",
    )

    if sys.argv.__len__() < 2:
        sys.exit("Please provide a file")

    args = parser.parse_args()

    file_name = args.dir

    if not os.path.isfile(file_name):
        sys.exit(f"File {utils.get_bold_text(file_name)} does not exist")

    if utils.has_unstaged_changes(file_name):
        sys.exit(f"File {utils.get_bold_text(file_name)} has unstaged changes")

    if args.azure_deployment:
        utils.is_azure_openai_environment_available()
        llm_wrapper = llm.LLM(azure_deployment=args.azure_deployment)
    elif args.gpt4:
        utils.is_openai_api_key_available()
        llm_wrapper = llm.LLM(model=GptModel.GPT_4)
    elif args.gpt3_5_16k:
        utils.is_openai_api_key_available()
        llm_wrapper = llm.LLM(model=GptModel.GPT_35_16K)
    elif args.ollama_model:
        llm_wrapper = llm.LLM(ollama=(args.ollama_base_url, args.ollama_model))
    elif args.sustech_deepseek:
        llm_wrapper = llm.LLM(model="sustech-deepseek")
    else:
        llm_wrapper = llm.LLM(local_model=args.local_model)

    generated_doc_comments = {}

    with open(file_name, "r") as file:
        # Read the entire content of the file into a string
        file_bytes = file.read().encode()

        file_extension = utils.get_file_extension(file_name)
        programming_language = utils.get_programming_language(file_extension)

        treesitter_parser = Treesitter.create_treesitter(programming_language)
        treesitterNodes: list[TreesitterMethodNode] = treesitter_parser.parse(
            file_bytes
        )

        for node in treesitterNodes:
            method_name = utils.get_bold_text(node.name)

            if node.doc_comment:
                print(
                    f"⚠️  Method {method_name} already has a doc comment. Skipping..."
                )
                continue

            if args.guided:
                print(f"Generate doc for {utils.get_bold_text(method_name)}? (y/n)")
                if not input().lower() == "y":
                    continue

            method_source_code = node.method_source_code

            tokens = utils.count_tokens(method_source_code)
            if tokens > 2048 and not (args.gpt4 or args.gpt3_5_16k):
                print(
                    f"⚠️  Method {method_name} has too many tokens. "
                    f"Consider using {utils.get_bold_text('--gpt4')} "
                    f"or {utils.get_bold_text('--gpt3_5-16k')}. "
                    "Skipping for now..."
                )
                continue

            spinner = yaspin(text=f"🔧 Generating doc comment for {method_name}...")
            spinner.start()

            documented_method_source_code = llm_wrapper.generate_doc_comment(
                programming_language.value, method_source_code, args.inline
            )

            print("\n--- LLM Raw Output Start ---")
            print(documented_method_source_code)
            print("--- LLM Raw Output End ---\n")

            # 后处理：提取注释内容并拼接
            doc_comment = extract_doc_comment(documented_method_source_code)
            if not doc_comment.strip().startswith("/**"):
                print(f"Warning: Output for {method_name} is not a standard doc comment.")

            merged_code = insert_doc_comment(method_source_code, doc_comment)

            generated_doc_comments[method_source_code] = merged_code

            spinner.stop()
            print(f"✅ Doc comment for {method_name} generated.")

    file.close()

    for original_code, merged_code in generated_doc_comments.items():
        utils.write_code_snippet_to_file(
            file_name, original_code, merged_code
        )
