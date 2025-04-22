// 处理借阅请求
const handleBorrow = async (row) => {
  // 首先检查是否有库存
  if (row.availableCopies <= 0) {
    ElMessage.error('该图书库存不足，暂时无法借阅');
    return;
  }

  const userId = userInfo.value?.id;
  if (!userId) {
    ElMessage.warning('请先登录后再操作');
    return;
  }

  try {
    await addBorrowing({
      bookId: row.id,
      userId: userId
    });
    ElMessage.success('借阅申请已提交，请等待管理员审核');
    // 刷新图书列表以更新状态
    fetchBooks();
  } catch (error) {
    console.error('借阅申请失败', error);
    ElMessage.error('借阅申请失败: ' + (error.message || '未知错误'));
  }
}; 