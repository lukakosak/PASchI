// Plugins
import vue from "@vitejs/plugin-vue";
import vuetify from "vite-plugin-vuetify";

// Utilities
import { defineConfig } from "vite";
import { fileURLToPath, URL } from "node:url";
import { VitePWA } from "vite-plugin-pwa";

// https://vitejs.dev/config/
export default defineConfig({
  define: { "process.env": {} },
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
    extensions: [".js", ".json", ".jsx", ".mjs", ".ts", ".tsx", ".vue"],
  },
  server: {
    port: 3000,
  },
  plugins: [
    vue(),
    // https://github.com/vuetifyjs/vuetify-loader/tree/next/packages/vite-plugin
    vuetify({
      autoImport: true,
      styles: { configFile: "src/styles/settings.scss" },
    }),
    VitePWA({
      registerType: "autoUpdate",
      devOptions: {
        enabled: true,
      },
      manifest: {
        icons: [
          {
            src: "/logo.png",
            sizes: "360x360",
            type: "image/png",
            purpose: "any maskable",
          },
        ],
      },
      workbox: {
        runtimeCaching: [
          {
            urlPattern: ({ url }) => url.pathname.startsWith("/api/"),
            handler: "NetworkFirst" as const,
            method: "GET",
            options: {
              cacheName: "api",
              cacheableResponse: {
                statuses: [0, 200],
              },
            },
          },
          {
            urlPattern: ({ url }) => url.pathname.startsWith("/api/"),
            handler: "NetworkOnly" as const,
            method: "POST",
            options: {
              backgroundSync: {
                name: "apiPOST",
                options: {
                  onSync: async ({ queue }) => {
                    let entry;
                    while ((entry = await queue.shiftRequest())) {
                      try {
                        await fetch(entry.request).then((response) => {
                          return response;
                        });
                      } catch (error) {
                        await queue.unshiftRequest(entry);
                        return;
                      }
                    }
                  },
                  forceSyncFallback: true,
                  maxRetentionTime: 30 * 24 * 60 * 60,
                },
              },
            },
          },
          {
            urlPattern: ({ url }) => url.pathname.startsWith("/api/"),
            handler: "NetworkOnly" as const,
            method: "PUT",
            options: {
              backgroundSync: {
                name: "apiPUT",
                options: {
                  onSync: async ({ queue }) => {
                    let entry;
                    while ((entry = await queue.shiftRequest())) {
                      try {
                        await fetch(entry.request).then((response) => {
                          return response;
                        });
                      } catch (error) {
                        await queue.unshiftRequest(entry);
                        return;
                      }
                    }
                  },
                  maxRetentionTime: 30 * 24 * 60 * 60,
                },
              },
            },
          },
          {
            urlPattern: ({ url }) => url.pathname.startsWith("/api/"),
            handler: "NetworkOnly" as const,
            method: "DELETE",
            options: {
              backgroundSync: {
                name: "apiDELETE",
                options: {
                  onSync: async ({ queue }) => {
                    let entry;
                    while ((entry = await queue.shiftRequest())) {
                      try {
                        await fetch(entry.request).then((response) => {
                          return response;
                        });
                      } catch (error) {
                        await queue.unshiftRequest(entry);
                        return;
                      }
                    }
                  },
                  maxRetentionTime: 30 * 24 * 60 * 60,
                },
              },
            },
          },
        ],
      },
    }),
  ],
});
