<template>
  <div class="mod-proxy__list">
    <!-- 查询表单 -->
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.pkg" placeholder="包名" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.proxyUser" placeholder="代理用户" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="openAddOrUpdateDialog()">{{ $t("add") }}</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" table-layout="auto" style="width: 100%">
      <el-table-column prop="id" label="ID" header-align="center" align="center" width="80px"></el-table-column>
      <el-table-column prop="pkg" label="包名" header-align="center" align="center" min-width="200px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="proxyUser" label="代理用户" header-align="center" align="center" width="200px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="created" label="创建时间" header-align="center" align="center" width="180px" sortable="custom">
        <template v-slot="scope">
          {{ formatDateTime(scope.row.created) }}
        </template>
      </el-table-column>
      <el-table-column prop="modified" label="修改时间" header-align="center" align="center" width="180px" sortable="custom">
        <template v-slot="scope">
          {{ formatDateTime(scope.row.modified) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" header-align="center" align="center" width="150px" fixed="right">
        <template v-slot="scope">
          <el-button type="danger" size="small" @click="deleteHandle(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      :current-page="page"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="limit"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="pageSizeChangeHandle"
      @current-change="pageCurrentChangeHandle"
    >
    </el-pagination>

    <!-- 新增/编辑弹窗 -->
    <el-dialog title="新增代理" v-model="addOrUpdateVisible" width="500px" :close-on-click-modal="false">
      <el-form :model="addOrUpdateForm" :rules="dataRule" ref="dataFormRef" label-width="100px">
        <el-form-item label="包名" prop="pkg">
          <el-input v-model="addOrUpdateForm.pkg" placeholder="请输入包名"></el-input>
        </el-form-item>
        <el-form-item label="代理用户" prop="proxyUser">
          <el-input v-model="addOrUpdateForm.proxyUser" placeholder="请输入代理用户"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addOrUpdateVisible = false">取消</el-button>
        <el-button type="primary" @click="dataFormSubmit()" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import useView from "@/hooks/useView";
import { defineComponent, reactive, toRefs, ref } from "vue";
import baseService from "@/service/baseService";
import { ElMessage, ElMessageBox } from "element-plus";

export default defineComponent({
  setup() {
    const state = reactive({
      post: true,
      getDataListURL: "/proxyInfo/page",
      getDataListIsPage: true,
      dataForm: {
        pkg: "",
        proxyUser: ""
      }
    });

    // 新增/编辑相关
    const addOrUpdateVisible = ref(false);
    const submitLoading = ref(false);
    const dataFormRef = ref();
    const addOrUpdateForm = reactive({
      pkg: "",
      proxyUser: ""
    });

    // 表单验证规则
    const dataRule = {
      pkg: [{ required: true, message: "包名不能为空", trigger: "blur" }],
      proxyUser: [{ required: true, message: "代理用户不能为空", trigger: "blur" }]
    };

    return {
      ...useView(state),
      ...toRefs(state),
      addOrUpdateVisible,
      submitLoading,
      dataFormRef,
      addOrUpdateForm,
      dataRule
    };
  },
  mounted() {
    this.getDataList();
  },
  methods: {
    // 格式化日期时间
    formatDateTime(date: Date | string | null) {
      if (!date) return "";

      const d = new Date(date);
      if (isNaN(d.getTime())) return "";

      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      const hours = String(d.getHours()).padStart(2, "0");
      const minutes = String(d.getMinutes()).padStart(2, "0");
      const seconds = String(d.getSeconds()).padStart(2, "0");

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },

    // 新增/编辑
    openAddOrUpdateDialog() {
      this.addOrUpdateVisible = true;
      this.addOrUpdateForm.pkg = "";
      this.addOrUpdateForm.proxyUser = "";
    },

    // 提交表单
    async dataFormSubmit() {
      this.dataFormRef?.validate(async (valid: boolean) => {
        if (!valid) {
          return false;
        }

        this.submitLoading = true;
        try {
          const { code, msg } = await baseService.post("/proxyInfo/save", {
            pkg: this.addOrUpdateForm.pkg,
            proxyUser: this.addOrUpdateForm.proxyUser
          });

          if (code === 0) {
            ElMessage({
              message: "操作成功",
              type: "success",
              duration: 1500
            });
            this.addOrUpdateVisible = false;
            this.getDataList();
          } else {
            ElMessage.error(msg || "操作失败");
          }
        } catch (error) {
          ElMessage.error("操作失败");
        } finally {
          this.submitLoading = false;
        }
      });
    },

    // 删除
    deleteHandle(id: number) {
      ElMessageBox.confirm("确定要删除该代理配置吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(async () => {
          try {
            const { code, msg } = await baseService.post("/proxyInfo/delete", { id });
            if (code === 0) {
              ElMessage({
                message: "删除成功",
                type: "success",
                duration: 1500
              });
              this.getDataList();
            } else {
              ElMessage.error(msg || "删除失败");
            }
          } catch (error) {
            ElMessage.error("删除失败");
          }
        })
        .catch(() => {
          // 取消删除
        });
    }
  }
});
</script>

<style scoped>
.mod-proxy__list {
  padding: 20px;
}
</style>
