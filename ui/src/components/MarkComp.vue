<script lang="ts" setup>
import { defineProps, computed } from "vue";
import { Vec2d } from "../util";

const props = defineProps<{
  position: Vec2d;
  size: Vec2d;
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
    fill="#623b69"
    fill-opacity="0.85"
  />
</template>
