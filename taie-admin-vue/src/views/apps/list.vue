<template>
  <div class="mod-install-app__list">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.deviceId" placeholder="设备ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.packageName" placeholder="应用包名" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.remark" placeholder="备注" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" table-layout="auto" style="width: 100%">
      <el-table-column prop="deviceId" label="设备ID" header-align="center" align="center" width="200px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="pkg" label="所属包" header-align="center" align="center" width="200px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="packageName" label="应用包名" header-align="center" align="center" min-width="250px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="remark" label="备注" header-align="center" align="center" min-width="200px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="created" label="最新上传时间" header-align="center" align="center" width="180px" sortable="custom">
        <template v-slot="scope">
          {{ formatDateTime(scope.row.created) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('handle')" header-align="center" align="center" width="100px" fixed="right">
        <template v-slot="scope">
          <el-button link type="primary" @click="openRemarkDialog(scope.row)">备注</el-button>
        </template>
      </el-table-column>
    </el-table>
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

    <!-- 备注弹窗 -->
    <el-dialog v-model="remarkDialogVisible" title="添加备注" width="500px" :close-on-click-modal="false">
      <div>
        <el-descriptions :column="1" border size="small" title="应用信息">
          <el-descriptions-item label="设备ID">{{ currentRow?.deviceId }}</el-descriptions-item>
          <el-descriptions-item label="所属包">{{ currentRow?.pkg }}</el-descriptions-item>
          <el-descriptions-item label="应用包名">{{ currentRow?.packageName }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div style="margin-top: 16px">
        <el-form label-width="80px">
          <el-form-item label="备注">
            <el-input v-model="remarkText" type="textarea" :rows="4" placeholder="请输入备注信息" clearable></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="remarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRemark" :loading="remarkSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import useView from "@/hooks/useView";
import baseService from "@/service/baseService";
import { defineComponent, reactive, toRefs, ref } from "vue";
import { ElMessage } from "element-plus";

export default defineComponent({
  props: {
    deviceId: {
      type: String,
      default: ""
    }
  },
  setup(props) {
    const state = reactive({
      post: true,
      getDataListURL: "/installApp/list",
      getDataListIsPage: true,
      dataForm: {
        deviceId: props.deviceId || "",
        packageName: "",
        remark: ""
      }
    });

    const remarkDialogVisible = ref(false);
    const remarkText = ref("");
    const remarkSubmitting = ref(false);
    const currentRow = ref<any | null>(null);

    return {
      ...useView(state),
      ...toRefs(state),
      remarkDialogVisible,
      remarkText,
      remarkSubmitting,
      currentRow
    };
  },
  mounted() {
    if (this.deviceId) {
      this.dataForm.deviceId = this.deviceId;
      this.getDataList();
    }
  },
  methods: {
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
    openRemarkDialog(row: any) {
      this.currentRow = row;
      this.remarkText = row.remark || "";
      this.remarkDialogVisible = true;
    },
    async submitRemark() {
      if (!this.currentRow) return;

      this.remarkSubmitting = true;
      try {
        const { code, msg } = await baseService.post("/installApp/addRemark", {
          id: this.currentRow.id,
          remark: this.remarkText || ""
        });
        if (code == 0) {
          ElMessage({ message: "操作成功", type: "success" });
          this.remarkDialogVisible = false;
          this.getDataList();
        } else {
          ElMessage({ message: msg || "操作失败", type: "error" });
        }
      } catch (error) {
        ElMessage({ message: "操作失败", type: "error" });
      } finally {
        this.remarkSubmitting = false;
      }
    }
  }
});
</script>

<style scoped>
.mod-install-app__list {
  padding: 20px;
}
</style>
