import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from "@/router.js"
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import axios from "axios";

// 全局配置 axios 携带 Cookie
axios.defaults.withCredentials = true;
axios.defaults.baseURL ='http://localhost:8085'
const app = createApp(App)
app.use(ElementPlus,{
    locale:zhCn
})
app.use(router)
app.mount('#app')

for(const [key,component]of Object.entries(ElementPlusIconsVue) ){
    app.component(key,component)
}