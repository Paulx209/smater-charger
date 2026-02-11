<template>
  <div class="announcement-form-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
          <span class="header-title">{{ isEdit ? '编辑公告' : '新增公告' }}</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="announcement-form"
      >
        <el-form-item label="公告标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入公告标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="公告内容" prop="content">
          <QuillEditor
            v-model:content="form.content"
            content-type="html"
            :options="editorOptions"
            style="height: 400px"
          />
        </el-form-item>

        <el-form-item label="生效时间范围">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            @change="handleTimeRangeChange"
          />
          <div class="form-tip">不设置表示长期有效</div>
        </el-form-item>

        <el-form-item>
          <el-button type="info" @click="handleSaveDraft" :loading="saving">
            保存草稿
          </el-button>
          <el-button type="primary" @click="handlePublish" :loading="publishing">
            发布
          </el-button>
          <el-button @click="handleBack">
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { useAnnouncementStore } from '@/stores/announcement'
import { AnnouncementStatus } from '@/types/announcement'

const router = useRouter()
const route = useRoute()
const announcementStore = useAnnouncementStore()

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)

// 是否编辑模式
const isEdit = computed(() => !!route.params.id)

// 时间范围
const timeRange = ref<[string, string] | null>(null)

// 表单数据
const form = reactive({
  title: '',
  content: '',
  startTime: undefined as string | undefined,
  endTime: undefined as string | undefined
})

// Quill编辑器配置
const editorOptions = {
  theme: 'snow',
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      ['blockquote', 'code-block'],
      [{ 'header': 1 }, { 'header': 2 }],
      [{ 'list': 'ordered' }, { 'list': 'bullet' }],
      [{ 'indent': '-1' }, { 'indent': '+1' }],
      [{ 'size': ['small', false, 'large', 'huge'] }],
      [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
      [{ 'color': [] }, { 'background': [] }],
      [{ 'align': [] }],
      ['link', 'image'],
      ['clean']
    ]
  },
  placeholder: '请输入公告内容...'
}

// 表单验证规则
const rules: FormRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 200, message: '标题最多200个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ]
}

// 时间范围改变
const handleTimeRangeChange = (value: [string, string] | null) => {
  if (value) {
    form.startTime = value[0]
    form.endTime = value[1]
  } else {
    form.startTime = undefined
    form.endTime = undefined
  }
}

// 返回列表
const handleBack = () => {
  router.back()
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    // 验证时间范围
    if (form.startTime && form.endTime) {
      if (new Date(form.startTime) >= new Date(form.endTime)) {
        ElMessage.error('开始时间必须小于结束时间')
        return
      }
    }

    saving.value = true

    if (isEdit.value) {
      // 编辑模式：更新公告
      await announcementStore.modifyAnnouncement(Number(route.params.id), {
        title: form.title,
        content: form.content,
        startTime: form.startTime,
        endTime: form.endTime
      })
    } else {
      // 新增模式：创建草稿
      await announcementStore.createNewAnnouncement({
        title: form.title,
        content: form.content,
        status: AnnouncementStatus.DRAFT,
        startTime: form.startTime,
        endTime: form.endTime
      })
    }

    ElMessage.success('保存成功')
    router.push('/admin/announcement')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('保存草稿失败:', error)
    }
  } finally {
    saving.value = false
  }
}

// 发布
const handlePublish = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    // 验证时间范围
    if (form.startTime && form.endTime) {
      if (new Date(form.startTime) >= new Date(form.endTime)) {
        ElMessage.error('开始时间必须小于结束时间')
        return
      }
    }

    publishing.value = true

    if (isEdit.value) {
      // 编辑模式：先更新，再发布
      await announcementStore.modifyAnnouncement(Number(route.params.id), {
        title: form.title,
        content: form.content,
        startTime: form.startTime,
        endTime: form.endTime
      })
      await announcementStore.publishAnnouncementById(Number(route.params.id))
    } else {
      // 新增模式：直接发布
      await announcementStore.createNewAnnouncement({
        title: form.title,
        content: form.content,
        status: AnnouncementStatus.PUBLISHED,
        startTime: form.startTime,
        endTime: form.endTime
      })
    }

    ElMessage.success('发布成功')
    router.push('/admin/announcement')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
    }
  } finally {
    publishing.value = false
  }
}

// 加载公告详情（编辑模式）
const loadAnnouncementDetail = async () => {
  if (!isEdit.value) return

  try {
    loading.value = true
    const announcement = await announcementStore.fetchAdminAnnouncementDetail(Number(route.params.id))

    form.title = announcement.title
    form.content = announcement.content
    form.startTime = announcement.startTime
    form.endTime = announcement.endTime

    // 设置时间范围
    if (announcement.startTime && announcement.endTime) {
      timeRange.value = [announcement.startTime, announcement.endTime]
    }
  } catch (error) {
    console.error('加载公告详情失败:', error)
    ElMessage.error('加载公告详情失败')
    router.push('/admin/announcement')
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  if (isEdit.value) {
    loadAnnouncementDetail()
  }
})
</script>

<style scoped>
.announcement-form-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.announcement-form {
  padding: 20px 0;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

:deep(.ql-container) {
  height: 350px;
}

:deep(.ql-editor) {
  min-height: 350px;
}
</style>
