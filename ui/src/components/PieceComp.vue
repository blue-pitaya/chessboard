<script lang="ts" setup>
import { Vec2d } from "@/util";
import { computed, defineProps } from "vue";

const props = defineProps<{
  position: Vec2d;
  size: Vec2d;
  imageFilename: string;
}>();

const transformString = computed(
  () => `translate(${props.position.x}, ${props.position.y})`
);
</script>

<template>
  <image
    :transform="transformString"
    :xlink:href="require('@/assets/' + imageFilename)"
    :width="size.x"
    :height="size.y"
  />
</template>
