<template>
  <div class="announcement-form-container">
    <el-card v-loading="loading" class="announcement-card">
      <template #header>
        <div class="card-header">
          <el-button :icon="ArrowLeft" @click="handleBack">返回列表</el-button>
          <span class="header-title">{{ isEdit ? '编辑公告' : '新建公告' }}</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="announcement-form">
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

        <el-form-item label="展示时间">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleTimeRangeChange"
          />
          <div class="form-tip">不设置展示时间时，公告将按默认策略持续可见。</div>
        </el-form-item>

        <el-form-item>
          <el-button type="info" :loading="saving" @click="handleSaveDraft">保存草稿</el-button>
          <el-button type="primary" :loading="publishing" @click="handlePublish">立即发布</el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { useAnnouncementStore } from '@/stores/announcement'
import { AnnouncementStatus } from '@/types/announcement'
import { navigateBack } from '@/utils/navigation'

const router = useRouter()
const route = useRoute()
const announcementStore = useAnnouncementStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)

const isEdit = computed(() => Boolean(route.params.id))
const timeRange = ref<[string, string] | null>(null)

const form = reactive({
  title: '',
  content: '',
  startTime: undefined as string | undefined,
  endTime: undefined as string | undefined
})

const editorOptions = {
  theme: 'snow',
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      ['blockquote', 'code-block'],
      [{ header: 1 }, { header: 2 }],
      [{ list: 'ordered' }, { list: 'bullet' }],
      [{ indent: '-1' }, { indent: '+1' }],
      [{ size: ['small', false, 'large', 'huge'] }],
      [{ header: [1, 2, 3, 4, 5, 6, false] }],
      [{ color: [] }, { background: [] }],
      [{ align: [] }],
      ['link', 'image'],
      ['clean']
    ]
  },
  placeholder: '请输入公告正文内容...'
}

const rules: FormRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 200, message: '公告标题不能超过 200 个字符', trigger: 'blur' }
  ],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const normalizeDateTime = (value?: string) => {
  if (!value) {
    return undefined
  }
  return value.replace('T', ' ')
}

const parseDateTime = (value?: string) => {
  if (!value) {
    return null
  }
  return new Date(value.replace(' ', 'T'))
}

const handleTimeRangeChange = (value: [string, string] | null) => {
  if (value) {
    form.startTime = normalizeDateTime(value[0])
    form.endTime = normalizeDateTime(value[1])
    return
  }
  form.startTime = undefined
  form.endTime = undefined
}

const handleBack = () => {
  navigateBack(router, '/announcement')
}

const validateTimeRange = () => {
  if (!form.startTime || !form.endTime) {
    return true
  }

  const startTime = parseDateTime(form.startTime)
  const endTime = parseDateTime(form.endTime)

  if (!startTime || !endTime || Number.isNaN(startTime.getTime()) || Number.isNaN(endTime.getTime())) {
    ElMessage.error('展示时间格式不正确')
    return false
  }

  if (startTime >= endTime) {
    ElMessage.error('开始时间必须早于结束时间')
    return false
  }

  return true
}

const buildPayload = (status?: AnnouncementStatus) => ({
  title: form.title,
  content: form.content,
  status,
  startTime: form.startTime,
  endTime: form.endTime
})

const handleSaveDraft = async () => {
  if (!formRef.value) {
    return
  }

  try {
    await formRef.value.validate()
    if (!validateTimeRange()) {
      return
    }

    saving.value = true
    if (isEdit.value) {
      await announcementStore.modifyAnnouncement(Number(route.params.id), buildPayload())
    } else {
      await announcementStore.createNewAnnouncement(buildPayload(AnnouncementStatus.DRAFT))
    }

    ElMessage.success('草稿已保存')
    router.push('/announcement')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存公告草稿失败:', error)
    }
  } finally {
    saving.value = false
  }
}

const handlePublish = async () => {
  if (!formRef.value) {
    return
  }

  try {
    await formRef.value.validate()
    if (!validateTimeRange()) {
      return
    }

    publishing.value = true
    if (isEdit.value) {
      await announcementStore.modifyAnnouncement(Number(route.params.id), buildPayload())
      await announcementStore.publishAnnouncementById(Number(route.params.id))
    } else {
      await announcementStore.createNewAnnouncement(buildPayload(AnnouncementStatus.PUBLISHED))
    }

    ElMessage.success('公告已发布')
    router.push('/announcement')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布公告失败:', error)
    }
  } finally {
    publishing.value = false
  }
}

const loadAnnouncementDetail = async () => {
  if (!isEdit.value) {
    return
  }

  try {
    loading.value = true
    const announcement = await announcementStore.fetchAdminAnnouncementDetail(Number(route.params.id))

    form.title = announcement.title
    form.content = announcement.content
    form.startTime = normalizeDateTime(announcement.startTime)
    form.endTime = normalizeDateTime(announcement.endTime)

    if (form.startTime && form.endTime) {
      timeRange.value = [form.startTime, form.endTime]
    }
  } catch (error) {
    console.error('加载公告详情失败:', error)
    ElMessage.error('加载公告详情失败')
    router.push('/announcement')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (isEdit.value) {
    loadAnnouncementDetail()
  }
})
</script>

<style scoped>
.announcement-form-container {
  padding: 20px;
}

.announcement-card {
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
  line-height: 1.6;
  color: #909399;
}

:deep(.el-date-editor) {
  width: min(100%, 480px);
}

:deep(.ql-container) {
  height: 350px;
}

:deep(.ql-editor) {
  min-height: 350px;
}
</style>
