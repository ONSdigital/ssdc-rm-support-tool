import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import eslint from "vite-plugin-eslint";

export default defineConfig(({ mode }) => {
  // vite-plugin-eslint fails the module transform on a lint error. Under Vitest
  // that aborts the test file before it runs, reporting no tests and a plugin
  // stack trace rather than the lint problem, so keep it out of test runs.
  // Linting is covered separately by `npx eslint .`.
  const lintPlugins = mode === "test" ? [] : [eslint()];

  return {
    build: {
      outDir: "build",
      assetsInlineLimit: 0,
    },
    plugins: [react(), ...lintPlugins],
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
    test: {
      globals: true,
      environment: "jsdom",
      setupFiles: "./src/setupTests.jsx",
    },
  };
});
