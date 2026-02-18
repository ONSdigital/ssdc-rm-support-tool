import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import eslint from "vite-plugin-eslint";

export default defineConfig(() => {
  return {
    build: {
      outDir: "build",
      assetsInlineLimit: 0,
    },
    plugins: [react(), eslint()],
    server: {
      port: 3000,
      proxy: {
        "/api": {
          target: "http://localhost:9999",
          changeOrigin: true,
          secure: false,
        },
      },
    },
  };
});
