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
import { defineComponent, reactive, toRefs } from "vue";

export default defineComponent({
  setup() {
    const state = reactive({
      post: true,
      getDataListURL: "/installApp/list",
      getDataListIsPage: true,
      dataForm: {
        deviceId: "",
        packageName: ""
      }
    });

    return { ...useView(state), ...toRefs(state) };
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
    }
  }
});
</script>

<style scoped>
.mod-install-app__list {
  padding: 20px;
}
</style>
