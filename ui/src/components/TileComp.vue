<script lang="ts" setup>
import { Vec2d } from "@/util";
import { computed, defineProps } from "vue";

const props = defineProps<{
  position: Vec2d;
  size: Vec2d;
  color: string;
  fileMark?: string;
  rankMark?: string;
}>();

const transformString = computed(
  () => `translate(${props.position.x}, ${props.position.y})`
);
</script>

<template>
  <rect
    :transform="transformString"
    :width="size.x"
    :height="size.y"
    :fill="color"
  />
  <foreignObject :transform="transformString" :width="size.x" :height="size.y">
    <div class="markContainer">
      <span>{{ rankMark }}</span>
      <span />
      <span />
      <span />
      <span />
      <span />
      <span />
      <span />
      <span>{{ fileMark }}</span>
    </div>
  </foreignObject>
</template>

<style scoped>
.markContainer {
  width: 100%;
  height: 100%;
  color: black;
  display: grid;
  grid-template-columns: auto 1fr auto;
  grid-template-rows: auto 1fr auto;
}

.markContainer span {
  pointer-events: none;
  user-select: none;
  margin: 5px;
}
</style>
