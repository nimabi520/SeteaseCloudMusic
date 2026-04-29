import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  base: "./", // 关键：给 Android file:///android_asset 场景
  build: {
    outDir: "dist",
    sourcemap: false
  }
});