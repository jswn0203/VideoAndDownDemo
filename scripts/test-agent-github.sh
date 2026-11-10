#!/usr/bin/env bash
# GitHub 版测试 Agent：解析 JUnit → PR 评论 + ci:green/red 标签 → auto 模式失败 @claude
# 运行于 GitHub Actions（ubuntu runner 自带 gh CLI），或本地配好 gh 后手动执行
# 依赖环境变量：GH_TOKEN / PR_NUM / GITHUB_REPOSITORY / PIPELINE_MODE（可选）
set -euo pipefail

: "${GH_TOKEN:?缺少 GH_TOKEN}" "${PR_NUM:?缺少 PR_NUM}"
export GH_REPO="${GITHUB_REPOSITORY:?缺少 GITHUB_REPOSITORY}"

REPORT=$(mktemp)
echo "## 🤖 测试 Agent 报告" > "$REPORT"
echo "- 流水线：[${GITHUB_RUN_ID:-本地}](https://github.com/${GH_REPO}/actions/runs/${GITHUB_RUN_ID:-})" >> "$REPORT"

# ---- 1. 解析 JUnit（来自 unit-tests 的 artifact 或本地构建）----
if ! find . -name "TEST-*.xml" | grep -q .; then
  echo "未找到 JUnit 报告，本机执行 ./gradlew testDebugUnitTest ..." >&2
  ./gradlew --no-daemon testDebugUnitTest || true
fi
TOTAL=0; FAILED=0
while IFS=$'\t' read -r errors failures skipped tests; do
  TOTAL=$((TOTAL + tests)); FAILED=$((FAILED + failures + errors))
done < <(find . -name "TEST-*.xml" -exec \
  sed -n 's/<testsuite[^>]*errors="\([0-9]*\)"[^>]*failures="\([0-9]*\)"[^>]*skipped="\([0-9]*\)"[^>]*tests="\([0-9]*\)".*/\1\t\2\t\3\t\4/p' {} \; 2>/dev/null) || true

if [ "$FAILED" -eq 0 ] && [ "$TOTAL" -gt 0 ]; then
  RESULT=green; echo "- 结论：✅ 共 $TOTAL 例，全部通过" >> "$REPORT"
else
  RESULT=red;   echo "- 结论：❌ 共 $TOTAL 例，失败/错误 $FAILED" >> "$REPORT"
fi

# ---- 2. 评论 + 标签 ----
gh pr comment "$PR_NUM" --body-file "$REPORT"
gh pr edit "$PR_NUM" --add-label "ci:${RESULT}" 2>/dev/null || \
  gh label create "ci:${RESULT}" --color "$( [ "$RESULT" = green ] && echo 0E8A16 || echo D93F0B )" >/dev/null 2>&1 && \
  gh pr edit "$PR_NUM" --add-label "ci:${RESULT}"

# ---- 3. auto 模式失败 → @claude 自动修复循环 ----
if [ "$RESULT" = "red" ] && [ "${PIPELINE_MODE:-hitl}" = "auto" ]; then
  gh pr comment "$PR_NUM" --body "@claude 单测失败 $FAILED 例，请定位根因修复后更新本 PR。"
fi

echo "测试结果：ci:${RESULT}"
