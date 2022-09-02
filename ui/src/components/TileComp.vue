<script lang="ts" setup>
import { Vec2d } from "@/util";
import { computed, defineProps } from "vue";

const props = defineProps<{
  position: Vec2d;
  size: Vec2d;
  color: string;
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
</template>
