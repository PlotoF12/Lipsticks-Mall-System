import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";
import HomePage from "../views/HomePage.vue";
const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: "/", component: HomePage },
        { path: "/login", redirect: "/user/login" },
        { path: "/mall", redirect: "/user/mall" },
        { path: "/recommend", redirect: "/user/recommend" },
        { path: "/visualization", redirect: "/user/visualization" },
        { path: "/product/:id", redirect: (to) => ({ path: `/user/product/${to.params.id}` }) },
        {
            path: "/user/login",
            component: () => import("../views/LoginPage.vue"),
            meta: { guest: true }
        },
        {
            path: "/user/register",
            component: () => import("../views/RegisterPage.vue"),
            meta: { guest: true }
        },
        { path: "/user/mall", component: () => import("../views/MallPage.vue") },
        { path: "/user/product/:id", component: () => import("../views/ProductDetailPage.vue"), meta: { requiresAuth: true } },
        { path: "/user/recommend", component: () => import("../views/RecommendPage.vue") },
        { path: "/user/visualization", component: () => import("../views/VisualizationPage.vue") },
        {
            path: "/user/tryon",
            component: () => import("../views/TryOnPage.vue"),
            meta: { requiresAuth: true }
        },
        {
            path: "/user/profile",
            component: () => import("../views/ProfilePage.vue"),
            meta: { requiresAuth: true }
        },
        {
            path: "/admin",
            component: () => import("../views/admin/AdminHomePage.vue"),
            meta: { requiresAdmin: true }
        },
        {
            path: "/admin/products",
            component: () => import("../views/admin/AdminProductsPage.vue"),
            meta: { requiresAdmin: true }
        },
        {
            path: "/admin/users",
            component: () => import("../views/admin/AdminUsersPage.vue"),
            meta: { requiresAdmin: true }
        }
    ]
});
router.beforeEach((to) => {
    const auth = useAuthStore();
    if (to.meta.requiresAdmin) {
        if (!auth.token) {
            return { path: "/user/login", query: { redirect: to.fullPath } };
        }
        if (auth.role !== "ADMIN") {
            return { path: "/" };
        }
    }
    if (to.meta.requiresAuth && !auth.token) {
        return { path: "/user/login", query: { redirect: to.fullPath } };
    }
    return true;
});
export default router;
