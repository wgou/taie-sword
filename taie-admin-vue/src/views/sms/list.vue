<template>
  <div class="mod-sms__list">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.deviceId" placeholder="设备ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.content" placeholder="短信内容" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" table-layout="auto" style="width: 100%">
      <el-table-column prop="deviceId" label="设备ID" header-align="center" align="center" width="200px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="content" label="短信内容" header-align="center" align="center" min-width="300px"></el-table-column>
      <el-table-column prop="created" label="创建时间" header-align="center" align="center" width="180px" sortable="custom">
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
  props: {
    deviceId: {
      type: String,
      default: ""
    }
  },
  setup(props) {
    const state = reactive({
      post: true,
      getDataListURL: "/smsInfo/page",
      getDataListIsPage: true,
      dataForm: {
        deviceId: props.deviceId || "",
        content: ""
      }
    });

    return {
      ...useView(state),
      ...toRefs(state)
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
    }
  }
});
</script>

<style scoped>
.mod-sms__list {
  padding: 20px;
}
</style>

