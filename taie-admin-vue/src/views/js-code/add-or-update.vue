<template>
  <div class="js-code-editor">
    <el-dialog 
      v-model="visible" 
      :title="!dataForm.id ? '新增' : '编辑'" 
      :close-on-click-modal="false" 
      :close-on-press-escape="false" 
      :fullscreen="true"
    >
      <el-form 
        :model="dataForm" 
        :rules="dataRule" 
        ref="dataFormRef" 
        label-width="100px" 
        size="default"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="dataForm.name" placeholder="请输入名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标识" prop="identification">
              <el-input v-model="dataForm.identification" placeholder="请输入标识"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input 
                v-model="dataForm.remark" 
                type="textarea" 
                :rows="2"
                placeholder="请输入备注"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="代码" prop="code">
          <monaco-editor 
            class="editor" 
            v-model="dataForm.code" 
            theme="vs-dark" 
            language="javascript" 
            style="width: 100%; height: 800px; border: 1px solid #dcdfe6; border-radius: 4px"
          />
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="dataFormSubmitHandle(true)" :loading="submitting">保存退出</el-button>
        <el-button type="primary" @click="dataFormSubmitHandle(false)" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from "vue";
import baseService from "@/service/baseService";
import { useDebounce } from "@/utils/utils";
import MonacoEditor from "@/components/monaco-editor/index.vue";
import { ElMessage } from "element-plus";

export default defineComponent({
  components: {
    MonacoEditor
  },
  setup() {
    const visible = ref(false);
    const submitting = ref(false);
    const dataFormRef = ref();

    const dataForm = reactive({
      id: "",
      name: "",
      identification: "",
      code: "",
      remark: ""
    });

    const dataRule = {
      name: [
        { required: true, message: "请输入名称", trigger: "blur" }
      ],
      identification: [
        { required: true, message: "请输入标识", trigger: "blur" }
      ],
      code: [
        { required: true, message: "请输入代码", trigger: "blur" }
      ]
    };

    return {
      visible,
      submitting,
      dataFormRef,
      dataForm,
      dataRule
    };
  },
  created() {
    this.dataFormSubmitHandle = useDebounce(this.dataFormSubmitHandle);
  },
  methods: {
    init() {
      this.visible = true;
      this.$nextTick(() => {
        // 重置表单
        this.dataFormRef?.resetFields();
        
        if (this.dataForm.id) {
          this.getInfo();
        } else {
          // 新增时清空表单
          this.dataForm.name = "";
          this.dataForm.identification = "";
          this.dataForm.code = "";
          this.dataForm.remark = "";
        }
      });
    },
    // 获取信息
    getInfo() {
      baseService.get(`/jsCode/${this.dataForm.id}`).then((res) => {
        if (res.code !== 0) {
          return ElMessage.error(res.msg);
        }
        Object.assign(this.dataForm, res.data);
      });
    },
    // 表单提交
    dataFormSubmitHandle(close: boolean) {
      this.dataFormRef?.validate((valid: boolean) => {
        if (!valid) {
          return false;
        }

        this.submitting = true;

        const submitFn = this.dataForm.id 
          ? baseService.post("/jsCode/update", this.dataForm)
          : baseService.post("/jsCode/save", this.dataForm);

        submitFn.then((res) => {
          this.submitting = false;
          if (res.code !== 0) {
            return ElMessage.error(res.msg);
          }
          ElMessage({
            message: "操作成功",
            type: "success",
            duration: 500,
            onClose: () => {
              if(close){
                this.visible = false;
              }
              this.$emit("refreshDataList");
            }
          });
        }).catch(() => {
          this.submitting = false;
        });
      });
    }
  }
});
</script>

<style scoped>
.js-code-editor :deep(.el-dialog__body) {
  padding: 20px;
}

.editor {
  border-radius: 4px;
  overflow: hidden;
}
.focused{
  border: 1px solid #409EFF!important;
}
</style>

