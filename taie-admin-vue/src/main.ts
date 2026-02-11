import "@/assets/icons/iconfont/iconfont.js";
import RenDeptTree from "@/components/ren-dept-tree";
import RenRadioGroup from "@/components/ren-radio-group";
import RenSelect from "@/components/ren-select";
import ElementPlus, { ElMessageBoxShortcutMethod, Message } from "element-plus";
import "element-plus/theme-chalk/display.css";
import "element-plus/theme-chalk/index.css";
import Sortable from "sortablejs";
import "vite-plugin-svg-icons/register";
import { createApp } from "vue";
import { Store } from "vuex";
import VXETable from "vxe-table";
import "vxe-table/lib/style.css";
import "xe-utils";
import App from "./App.vue";
import { initI18n } from "./i18n";
import { IObject } from "./types/interface";
import router from "./router";
import store from "./store";
import * as ElementPlusIcons from "@element-plus/icons-vue";
import VFormRender from "vform3-builds/dist/render.umd.js";
import "vform3-builds/dist/render.style.css";
import axios from "axios";

//bug:https://www.zhihu.com/question/437009843
declare module "@vue/runtime-core" {
  interface ComponentCustomProperties {
    /**
     * vuex存储库
     */
    $store: Store<IObject>;
    /**
     * ref引用
     */
    $refs: any;
    /**
     * element-plus消息方法
     */
    $message: Message;
    /**
     * element-plus弹窗确认
     */
    $confirm: ElMessageBoxShortcutMethod;
    /**
     * element-plus弹窗
     */
    $alert: ElMessageBoxShortcutMethod;
    /**
     * vue3 v-model绑定默认字段名称
     */
    modelValue: any;
    /**
     * sortablejs组件
     */
    sortable: Sortable;
  }
}
VXETable.setup({
  zIndex: 3000,
  select: {
    transfer: true
  }
});
const app = createApp(App);

// elm icon图标
Object.keys(ElementPlusIcons).forEach((iconName) => {
  app.component(iconName, ElementPlusIcons[iconName as keyof typeof ElementPlusIcons]);
});

// 获取环境变量
const wsUrl = import.meta.env.VITE_WS_URL;
// console.log("wsUrl:", wsUrl);
// 扩展 Window 接口以包含 wsUrl 属性
declare global {
  interface Window {
    wsUrl: string;
  }
}
window.wsUrl = wsUrl;

app
  .use(store)
  .use(router)
  .use(RenRadioGroup)
  .use(RenSelect)
  .use(RenDeptTree)
  .use(ElementPlus, { size: "small" })
  .use(VFormRender)
  .use(VXETable)
  .use(initI18n)
  .mount("#app");


window.axios = axios;
