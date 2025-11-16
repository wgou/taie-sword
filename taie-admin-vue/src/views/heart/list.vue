<template>
  <div class="mod-heart__list">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.pkg" placeholder="所属包" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.deviceId" placeholder="设备ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" table-layout="auto" style="width: 100%">
      <el-table-column prop="deviceId" label="设备ID" header-align="center" align="center" show-overflow-tooltip></el-table-column>
      <el-table-column prop="pkg" label="所属包" header-align="center" align="center" show-overflow-tooltip></el-table-column>
      <el-table-column prop="created" label="最近活动时间" header-align="center" align="center" sortable="custom"></el-table-column>
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
    },
    pkg: {
      type: String,
      default: ""
    }
  },
  setup(props) {
    const state = reactive({
      post: true,
      getDataListURL: "/heart/page",
      getDataListIsPage: true,
      dataForm: {
        deviceId: props.deviceId || "",
        pkg: props.pkg || ""
      }
    });

    return {
      ...useView(state),
      ...toRefs(state)
    };
  },
  mounted() {
    if (this.deviceId || this.pkg) {
      this.dataForm.deviceId = this.deviceId;
      this.dataForm.pkg = this.pkg;
      this.getDataList();
    }
  }
});
</script>

<style scoped>
.mod-heart__list {
  padding: 20px;
}
</style>

